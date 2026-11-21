package org.lsposed.corepatch.ui

import android.app.Activity
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.lsposed.corepatch.App.Companion.mService
import org.lsposed.corepatch.Config
import org.lsposed.corepatch.R
import org.lsposed.corepatch.UiTheme
import org.lsposed.corepatch.data.patchSections

/** Home dashboard: framework status and which patches are active. */
fun homeScreen(activity: Activity): View {
    val accent = UiTheme.accent
    val subText = activity.themeColor(R.attr.subTextColor)
    val primary = activity.textColorPrimary()

    fun statusRow(label: String, value: String, valueColor: Int): LinearLayout =
        LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = 52.dp
            setPadding(20.dp, 14.dp, 20.dp, 14.dp)
            contentDescription = "$label: $value"
            addView(TextView(activity).apply {
                text = label
                typeface = UiTheme.mono(activity, UiTheme.WEIGHT_REGULAR)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(primary)
            })
            addView(TextView(activity).apply {
                text = ".".repeat(64)
                maxLines = 1
                typeface = UiTheme.mono(activity, UiTheme.WEIGHT_REGULAR)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(subText.withAlpha(0.4f))
            }, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f).apply {
                marginStart = 8.dp
                marginEnd = 8.dp
            })
            addView(TextView(activity).apply {
                text = value
                typeface = UiTheme.mono(activity, UiTheme.WEIGHT_BOLD)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(valueColor)
            })
        }

    fun noteRow(label: String, color: Int): TextView = TextView(activity).apply {
        text = label
        typeface = UiTheme.mono(activity, UiTheme.WEIGHT_REGULAR)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        setTextColor(color)
        setPadding(20.dp, 14.dp, 20.dp, 14.dp)
    }

    val service = mService
    val bound = service != null
    val scoped = service != null && "system" in service.scope
    val sections = patchSections(activity)
    val all = sections.flatMap { it.second }
    val enabled = all.filter { Config.getConfig(it.key) }

    val scroll = ScrollView(activity)
    val page = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(16.dp, 16.dp, 16.dp, 24.dp)
    }
    scroll.addView(page)

    page.addView(sectionHeader(activity, activity.getString(R.string.home_section_status), accent))
    val statusCard = card(activity)
    statusCard.addView(statusRow(
        activity.getString(R.string.status_service),
        "[${activity.getString(R.string.status_ok)}]",
        if (bound) accent else activity.textColorPrimary(),
    ))
    statusCard.addView(statusRow(
        activity.getString(R.string.status_scope),
        if (scoped) "[${activity.getString(R.string.status_ok)}]" else "[${activity.getString(R.string.status_fail)}]",
        if (scoped) accent else activity.textColorPrimary(),
    ))
    statusCard.addView(statusRow(
        activity.getString(R.string.status_active),
        "[${enabled.size}/${all.size}]",
        if (enabled.isNotEmpty()) accent else subText,
    ))
    page.addView(statusCard)

    page.addView(sectionHeader(activity, activity.getString(R.string.home_section_active), accent))
    val activeCard = card(activity)
    if (enabled.isEmpty()) {
        activeCard.addView(noteRow(activity.getString(R.string.home_none_active), subText))
    } else {
        enabled.forEach { data ->
            activeCard.addView(noteRow("» ${data.title}", primary))
        }
    }
    page.addView(activeCard)
    return scroll
}
