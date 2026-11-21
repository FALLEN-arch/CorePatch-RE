package org.lsposed.corepatch.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.ScrollView
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

/** Terminal press feedback: a rounded accent tint that fades in quickly on
 *  press and out slowly on release — no Material ripple bloom ("smoke"). */
class PressGlowDrawable(accent: Int, cornerRadiusPx: Float) : GradientDrawable() {
    private var animator: ValueAnimator? = null

    init {
        setColor(accent)
        cornerRadius = cornerRadiusPx
        alpha = 0
    }

    override fun isStateful() = true

    override fun onStateChange(state: IntArray): Boolean {
        val target = if (state.contains(android.R.attr.state_pressed)) 38 else 0
        animator?.cancel()
        animator = ValueAnimator.ofInt(alpha, target).apply {
            duration = if (target > 0) 90L else 180L
            interpolator = DecelerateInterpolator()
            addUpdateListener { value ->
                alpha = value.animatedValue as Int
                invalidateSelf()
            }
            start()
        }
        return true
    }
}

/** Press feedback for clickable rows. */
fun pressGlow(context: Context) = PressGlowDrawable(UiTheme.accent, 12.dp.toFloat())

/** Page scroll container: content can glide past the bottom edge and the
 *  fling decays a little slower, so long pages feel lighter to scroll. */
fun pageScroll(context: Context): ScrollView = object : ScrollView(context) {
    override fun fling(vy: Int) {
        super.fling((vy * 1.2f).toInt())
    }
}.apply {
    clipToPadding = false
    setPadding(0, 0, 0, 24.dp)
}
