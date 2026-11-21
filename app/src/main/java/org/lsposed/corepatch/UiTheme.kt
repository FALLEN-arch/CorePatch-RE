package org.lsposed.corepatch

import android.app.Activity
import android.content.res.Configuration

/** Theme selection: always follows the system dark mode setting. */
object UiTheme {
    /** Must be called before the activity's setContentView. */
    fun apply(activity: Activity) {
        val isSystemNight = (activity.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        activity.setTheme(if (isSystemNight) R.style.AppTheme_Dark else R.style.AppTheme)
    }
}
