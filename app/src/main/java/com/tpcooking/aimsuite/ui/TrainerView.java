package com.tpcooking.aimsuite.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tpcooking.aimsuite.engine.AimEngine;
import com.tpcooking.aimsuite.engine.AimSettings;
import com.tpcooking.aimsuite.engine.Bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Self-contained aim trainer scene: simulated bots, ESP-style rendering
 * (box / skeleton / name / health / distance / tracer / radar), FOV circle
 * and a smoothed aim crosshair driven by AimEngine.
 * Everything drawn here belongs to this app; nothing reads other apps.
 */
public class TrainerView extends View {
    public final AimSettings settings = new AimSettings();
    private final AimEngine engine = new AimEngine();
    private final List<Bot> bots = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean running = true;
    private long lastFrameMs = 0;
    private float fps = 0f;
    private Bot currentTarget;
    // Two simulated walls (normalized coords: x, y, w, h)
    private final float[][] walls = {
            {0.30f, 0.30f, 0.05f, 0.34f},
            {0.66f, 0.48f, 0.05f, 0.34f}
    };

    public TrainerView(Context context) {
        super(context);
        init();
    }

    public TrainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        Random rng = new Random(42);
        String[] names = {"Alpha", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot", "Golf", "Hotel"};
        for (int i = 0; i < names.length; i++) {
            bots.add(new Bot(names[i], i >= 6, rng)); // last two are teammates
        }
        textPaint.setColor(Color.WHITE);
        setBackgroundColor(0xFF14181F);
    }

    public boolean isRunning() { return running; }
    public void setRunning(boolean r) { running = r; invalidate(); }
    public Bot getCurrentTarget() { return currentTarget; }
    public float getFps() { return fps; }
    public List<Bot> getBots() { return bots; }
    public AimEngine getEngine() { return engine; }

    public void resetBots() {
        bots.clear();
        init();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE) {
            if (getWidth() > 0 && getHeight() > 0) {
                engine.setAim(event.getX() / getWidth(), event.getY() / getHeight());
            }
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        long now = SystemClock.uptimeMillis();
        float dt = lastFrameMs == 0 ? 0.016f : Math.min((now - lastFrameMs) / 1000f, 0.05f);
        lastFrameMs = now;
        if (dt > 0) fps = fps * 0.9f + (1f / Math.max(dt, 0.001f)) * 0.1f;

        if (running) {
            for (Bot b : bots) b.update(dt, settings.autoRespawn);
        }

        drawGrid(canvas, w, h);
        drawWalls(canvas, w, h);

        float cx = w / 2f, cy = h / 2f;

        // Prepare pixel positions + wall-blocked flags for target selection.
        float[] botPx = new float[bots.size() * 2];
        boolean[] blocked = new boolean[bots.size()];
        for (int i = 0; i < bots.size(); i++) {
            Bot b = bots.get(i);
            float tx = b.x * w;
            float ty = ("Head".equals(settings.targetPart) ? b.headY() : b.y) * h;
            botPx[i * 2] = tx;
            botPx[i * 2 + 1] = ty;
            blocked[i] = isWallBlocked(cx, cy, tx, ty, w, h);
        }

        currentTarget = settings.aimbot
                ? engine.selectTarget(bots, botPx, cx, cy, settings, blocked)
                : null;

        if (settings.aimbot && currentTarget != null) {
            int idx = bots.indexOf(currentTarget);
            engine.stepAim(currentTarget, botPx[idx * 2] / w, botPx[idx * 2 + 1] / h,
                    currentTarget.vx, currentTarget.vy, settings);
        }

        for (Bot b : bots) drawBot(canvas, b, w, h, cx, cy);
        if (settings.esp && settings.radarEsp) drawRadar(canvas, w, h);
        drawHud(canvas, w, h);
        drawFovAndCrosshair(canvas, w, h, cx, cy);

        if (running) postInvalidateDelayed(16);
    }

    private boolean isWallBlocked(float cx, float cy, float tx, float ty, int w, int h) {
        for (float[] wall : walls) {
            if (AimEngine.segmentHitsRect(cx, cy, tx, ty,
                    wall[0] * w, wall[1] * h, wall[2] * w, wall[3] * h)) return true;
        }
        return false;
    }

