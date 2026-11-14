# Aimbot Suite Trainer — Android (bản sạch)

Project Android native (Java, không cần AndroidX) được build hoàn toàn bằng GitHub Actions.

## Tải APK

1. Vào tab **Actions** của repo → chọn run **Build Android APK** mới nhất (màu xanh).
2. Kéo xuống **Artifacts** → tải **AimbotSuite-APKs**.
3. Trong file zip có:
   - `app-debug.apk` — bản debug, cài trực tiếp được.
   - `app-release.apk` — bản release, đã ký bằng debug key để cài thử (thay keystore thật trước khi phát hành chính thức).

Muốn build lại: tab **Actions** → **Build Android APK** → **Run workflow**.
Muốn tạo Release có APK đính kèm: push tag dạng `v1.0`.

## Build local (tuỳ chọn)

Cần JDK 17 + Android SDK platform 34 + Gradle 8.7:

```bash
gradle assembleDebug assembleRelease
```

APK nằm ở `app/build/outputs/apk/`.

## Engine trong app

- `engine/AimEngine.java` — chọn mục tiêu (ưu tiên FOV / Distance / Health), dynamic FOV, prediction, smoothing, target lock, wall-check bằng phép cắt đoạn thẳng–hình chữ nhật.
- `engine/Bot.java` — bot mô phỏng di chuyển, máu, respawn.
- `ui/TrainerView.java` — vẽ scene trainer: FOV circle, ESP box 2D/3D, skeleton, name, health, distance, tracer, weapon, radar, HUD FPS/target.
- `MainActivity.java` — toàn bộ toggle/slider/spinner cấu hình, lưu bằng SharedPreferences (`AimbotSuite_Config`).

Bản này **không** có tracker/webhook, không truy cập Roblox hay bất kỳ app/game nào khác; engine chỉ chạy trên bot mô phỏng trong chính app.
