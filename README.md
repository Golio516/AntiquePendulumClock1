# Antique Pendulum Clock Live Wallpaper

A minimal Android Studio Kotlin project for a luxury antique clock live wallpaper.

## What it does
- Uses the included portrait clock artwork as the wallpaper background.
- Animates a pendulum continuously while the wallpaper is visible.
- Draws real-time hour, minute, and second hands.
- Stops its animation loop when the wallpaper is not visible.

## Important calibration note
The pendulum and clock hands are drawn over approximate positions in the generated artwork. Depending on the exact crop/aspect ratio on your Realme 14x 5G, their positions may need small adjustments in `PendulumWallpaperService.kt` (`pivotX`, `pivotY`, `cx`, and `cy`).

## Build and install
1. Open this folder in Android Studio (current stable version).
2. Let Gradle sync. Use JDK 17.
3. Connect your phone or choose Build > Build Bundle(s) / APK(s) > Build APK(s).
4. Install the generated APK.
5. On your phone, open Settings > Wallpapers & style (wording may vary) > Live wallpapers.
6. Select **Antique Pendulum Clock** and apply it to the Home screen.

The project is source code; an APK must be built in Android Studio. This environment does not sign or install apps on your phone.
