package org.lsposed.corepatch.ui

import android.content.Context
import android.util.TypedValue
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Switch
import android.widget.TextView
import org.lsposed.corepatch.R

class CustomSwitchLayout(context: Context) : CustomViewGroup(context) {

    val titleView = TextView(context).apply {
        setTextAppearance(android.R.style.TextAppearance_Medium)
        typeface = context.resources.getFont(R.font.montserrat_medium)
        setTextSize(16f)
        layoutParams = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            topMargin = 12.dp
            leftMargin = 12.dp
            rightMargin = 12.dp
        }
        this@CustomSwitchLayout.addView(this)
    }
    val subtitleView = TextView(context).apply {
        setTextColor(context.themeColor(R.attr.subTextColor))
        setTextSize(13f)
        layoutParams = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            leftMargin = 12.dp
            bottomMargin = 12.dp
            rightMargin = 12.dp
        }
        this@CustomSwitchLayout.addView(this)
    }
    val switchView = Switch(context).apply {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        layoutParams = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            leftMargin = 8.dp
            rightMargin = 12.dp
        }
        this@CustomSwitchLayout.addView(this)
    }

    init {
        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        setBackgroundResource(outValue.resourceId)
        minimumHeight = 56.dp
        setOnClickListener { switchView.toggle() }
    }

    /** Merges title + subtitle into one TalkBack node on the row. */
    fun setRowDescription(title: String, subtitle: String?) {
        contentDescription = if (subtitle.isNullOrEmpty()) title else "$title. $subtitle"
    }

    fun setOnCheckListener(listener: (Boolean) -> Unit) {
        switchView.setOnCheckedChangeListener { _, isChecked ->
            listener(isChecked)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        switchView.measure(
            measuredWidth.toAtMostMeasureSpec(), measuredHeight.toAtMostMeasureSpec()
        )
        val titleWidth =
            measuredWidth - switchView.measuredWidth - switchView.marginStart - switchView.marginEnd - titleView.marginStart - titleView.marginEnd

        titleView.measure(
            titleWidth.toExactlyMeasureSpec(), titleView.defaultHeightMeasureSpec(this)
        )
        subtitleView.measure(
            titleWidth.toExactlyMeasureSpec(), subtitleView.defaultHeightMeasureSpec(this)
        )

        val totalHeight =
            (titleView.marginTop + titleView.measuredHeight + subtitleView.measuredHeight + subtitleView.marginBottom).coerceAtLeast(
                switchView.measuredHeight
            )

        setMeasuredDimension(measuredWidth, totalHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val titleY = if (subtitleView.text.isNullOrEmpty()) {
            (height / 2) - (titleView.measuredHeight / 2)
        } else {
            titleView.marginTop
        }
        titleView.autoLayout(titleView.marginStart, titleY)
        subtitleView.autoLayout(titleView.marginStart, titleView.bottom)
        switchView.autoLayout(
            this@CustomSwitchLayout.measuredWidth - switchView.marginStart - switchView.measuredWidth,
            (height / 2) - (switchView.measuredHeight / 2)
        )
    }

}
