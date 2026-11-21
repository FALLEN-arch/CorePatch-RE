package org.lsposed.corepatch

import android.app.Activity
import android.content.Context
import android.content.res.Configuration

/** UI theme selection, persisted in app-local preferences. */
object UiTheme {
    private const val PREFS = "ui"
    private const val KEY = "theme"

    const val SYSTEM = "system"
    const val LIGHT = "light"
    const val DARK = "dark"
    const val AMOLED = "amoled"

    fun get(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, SYSTEM) ?: SYSTEM
    }

    fun set(context: Context, value: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, value).apply()
    }

    /** Must be called before the activity's setContentView. */
    fun apply(activity: Activity) {
        val isSystemNight = (activity.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        activity.setTheme(
            when (get(activity)) {
                LIGHT -> R.style.AppTheme
                DARK -> R.style.AppTheme_Dark
                AMOLED -> R.style.AppTheme_Amoled
                else -> if (isSystemNight) R.style.AppTheme_Dark else R.style.AppTheme
            }
        )
    }
}
