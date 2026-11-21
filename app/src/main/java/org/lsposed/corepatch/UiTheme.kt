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
    /** Fallback accents: the RR icon lavender, tuned per scrim. */
    private val FALLBACK_ACCENT_BRIGHT = 0xFFB0B8F0.toInt()
    private val FALLBACK_ACCENT_DEEP = 0xFF2538C1.toInt()

    /** Wallpaper-derived accent, refreshed on every activity create. */
    @Volatile
    var accent: Int = FALLBACK_ACCENT_BRIGHT
        private set

    /** Must be called before the activity's setContentView. */
    fun apply(activity: Activity) {
        val isSystemNight = (activity.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        loadWallpaperAccent(activity, isSystemNight)
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

    /**
     * Picks the wallpaper's dominant color, normalised to a usable accent.
     * On the light scrim the accent must be dark to contrast; on the dark
     * scrim it must be bright.
     */
    private fun loadWallpaperAccent(activity: Activity, night: Boolean) {
        val fallback = if (night) FALLBACK_ACCENT_BRIGHT else FALLBACK_ACCENT_DEEP
        try {
            val colors = WallpaperManager.getInstance(activity)
                .getWallpaperColors(WallpaperManager.FLAG_SYSTEM) ?: run {
                accent = fallback; return
            }
            val hsv = FloatArray(3)
            Color.colorToHSV(colors.primaryColor.toArgb(), hsv)
            // Keep the wallpaper's hue; force visible saturation and a value
            // tuned to the active scrim.
            hsv[1] = hsv[1].coerceIn(0.45f, 0.85f)
            hsv[2] = if (night) 0.86f else 0.40f
            accent = Color.HSVToColor(hsv)
        } catch (_: Exception) {
            accent = fallback
        }
    }
}
