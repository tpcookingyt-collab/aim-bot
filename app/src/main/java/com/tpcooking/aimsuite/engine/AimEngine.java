package com.tpcooking.aimsuite.engine;

import java.util.List;

/**
 * Target selection + smoothed aim for the trainer scene.
 * Pure math on the app's own bots: no access to any other app or game.
 */
public class AimEngine {
    private Bot lockedTarget;
    private float aimX = 0.5f, aimY = 0.5f; // normalized crosshair position

    public float getAimX() { return aimX; }
    public float getAimY() { return aimY; }

    public void setAim(float x, float y) {
        aimX = clamp01(x);
        aimY = clamp01(y);
    }

    public Bot getLockedTarget() { return lockedTarget; }

    /** Dynamic FOV in pixels: grows when the closest bot is near. */
    public float dynamicFovPx(AimSettings s, float closestDistPx) {
        if (!s.fovDynamic) return s.aimFov;
        float scale = 1f + (500f - Math.min(closestDistPx, 500f)) / 500f;
        return clamp(s.aimFov * scale, s.aimFov, s.aimFov * 2f);
    }

    /**
     * Pick the best target around the screen center.
     * Coordinates are pixels; bots are given in pixels by the caller.
     */
    public Bot selectTarget(List<Bot> bots, float[] botPx, float centerX, float centerY,
                            AimSettings s, boolean[] wallBlocked) {
        if (s.targetLock && lockedTarget != null && lockedTarget.isAlive()
                && !(s.teamCheck && lockedTarget.teammate)) {
            return lockedTarget;
        }
        Bot best = null;
        float bestScore = Float.MAX_VALUE;
        float fov = dynamicFovPx(s, 500f);
        for (int i = 0; i < bots.size(); i++) {
            Bot b = bots.get(i);
            if (!b.isAlive()) continue;
            if (s.teamCheck && b.teammate) continue;
            if (s.wallCheck && wallBlocked != null && wallBlocked[i]) continue;
            float px = botPx[i * 2];
            float py = botPx[i * 2 + 1];
            float dx = px - centerX, dy = py - centerY;
            float fovDist = (float) Math.sqrt(dx * dx + dy * dy);
            if (fovDist > fov) continue;
            float score;
            switch (s.targetPriority) {
                case "Distance": score = fovDist; break;
                case "Health":   score = b.health; break;
                default:         score = fovDist; break; // FOV
            }
            if (score < bestScore) {
                bestScore = score;
                best = b;
            }
        }
        lockedTarget = best;
        return best;
    }

    /**
     * Move the crosshair toward the target with optional prediction + smoothing.
     * Target position/velocity are normalized (0..1). Returns the new aim point.
     */
    public float[] stepAim(Bot target, float targetX, float targetY,
                           float targetVx, float targetVy, AimSettings s) {
        if (target == null) return new float[]{aimX, aimY};
        float goalX = targetX, goalY = targetY;
        if (s.prediction) {
            float lead = s.predictionStrength / 100f * 0.35f; // seconds of lead, scaled
            goalX += targetVx * lead;
            goalY += targetVy * lead;
        }
        // Higher smoothing % = slower, smoother movement.
        float alpha = clamp(1.05f - s.smoothing / 100f, 0.05f, 1f);
        aimX += (goalX - aimX) * alpha;
        aimY += (goalY - aimY) * alpha;
        aimX = clamp01(aimX);
        aimY = clamp01(aimY);
        return new float[]{aimX, aimY};
    }

    /** Segment vs axis-aligned rect intersection (for the simulated wall check). */
    public static boolean segmentHitsRect(float x1, float y1, float x2, float y2,
                                          float rx, float ry, float rw, float rh) {
        // Quick reject / accept using slab test.
        float dx = x2 - x1, dy = y2 - y1;
        float tmin = 0f, tmax = 1f;
        if (Math.abs(dx) < 1e-6f) {
            if (x1 < rx || x1 > rx + rw) return false;
        } else {
            float t1 = (rx - x1) / dx, t2 = (rx + rw - x1) / dx;
            if (t1 > t2) { float t = t1; t1 = t2; t2 = t; }
            tmin = Math.max(tmin, t1); tmax = Math.min(tmax, t2);
            if (tmin > tmax) return false;
        }
        if (Math.abs(dy) < 1e-6f) {
            if (y1 < ry || y1 > ry + rh) return false;
        } else {
            float t1 = (ry - y1) / dy, t2 = (ry + rh - y1) / dy;
            if (t1 > t2) { float t = t1; t1 = t2; t2 = t; }
            tmin = Math.max(tmin, t1); tmax = Math.min(tmax, t2);
            if (tmin > tmax) return false;
        }
        return true;
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }
    private static float clamp01(float v) { return clamp(v, 0f, 1f); }
}
