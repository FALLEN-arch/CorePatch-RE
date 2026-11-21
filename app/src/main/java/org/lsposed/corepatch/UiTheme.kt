package org.lsposed.corepatch

import android.app.Activity
import android.content.res.Configuration
import android.view.WindowManager

/**
 * Terminal-glass theming. Theme follows system dark mode; the page scrim is
 * translucent so the wallpaper shows through (blurred on Android 12+).
 * Accents are RR's brand palette — lavender page and deep-navy accent
 * extracted from the icon's outline color, computed for 8.7:1 contrast on
 * the light scrim — so RR always looks like RR regardless of wallpaper.
 */
object UiTheme {
    private const val FALLBACK_ACCENT_DEEP = 0xFF2538C1.toInt()
    private const val FALLBACK_ACCENT_BRIGHT = 0xFFB0B8F0.toInt()

    @Volatile
    var accent: Int = FALLBACK_ACCENT_DEEP
        private set

    fun apply(activity: Activity) {
        val isSystemNight = (activity.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        activity.setTheme(if (isSystemNight) R.style.AppTheme_Dark else R.style.AppTheme)

        val w = activity.window
        w.setStatusBarColor(android.graphics.Color.TRANSPARENT)
        w.setNavigationBarColor(android.graphics.Color.TRANSPARENT)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            w.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            w.attributes = w.attributes.apply {
                blurBehindRadius = (48 * activity.resources.displayMetrics.density).toInt()
            }
        }
        accent = if (isSystemNight) FALLBACK_ACCENT_BRIGHT else FALLBACK_ACCENT_DEEP
    }
}
