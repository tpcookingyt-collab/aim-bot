package com.tpcooking.aimsuite;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tpcooking.aimsuite.engine.AimSettings;
import com.tpcooking.aimsuite.ui.TrainerView;

/**
 * Aimbot Suite Trainer - clean, self-contained Android version.
 * All engine features run on the app's own simulated bots.
 * No tracking, no webhook, no access to other apps.
 */
public class MainActivity extends Activity {
    private static final String PREFS = "AimbotSuite_Config";
    private TrainerView trainer;
    private AimSettings s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trainer = new TrainerView(this);
        s = trainer.settings;
        loadConfig();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0E1116);

        trainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(430)));
        root.addView(trainer);

        ScrollView scroll = new ScrollView(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(14), dp(10), dp(14), dp(24));
        scroll.addView(panel);
        root.addView(scroll);
        setContentView(root);

        panel.addView(header("🎯  Aimbot"));
        panel.addView(toggle("Enable Aimbot", s.aimbot, v -> { s.aimbot = v; saveConfig(); }));
        panel.addView(toggle("Wall Check", s.wallCheck, v -> { s.wallCheck = v; saveConfig(); }));
        panel.addView(toggle("Team Check", s.teamCheck, v -> { s.teamCheck = v; saveConfig(); }));
        panel.addView(toggle("Aim Prediction", s.prediction, v -> { s.prediction = v; saveConfig(); }));
        panel.addView(toggle("Target Lock", s.targetLock, v -> { s.targetLock = v; saveConfig(); }));
        panel.addView(toggle("Dynamic FOV", s.fovDynamic, v -> { s.fovDynamic = v; saveConfig(); }));
        panel.addView(toggle("Show FOV Circle", s.fovVisible, v -> { s.fovVisible = v; saveConfig(); }));
        panel.addView(slider("FOV Radius", 50, 500, s.aimFov, " px", v -> { s.aimFov = v; saveConfig(); }));
        panel.addView(slider("Smoothing", 1, 100, s.smoothing, "%", v -> { s.smoothing = v; saveConfig(); }));
        panel.addView(slider("Prediction Strength", 1, 50, s.predictionStrength, "%",
                v -> { s.predictionStrength = v; saveConfig(); }));
        panel.addView(slider("FOV Transparency", 0, 100, Math.round(s.fovTransparency * 100), "%",
                v -> { s.fovTransparency = v / 100f; saveConfig(); }));
        panel.addView(spinner("Target Part",
                new String[]{"Head", "HumanoidRootPart", "UpperTorso"}, s.targetPart,
                v -> { s.targetPart = v; saveConfig(); }));
        panel.addView(spinner("Target Priority",
                new String[]{"FOV", "Distance", "Health"}, s.targetPriority,
                v -> { s.targetPriority = v; saveConfig(); }));

        panel.addView(header("👁  ESP (trong trainer)"));
        panel.addView(toggle("Enable ESP", s.esp, v -> { s.esp = v; saveConfig(); }));
        panel.addView(toggle("2D Box ESP", s.box2d, v -> { s.box2d = v; saveConfig(); }));
        panel.addView(toggle("3D Box ESP", s.box3d, v -> { s.box3d = v; saveConfig(); }));
        panel.addView(toggle("Skeleton ESP", s.skeletonEsp, v -> { s.skeletonEsp = v; saveConfig(); }));
        panel.addView(toggle("Name ESP", s.nameEsp, v -> { s.nameEsp = v; saveConfig(); }));
        panel.addView(toggle("Health Bar", s.healthEsp, v -> { s.healthEsp = v; saveConfig(); }));
        panel.addView(toggle("Distance ESP", s.distanceEsp, v -> { s.distanceEsp = v; saveConfig(); }));
        panel.addView(toggle("Tracer ESP", s.tracerEsp, v -> { s.tracerEsp = v; saveConfig(); }));
        panel.addView(toggle("Weapon ESP", s.weaponEsp, v -> { s.weaponEsp = v; saveConfig(); }));
        panel.addView(toggle("Chams (Highlight)", s.highlightEsp, v -> { s.highlightEsp = v; saveConfig(); }));
        panel.addView(toggle("Radar", s.radarEsp, v -> { s.radarEsp = v; saveConfig(); }));
        panel.addView(slider("Text Size", 10, 24, s.espTextSize, " px", v -> { s.espTextSize = v; saveConfig(); }));
        panel.addView(slider("Max Distance", 100, 3000, s.espMaxDistance, " studs",
                v -> { s.espMaxDistance = v; saveConfig(); }));
        panel.addView(slider("Outline Thickness", 1, 5, s.espOutlineThick, "px",
                v -> { s.espOutlineThick = v; saveConfig(); }));

        panel.addView(header("⚙️  Trainer"));
        panel.addView(toggle("Auto Respawn Bots", s.autoRespawn, v -> { s.autoRespawn = v; saveConfig(); }));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button playPause = new Button(this);
        playPause.setText("⏸  Pause");
        playPause.setOnClickListener(v -> {
            boolean now = !trainer.isRunning();
            trainer.setRunning(now);
            playPause.setText(now ? "⏸  Pause" : "▶  Resume");
        });
        Button reset = new Button(this);
        reset.setText("↺  Reset Bots");
        reset.setOnClickListener(v -> trainer.resetBots());
        row.addView(playPause, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(reset, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        panel.addView(row);

        TextView note = new TextView(this);
        note.setText("Bản sạch: engine chỉ chạy trên bot mô phỏng trong app này. "
                + "Không tracker, không webhook, không truy cập game/app khác. "
                + "Chạm vào khung trainer để di chuyển tâm ngắm thủ công.");
        note.setTextColor(0xFF90A4AE);
        note.setTextSize(12f);
        note.setPadding(0, dp(12), 0, 0);
        panel.addView(note);
    }

    // ---------- UI helpers ----------
    private interface BoolCb { void onChanged(boolean v); }
    private interface IntCb { void onChanged(int v); }
    private interface StrCb { void onChanged(String v); }

    private TextView header(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(18f);
        tv.setPadding(0, dp(14), 0, dp(4));
        return tv;
    }

    private CheckBox toggle(String label, boolean initial, BoolCb cb) {
        CheckBox box = new CheckBox(this);
        box.setText(label);
        box.setTextColor(0xFFE0E0E0);
        box.setChecked(initial);
        box.setOnCheckedChangeListener((b, checked) -> { cb.onChanged(checked); trainer.invalidate(); });
        return box;
    }

    private LinearLayout slider(String label, int min, int max, int initial, String suffix, IntCb cb) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        TextView tv = new TextView(this);
        tv.setTextColor(0xFFE0E0E0);
        tv.setText(label + ": " + initial + suffix);
        SeekBar bar = new SeekBar(this);
        bar.setMax(max - min);
        bar.setProgress(initial - min);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean fromUser) {
                int v = p + min;
                tv.setText(label + ": " + v + suffix);
                if (fromUser) { cb.onChanged(v); trainer.invalidate(); }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) { }
            @Override public void onStopTrackingTouch(SeekBar sb) { }
        });
        wrap.addView(tv);
        wrap.addView(bar);
        return wrap;
    }

    private LinearLayout spinner(String label, String[] options, String initial, StrCb cb) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(0xFFE0E0E0);
        Spinner sp = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, options);
        sp.setAdapter(adapter);
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(initial)) sp.setSelection(i);
        }
        final boolean[] first = {true};
        sp.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent,
                                                 android.view.View view, int pos, long id) {
                if (first[0]) { first[0] = false; return; }
                cb.onChanged(options[pos]);
                trainer.invalidate();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
        wrap.addView(tv);
        wrap.addView(sp);
        return wrap;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    // ---------- Config persistence (như SaveConfig/LoadConfig gốc) ----------
    private void saveConfig() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putBoolean("aimbot", s.aimbot).putBoolean("wallCheck", s.wallCheck)
                .putBoolean("teamCheck", s.teamCheck).putBoolean("prediction", s.prediction)
                .putBoolean("targetLock", s.targetLock).putBoolean("fovDynamic", s.fovDynamic)
                .putBoolean("fovVisible", s.fovVisible).putInt("aimFov", s.aimFov)
                .putInt("smoothing", s.smoothing).putInt("predictionStrength", s.predictionStrength)
                .putFloat("fovTransparency", s.fovTransparency)
                .putString("targetPart", s.targetPart).putString("targetPriority", s.targetPriority)
                .putBoolean("esp", s.esp).putBoolean("box2d", s.box2d).putBoolean("box3d", s.box3d)
                .putBoolean("skeletonEsp", s.skeletonEsp).putBoolean("nameEsp", s.nameEsp)
                .putBoolean("healthEsp", s.healthEsp).putBoolean("distanceEsp", s.distanceEsp)
                .putBoolean("tracerEsp", s.tracerEsp).putBoolean("weaponEsp", s.weaponEsp)
                .putBoolean("highlightEsp", s.highlightEsp).putBoolean("radarEsp", s.radarEsp)
                .putInt("espTextSize", s.espTextSize).putInt("espMaxDistance", s.espMaxDistance)
                .putInt("espOutlineThick", s.espOutlineThick).putBoolean("autoRespawn", s.autoRespawn)
                .apply();
    }

    private void loadConfig() {
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (!p.contains("aimFov")) return;
        s.aimbot = p.getBoolean("aimbot", s.aimbot);
        s.wallCheck = p.getBoolean("wallCheck", s.wallCheck);
        s.teamCheck = p.getBoolean("teamCheck", s.teamCheck);
        s.prediction = p.getBoolean("prediction", s.prediction);
        s.targetLock = p.getBoolean("targetLock", s.targetLock);
        s.fovDynamic = p.getBoolean("fovDynamic", s.fovDynamic);
        s.fovVisible = p.getBoolean("fovVisible", s.fovVisible);
        s.aimFov = p.getInt("aimFov", s.aimFov);
        s.smoothing = p.getInt("smoothing", s.smoothing);
        s.predictionStrength = p.getInt("predictionStrength", s.predictionStrength);
        s.fovTransparency = p.getFloat("fovTransparency", s.fovTransparency);
        s.targetPart = p.getString("targetPart", s.targetPart);
        s.targetPriority = p.getString("targetPriority", s.targetPriority);
        s.esp = p.getBoolean("esp", s.esp);
        s.box2d = p.getBoolean("box2d", s.box2d);
        s.box3d = p.getBoolean("box3d", s.box3d);
        s.skeletonEsp = p.getBoolean("skeletonEsp", s.skeletonEsp);
        s.nameEsp = p.getBoolean("nameEsp", s.nameEsp);
        s.healthEsp = p.getBoolean("healthEsp", s.healthEsp);
        s.distanceEsp = p.getBoolean("distanceEsp", s.distanceEsp);
        s.tracerEsp = p.getBoolean("tracerEsp", s.tracerEsp);
        s.weaponEsp = p.getBoolean("weaponEsp", s.weaponEsp);
        s.highlightEsp = p.getBoolean("highlightEsp", s.highlightEsp);
        s.radarEsp = p.getBoolean("radarEsp", s.radarEsp);
        s.espTextSize = p.getInt("espTextSize", s.espTextSize);
        s.espMaxDistance = p.getInt("espMaxDistance", s.espMaxDistance);
        s.espOutlineThick = p.getInt("espOutlineThick", s.espOutlineThick);
        s.autoRespawn = p.getBoolean("autoRespawn", s.autoRespawn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        trainer.invalidate();
    }

    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
