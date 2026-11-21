package org.lsposed.corepatch.ui

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import org.lsposed.corepatch.UiTheme

/**
 * A terminal-style toggle row: `» key ······ [ON]`, with the description
 * underneath. The whole row is clickable; state is spoken by TalkBack via
 * the merged content description.
 */
class TerminalRow(context: Context) : LinearLayout(context) {

    val keyView = TextView(context).apply {
        typeface = UiTheme.mono(context, UiTheme.WEIGHT_REGULAR)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        setTextColor(context.textColorPrimary())
        gravity = Gravity.CENTER_VERTICAL
    }

    private val leaderView = TextView(context).apply {
        typeface = UiTheme.mono(context, UiTheme.WEIGHT_REGULAR)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        text = ".".repeat(64)
        maxLines = 1
    }

    private val statusView = TextView(context).apply {
        typeface = UiTheme.mono(context, UiTheme.WEIGHT_BOLD)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        gravity = Gravity.CENTER_VERTICAL
    }

    private val subtitleView = TextView(context).apply {
        typeface = UiTheme.mono(context, UiTheme.WEIGHT_REGULAR)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
    }

    private var checked = false

    init {
        orientation = VERTICAL
        background = pressGlow(context)
        minimumHeight = 52.dp
        setPadding(20.dp, 14.dp, 20.dp, 14.dp)

        val line = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        line.addView(keyView)
        line.addView(leaderView, LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
            marginStart = 8.dp
            marginEnd = 8.dp
        })
        line.addView(statusView)
        addView(line)
        addView(subtitleView)
    }

    /** Binds the row to a toggle: state first, then change listener. */
    fun setRow(
        title: String,
        subtitle: String?,
        initial: Boolean,
        accent: Int,
        subText: Int,
        onToggle: (Boolean) -> Unit,
    ) {
        this.checked = initial
        keyView.text = title
        subtitleView.setTextColor(subText)
        leaderView.setTextColor(subText.withAlpha(0.4f))
        subtitleView.text = subtitle
        statusView.setTextColor(if (initial) accent else subText)

        setOnClickListener {
            checked = !checked
            statusView.text = if (checked) "[ON]" else "[OFF]"
            statusView.setTextColor(if (checked) accent else subText)
            updateDescription()
            onToggle(checked)
        }
        updateDescription()
        // Set the initial status text last so the listener-driven path
        // and initial render can't disagree.
        statusView.text = if (checked) "[ON]" else "[OFF]"
    }

    private fun updateDescription() {
        val title = keyView.text.toString()
        val desc = buildString {
            append(title)
            if (!subtitleView.text.isNullOrEmpty()) append(". ").append(subtitleView.text)
            append(if (checked) ". On" else ". Off")
        }
        contentDescription = desc
    }
}
