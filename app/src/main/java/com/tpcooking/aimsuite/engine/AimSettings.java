package com.tpcooking.aimsuite.engine;

/**
 * Settings for the in-app aim trainer engine.
 * Defaults mirror the original AimbotSuite configuration.
 * This engine only operates on bots inside this app's own trainer scene.
 */
public class AimSettings {
    // Aimbot
    public boolean aimbot = false;
    public boolean wallCheck = true;
    public boolean teamCheck = true;
    public boolean prediction = false;
    public int predictionStrength = 10;   // percent
    public boolean targetLock = false;
    public boolean fovDynamic = false;
    public boolean fovVisible = true;
    public int aimFov = 150;              // px
    public int smoothing = 15;            // percent
    public float fovTransparency = 0.7f;
    public String targetPart = "Head";    // Head | HumanoidRootPart | UpperTorso
    public String targetPriority = "FOV"; // FOV | Distance | Health

    // ESP (rendered inside the trainer scene only)
    public boolean esp = false;
    public boolean box2d = true;
    public boolean box3d = false;
    public boolean skeletonEsp = false;
    public boolean nameEsp = true;
    public boolean healthEsp = true;
    public boolean distanceEsp = true;
    public boolean tracerEsp = false;
    public boolean weaponEsp = false;
    public boolean highlightEsp = false;
    public boolean radarEsp = false;
    public int espTextSize = 13;
    public float espTransparency = 0.2f;
    public int espOutlineThick = 1;
    public int espMaxDistance = 1500;     // studs (simulated)
    public float espBoxScale = 1.0f;
    public int enemyColor = 0xFFFF5050;
    public int teamColor = 0xFF50FF50;

    // Misc
    public boolean autoRespawn = true;
}
