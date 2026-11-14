package com.tpcooking.aimsuite.engine;

import java.util.Random;

/** A simulated bot moving inside the trainer arena (normalized 0..1 coords). */
public class Bot {
    public final String name;
    public final boolean teammate;
    public String weapon;
    public float x, y;        // center position, normalized
    public float vx, vy;      // velocity, normalized units / second
    public float health;
    public final float maxHealth = 100f;
    private float respawnTimer = 0f;
    private final Random random = new Random();

    private static final String[] WEAPONS = {"Rifle", "SMG", "Sniper", "Shotgun", "Pistol", "LMG"};

    public Bot(String name, boolean teammate, Random rng) {
        this.name = name;
        this.teammate = teammate;
        this.weapon = WEAPONS[rng.nextInt(WEAPONS.length)];
        this.x = 0.15f + rng.nextFloat() * 0.7f;
        this.y = 0.15f + rng.nextFloat() * 0.7f;
        float speed = 0.08f + rng.nextFloat() * 0.14f;
        double angle = rng.nextDouble() * Math.PI * 2.0;
        this.vx = (float) (Math.cos(angle) * speed);
        this.vy = (float) (Math.sin(angle) * speed);
        this.health = 40f + rng.nextFloat() * 60f;
    }

    public boolean isAlive() {
        return health > 0f;
    }

    /** Head position in normalized coords (body is drawn below it). */
    public float headY() {
        return y - 0.055f;
    }

    public void update(float dt, boolean autoRespawn) {
        if (!isAlive()) {
            if (autoRespawn) {
                respawnTimer += dt;
                if (respawnTimer > 2f) {
                    respawnTimer = 0f;
                    health = maxHealth;
                    x = 0.15f + random.nextFloat() * 0.7f;
                    y = 0.15f + random.nextFloat() * 0.7f;
                }
            }
            return;
        }
        // Slow random damage / regen so health bars move during training.
        health += (random.nextFloat() - 0.52f) * 18f * dt;
        if (health > maxHealth) health = maxHealth;
        if (health < 0f) health = 0f;

        x += vx * dt;
        y += vy * dt;
        if (x < 0.08f) { x = 0.08f; vx = Math.abs(vx); }
        if (x > 0.92f) { x = 0.92f; vx = -Math.abs(vx); }
        if (y < 0.14f) { y = 0.14f; vy = Math.abs(vy); }
        if (y > 0.92f) { y = 0.92f; vy = -Math.abs(vy); }
    }
}
