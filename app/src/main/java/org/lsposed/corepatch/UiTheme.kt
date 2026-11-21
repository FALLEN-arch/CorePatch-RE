package org.lsposed.corepatch

import android.app.Activity
import android.app.WallpaperManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.WindowManager

/**
 * Terminal-glass theming. The theme follows the system dark mode; the page
 * scrim is translucent so the (blurred) wallpaper shows through, and the
 * accent hue is derived from the wallpaper itself.
 */
object UiTheme {
    /** Fallback accent: the RR icon lavender. */
    private val FALLBACK_ACCENT = 0xFFB0B8F0.toInt() // the RR icon lavender

    /** Wallpaper-derived accent, refreshed on every activity create. */
    @Volatile
    var accent: Int = FALLBACK_ACCENT
        private set

    /** Must be called before the activity's setContentView. */
    fun apply(activity: Activity) {
        loadWallpaperAccent(activity)
        val isSystemNight = (activity.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        activity.setTheme(if (isSystemNight) R.style.AppTheme_Dark else R.style.AppTheme)

        val w = activity.window
        w.setStatusBarColor(Color.TRANSPARENT)
        w.setNavigationBarColor(Color.TRANSPARENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Frosted glass: blur the wallpaper behind the translucent page.
            w.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            w.attributes = w.attributes.apply {
                blurBehindRadius = (48 * activity.resources.displayMetrics.density).toInt()
            }
        }
    }

    /** Picks the wallpaper's dominant color, normalised to a usable accent. */
    private fun loadWallpaperAccent(activity: Activity) {
        try {
            val colors = WallpaperManager.getInstance(activity)
                .getWallpaperColors(WallpaperManager.FLAG_SYSTEM) ?: return
            val hsv = FloatArray(3)
            Color.colorToHSV(colors.primaryColor.toArgb(), hsv)
            // Keep the wallpaper's hue; force visible saturation and a light
            // value so the accent reads on both scrims.
            hsv[1] = hsv[1].coerceIn(0.35f, 0.85f)
            hsv[2] = 0.86f
            accent = Color.HSVToColor(hsv)
        } catch (_: Exception) {
            accent = FALLBACK_ACCENT
        }
    }
}
