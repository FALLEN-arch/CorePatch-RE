package org.lsposed.corepatch.ui

import android.content.Context
import android.content.SharedPreferences

/**
 * App-local UI preferences (theme mode, accent tone, font, language),
 * stored in the "ui" file — separate from the Xposed remote patch config.
 */
object UiPrefs {
    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    const val FONT_JETBRAINS = "jetbrainsmono"
    const val FONT_IBM_PLEX = "ibmplexmono"

    const val LANG_SYSTEM = "system"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences("ui", Context.MODE_PRIVATE)

    fun theme(context: Context): String =
        prefs(context).getString("theme", THEME_SYSTEM) ?: THEME_SYSTEM

    fun setTheme(context: Context, value: String) {
        prefs(context).edit().putString("theme", value).apply()
    }

    fun accentIndex(context: Context): Int =
        prefs(context).getInt("accent", 0)

    fun setAccentIndex(context: Context, value: Int) {
        prefs(context).edit().putInt("accent", value).apply()
    }

    fun font(context: Context): String =
        prefs(context).getString("font", FONT_JETBRAINS) ?: FONT_JETBRAINS

    fun setFont(context: Context, value: String) {
        prefs(context).edit().putString("font", value).apply()
    }

    fun language(context: Context): String =
        prefs(context).getString("lang", LANG_SYSTEM) ?: LANG_SYSTEM

    fun setLanguage(context: Context, value: String) {
        prefs(context).edit().putString("lang", value).apply()
    }
}