    private void drawGrid(Canvas canvas, int w, int h) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        paint.setColor(0xFF232A35);
        int step = Math.max(32, w / 12);
        for (int x = 0; x <= w; x += step) canvas.drawLine(x, 0, x, h, paint);
        for (int y = 0; y <= h; y += step) canvas.drawLine(0, y, w, y, paint);
    }

    private void drawWalls(Canvas canvas, int w, int h) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF3A4356);
        for (float[] wall : walls) {
            canvas.drawRect(wall[0] * w, wall[1] * h,
                    (wall[0] + wall[2]) * w, (wall[1] + wall[3]) * h, paint);
        }
    }

    private void drawBot(Canvas canvas, Bot b, int w, int h, float cx, float cy) {
        if (!b.isAlive()) return;
        float bx = b.x * w, by = b.y * h;
        float bodyW = 34f * settings.espBoxScale;
        float bodyH = 62f * settings.espBoxScale;
        float left = bx - bodyW / 2f, right = bx + bodyW / 2f;
        float top = by - bodyH, bottom = by;
        int baseColor = b.teammate ? settings.teamColor : settings.enemyColor;
        boolean blocked = isWallBlocked(cx, cy, bx, b.headY() * h, w, h);

        // Highlight / chams fill
        if (settings.esp && settings.highlightEsp) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(withAlpha(baseColor, 70));
            canvas.drawRect(left, top, right, bottom, paint);
        }

        // Body + head (the bot itself)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(b.teammate ? 0xFF2E7D32 : 0xFF8E2424);
        canvas.drawRect(left + 6, top + 16, right - 6, bottom, paint);
        paint.setColor(0xFFE0B48C);
        canvas.drawCircle(bx, top + 9, 9f, paint);

        if (!settings.esp) return;

        float distPx = (float) Math.hypot(bx - cx, by - cy);
        float distStuds = distPx * 2.2f; // simulated scale
        if (distStuds > settings.espMaxDistance) return;
        int col = blocked ? 0xFF9E9E9E : baseColor;
        float alphaF = settings.distanceEsp
                ? clamp(1f - distStuds / settings.espMaxDistance, 0.25f, 1f) : 1f;
        int drawCol = withAlpha(col, Math.round(255 * (1f - settings.espTransparency) * alphaF));

        if (settings.tracerEsp) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f);
            paint.setColor(drawCol);
            canvas.drawLine(cx, h, bx, bottom, paint);
        }

        if (settings.box3d) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(settings.espOutlineThick);
            paint.setColor(drawCol);
            float inset = 6f;
            canvas.drawRect(left - inset, top - inset, right + inset, bottom + inset, paint);
            canvas.drawLine(left - inset, top - inset, left + inset, top + inset, paint);
            canvas.drawLine(right + inset, top - inset, right - inset, top + inset, paint);
        }
        if (settings.box2d) drawCornerBox(canvas, left, top, right, bottom, drawCol);

        if (settings.skeletonEsp) drawSkeleton(canvas, bx, top, bottom, drawCol);

        textPaint.setTextAlign(Paint.Align.CENTER);
        if (settings.nameEsp) {
            textPaint.setTextSize(settings.espTextSize + 3f);
            textPaint.setColor(drawCol);
            canvas.drawText(b.name, bx, top - 22f, textPaint);
        }
        if (settings.healthEsp) {
            float frac = b.health / b.maxHealth;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xAA000000);
            canvas.drawRect(left - 9, top, left - 5, bottom, paint);
            paint.setColor(frac > 0.5f ? 0xFF66BB6A : frac > 0.25f ? 0xFFFFCA28 : 0xFFEF5350);
            canvas.drawRect(left - 9, bottom - (bottom - top) * frac, left - 5, bottom, paint);
            textPaint.setTextSize(settings.espTextSize);
            textPaint.setColor(Color.WHITE);
            canvas.drawText(String.valueOf(Math.round(b.health)), bx, top - 6f, textPaint);
        }
        float labelY = bottom + 16f;
        if (settings.distanceEsp) {
            textPaint.setTextSize(settings.espTextSize);
            textPaint.setColor(drawCol);
            canvas.drawText(Math.round(distStuds) + " studs", bx, labelY, textPaint);
            labelY += 15f;
        }
        if (settings.weaponEsp) {
            textPaint.setTextSize(settings.espTextSize);
            textPaint.setColor(Color.WHITE);
            canvas.drawText(b.weapon, bx, labelY, textPaint);
        }
    }

    private void drawCornerBox(Canvas canvas, float l, float t, float r, float b, int col) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(settings.espOutlineThick + 0.5f);
        paint.setColor(col);
        float cw = (r - l) * 0.28f, ch = (b - t) * 0.28f;
        canvas.drawLine(l, t, l + cw, t, paint); canvas.drawLine(l, t, l, t + ch, paint);
        canvas.drawLine(r, t, r - cw, t, paint); canvas.drawLine(r, t, r, t + ch, paint);
        canvas.drawLine(l, b, l + cw, b, paint); canvas.drawLine(l, b, l, b - ch, paint);
        canvas.drawLine(r, b, r - cw, b, paint); canvas.drawLine(r, b, r, b - ch, paint);
    }

    private void drawSkeleton(Canvas canvas, float bx, float top, float bottom, int col) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(col);
        float neck = top + 18f, hip = top + (bottom - top) * 0.58f;
        canvas.drawLine(bx, neck, bx, hip, paint);                       // spine
        canvas.drawLine(bx - 13f, neck + 6f, bx + 13f, neck + 6f, paint); // arms
        canvas.drawLine(bx, hip, bx - 10f, bottom, paint);
        canvas.drawLine(bx, hip, bx + 10f, bottom, paint);
    }

    private void drawRadar(Canvas canvas, int w, int h) {
        float size = 132f, rx = w - size - 12f, ry = 12f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xB010141A);
        canvas.drawRect(rx, ry, rx + size, ry + size, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(0xFF4CAF50);
        canvas.drawRect(rx, ry, rx + size, ry + size, paint);
        canvas.drawLine(rx + size / 2f, ry, rx + size / 2f, ry + size, paint);
        canvas.drawLine(rx, ry + size / 2f, rx + size, ry + size / 2f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(rx + size / 2f, ry + size / 2f, 3f, paint);
        for (Bot b : bots) {
            if (!b.isAlive()) continue;
            paint.setColor(b.teammate ? settings.teamColor : settings.enemyColor);
            canvas.drawCircle(rx + b.x * size, ry + b.y * size, 4f, paint);
        }
    }

    private void drawHud(Canvas canvas, int w, int h) {
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(15f);
        textPaint.setColor(0xFFB0BEC5);
        int alive = 0;
        for (Bot b : bots) if (b.isAlive()) alive++;
        String target = currentTarget == null ? "-" : currentTarget.name;
        canvas.drawText(String.format("FPS %.0f  |  Bots %d  |  Target %s", fps, alive, target),
                12f, 24f, textPaint);
    }

    private void drawFovAndCrosshair(Canvas canvas, int w, int h, float cx, float cy) {
        if (settings.aimbot && settings.fovVisible) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f);
            paint.setColor(withAlpha(0xFFFFFF00, Math.round(255 * (1f - settings.fovTransparency))));
            canvas.drawCircle(cx, cy, engine.dynamicFovPx(settings, 500f), paint);
        }
        float ax = engine.getAimX() * w, ay = engine.getAimY() * h;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(currentTarget != null ? 0xFFFF5252 : 0xFFFFFFFF);
        canvas.drawLine(ax - 12, ay, ax - 4, ay, paint);
        canvas.drawLine(ax + 4, ay, ax + 12, ay, paint);
        canvas.drawLine(ax, ay - 12, ax, ay - 4, paint);
        canvas.drawLine(ax, ay + 4, ax, ay + 12, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(ax, ay, 2f, paint);
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static int withAlpha(int color, int alpha) {
        return Color.argb(clampInt(alpha), Color.red(color), Color.green(color), Color.blue(color));
    }

    private static int clampInt(int v) { return Math.max(0, Math.min(255, v)); }
}
