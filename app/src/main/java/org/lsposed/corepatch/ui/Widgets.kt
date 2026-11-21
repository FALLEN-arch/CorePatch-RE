package org.lsposed.corepatch.ui

import android.content.Context
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import org.lsposed.corepatch.R
import org.lsposed.corepatch.UiTheme

/** Resolves the theme's primary text color. */
fun Context.textColorPrimary(): Int {
    val value = TypedValue()
    theme.resolveAttribute(android.R.attr.textColorPrimary, value, true)
    return value.data
}

/** Applies an alpha fraction to a color, preserving its RGB channels. */
fun Int.withAlpha(alphaFraction: Float): Int {
    val a = (255 * alphaFraction).toInt().coerceIn(0, 255)
    return (a shl 24) or (this and 0x00FFFFFF)
}

/** `── TITLE` rule rendered above each card. */
fun sectionHeader(context: Context, title: String, accent: Int): TextView = TextView(context).apply {
    typeface = UiTheme.mono(context, UiTheme.WEIGHT_MEDIUM)
    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
    letterSpacing = 0.1f
    setTextColor(accent)
    setPadding(20.dp, 24.dp, 20.dp, 8.dp)
    text = "── ${title.uppercase()}"
}

/** Rounded section card using the theme's cardColor. */
fun card(context: Context): LinearLayout = LinearLayout(context).apply {
    orientation = LinearLayout.VERTICAL
    background = context.getDrawable(R.drawable.card_bg)
}
