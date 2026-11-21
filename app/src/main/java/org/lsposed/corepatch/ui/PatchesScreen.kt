package org.lsposed.corepatch.ui

import android.app.Activity
import android.app.AlertDialog
import android.view.View
import android.widget.LinearLayout
import org.lsposed.corepatch.Config
import org.lsposed.corepatch.R
import org.lsposed.corepatch.UiTheme
import org.lsposed.corepatch.data.patchSections

/** The patches: grouped toggles written to the Xposed remote preferences. */
fun patchesScreen(activity: Activity): View {
    val accent = UiTheme.accent
    val subText = activity.themeColor(R.attr.subTextColor)

    val scroll = pageScroll(activity)
    val page = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(16.dp, 16.dp, 16.dp, 24.dp)
    }
    scroll.addView(page)

    patchSections(activity).forEach { (header, section) ->
        page.addView(sectionHeader(activity, header, accent))
        val cardView = card(activity)
        section.forEach { data ->
            cardView.addView(TerminalRow(activity).apply {
                setRow(data.title, data.description, Config.getConfig(data.key), accent, subText) { isChecked ->
                    Config.setConfig(data.key, isChecked)
                    if (isChecked && data.warning != null) {
                        AlertDialog.Builder(activity)
                            .setMessage(data.warning)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    }
                }
            })
        }
        page.addView(cardView)
    }
    return scroll
}
