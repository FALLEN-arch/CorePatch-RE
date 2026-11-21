package org.lsposed.corepatch

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.view.WindowManager
import org.lsposed.corepatch.ui.UiPrefs

/**
 * Terminal-glass theming. Theme follows the Appearance setting (system /
 * light / dark); the page scrim is translucent so the wallpaper shows
 * through (blurred on Android 12+). Accents come from RR's brand palette —
 * deep navy on the light scrim, lavender-family tones on dark — so RR
 * always looks like RR regardless of wallpaper.
 */
object UiTheme {
    private const val FALLBACK_ACCENT_DEEP = 0xFF2538C1.toInt()
    private const val FALLBACK_ACCENT_BRIGHT = 0xFFB0B8F0.toInt()

    // Type weights for mono().
    const val WEIGHT_REGULAR = 0
    const val WEIGHT_MEDIUM = 1
    const val WEIGHT_BOLD = 2

    /** An accent tone: a deep variant for the light scrim, a bright one for dark. */
    data class Accent(val id: String, val light: Int, val dark: Int)

    /** Accent tones offered in Settings ▸ Appearance. Order matches the
     *  accent_* label resources in SettingsScreen. */
    val ACCENTS = listOf(
        Accent("rr", 0xFF2538C1.toInt(), 0xFFB0B8F0.toInt()),
        Accent("phosphor", 0xFF1B7A3D.toInt(), 0xFF6EE7A0.toInt()),
        Accent("amber", 0xFFB45309.toInt(), 0xFFFBBF24.toInt()),
        Accent("cyan", 0xFF0E7490.toInt(), 0xFF67E8F9.toInt()),
    )

    /** True when the resolved theme is dark. */
    @Volatile
    var isDark: Boolean = false
        private set

    @Volatile
    var accent: Int = FALLBACK_ACCENT_DEEP
        private set

    fun apply(activity: Activity) {
        val sysNight = (activity.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        isDark = when (UiPrefs.theme(activity)) {
            UiPrefs.THEME_LIGHT -> false
            UiPrefs.THEME_DARK -> true
            else -> sysNight
        }
        activity.setTheme(if (isDark) R.style.AppTheme_Dark else R.style.AppTheme)

        val w = activity.window
        w.setStatusBarColor(android.graphics.Color.TRANSPARENT)
        w.setNavigationBarColor(android.graphics.Color.TRANSPARENT)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            w.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            w.attributes = w.attributes.apply {
                blurBehindRadius = (48 * activity.resources.displayMetrics.density).toInt()
            }
        }
        val tone = ACCENTS.getOrNull(UiPrefs.accentIndex(activity)) ?: ACCENTS[0]
        accent = if (isDark) tone.dark else tone.light
    }

    /** Bundled mono typeface honoring the font setting. */
    fun mono(context: Context, weight: Int): Typeface {
        val id = when (UiPrefs.font(context)) {
            UiPrefs.FONT_IBM_PLEX -> when (weight) {
                WEIGHT_MEDIUM -> R.font.ibmplexmono_medium
                WEIGHT_BOLD -> R.font.ibmplexmono_bold
                else -> R.font.ibmplexmono_regular
            }
            else -> when (weight) {
                WEIGHT_MEDIUM -> R.font.jetbrainsmono_medium
                WEIGHT_BOLD -> R.font.jetbrainsmono_bold
                else -> R.font.jetbrainsmono_regular
            }
        }
        return context.resources.getFont(id)
    }
}
