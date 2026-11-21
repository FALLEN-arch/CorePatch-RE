package org.lsposed.corepatch

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.lsposed.corepatch.App.Companion.mService
import org.lsposed.corepatch.App.Companion.reloadListener
import org.lsposed.corepatch.ui.UiPrefs
import org.lsposed.corepatch.ui.dp
import org.lsposed.corepatch.ui.homeScreen
import org.lsposed.corepatch.ui.patchesScreen
import org.lsposed.corepatch.ui.settingsScreen
import org.lsposed.corepatch.ui.themeColor
import java.util.Locale

class MainActivity : Activity() {

    companion object {
        private const val TAB_HOME = 0
        private const val TAB_PATCHES = 1
        private const val TAB_SETTINGS = 2

        // Survives recreate() so the user stays on the tab they were on
        // after changing an appearance setting.
        private var lastTab = TAB_HOME
    }

    private var contentLayout: FrameLayout? = null
    private var tabLabels: List<TextView> = emptyList()
    private var indicator: View? = null

    /** Applies the language chosen in Settings ▸ Appearance ▸ Language. */
    override fun attachBaseContext(base: Context) {
        val lang = UiPrefs.language(base)
        super.attachBaseContext(
            if (lang == UiPrefs.LANG_SYSTEM) base else {
                val config = Configuration(base.resources.configuration)
                config.setLocale(Locale.forLanguageTag(lang))
                base.createConfigurationContext(config)
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        UiTheme.apply(this)
        super.onCreate(savedInstanceState)
        reloadListener = {
            runOnUiThread {
                showContent()
            }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            fitsSystemWindows = true
        }
        root.addView(buildHeader())
        val content = FrameLayout(this)
        contentLayout = content
        root.addView(content, LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f))
        setContentView(root)
        // Position the tab indicator once the header has been laid out.
        tabLabels.getOrNull(lastTab)?.post { placeIndicator(lastTab, animate = false) }
        showContent()
    }

    /** Terminal header: `RR▌` left, tabs right, sliding underline below. */
    private fun buildHeader(): View {
        val accent = UiTheme.accent
        val subText = themeColor(R.attr.subTextColor)
        val container = FrameLayout(this)
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16.dp, 12.dp, 16.dp, 0)
        }
        row.addView(TextView(this).apply {
            text = styledTitle()
            setTextSize(20f)
        })
        row.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f))
        val tabs = arrayOf(R.string.nav_home, R.string.nav_patches, R.string.nav_settings)
        val labels = mutableListOf<TextView>()
        tabs.forEachIndexed { index, resId ->
            val (tab, label) = buildTab(getString(resId), index, accent, subText)
            row.addView(tab)
            labels.add(label)
        }
        tabLabels = labels
        val bar = View(this).apply {
            setBackgroundColor(accent)
        }
        indicator = bar
        container.addView(row, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        container.addView(bar, FrameLayout.LayoutParams(0, 2.dp, Gravity.BOTTOM))
        return container
    }

    /** Builds one tab; returns the clickable container and its label view. */
    private fun buildTab(label: String, index: Int, accent: Int, subText: Int): Pair<LinearLayout, TextView> {
        val active = lastTab == index
        val tab = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            minimumHeight = 44.dp
            layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                marginStart = 12.dp
            }
            contentDescription = label
            setOnClickListener {
                if (lastTab != index) selectTab(index)
            }
        }
        val labelView = TextView(this).apply {
            text = label
            typeface = UiTheme.mono(this@MainActivity, UiTheme.WEIGHT_MEDIUM)
            setTextSize(13f)
            letterSpacing = 0.05f
            setTextColor(if (active) accent else subText)
            setPadding(6.dp, 10.dp, 6.dp, 6.dp)
            layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        }
        tab.addView(labelView)
        return tab to labelView
    }

    private fun selectTab(index: Int) {
        lastTab = index
        animateTabColors(index)
        placeIndicator(index, animate = true)
        showContent(animate = true)
    }

    /** Slides the underline to the active tab's label, morphing its width. */
    private fun placeIndicator(index: Int, animate: Boolean) {
        val bar = indicator ?: return
        val label = tabLabels.getOrNull(index) ?: return
        if (label.width == 0) {
            // Not laid out yet — retry after the next layout pass.
            label.post { placeIndicator(index, animate) }
            return
        }
        val targetX = (label.parent as View).left + label.left
        val targetWidth = label.width
        if (animate) {
            val fromX = bar.translationX
            val fromWidth = bar.layoutParams.width
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 220L
                interpolator = DecelerateInterpolator()
                addUpdateListener { animator ->
                    val t = animator.animatedValue as Float
                    bar.translationX = fromX + (targetX - fromX) * t
                    bar.layoutParams.width =
                        (fromWidth + (targetWidth - fromWidth) * t).toInt().coerceAtLeast(1)
                    bar.requestLayout()
                }
                start()
            }
        } else {
            bar.translationX = targetX
            bar.layoutParams.width = targetWidth
        }
    }

    /** Cross-fades each tab label between accent and dimmed colors. */
    private fun animateTabColors(activeIndex: Int) {
        val accent = UiTheme.accent
        val subText = themeColor(R.attr.subTextColor)
        tabLabels.forEachIndexed { index, label ->
            ValueAnimator.ofObject(
                ArgbEvaluator(), label.currentTextColor,
                if (index == activeIndex) accent else subText
            ).apply {
                duration = 220L
                addUpdateListener { animator ->
                    label.setTextColor(animator.animatedValue as Int)
                }
                start()
            }
        }
    }

    private fun showContent(animate: Boolean = false) {
        val service = mService
        val available = service != null && "system" in service.scope
        val view = when (lastTab) {
            TAB_HOME -> homeScreen(this)
            TAB_PATCHES -> if (available) patchesScreen(this) else unavailableView()
            TAB_SETTINGS -> settingsScreen(this) { recreate() }
            else -> homeScreen(this)
        }
        val container = contentLayout ?: return
        if (!animate) {
            container.removeAllViews()
            container.addView(view, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
            return
        }
        // Cross-fade: the new screen rises in while the old one fades out.
        view.alpha = 0f
        view.translationY = 12 * resources.displayMetrics.density
        container.addView(view, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(220L)
            .setInterpolator(DecelerateInterpolator())
            .start()
        for (i in container.childCount - 2 downTo 0) {
            val child = container.getChildAt(i)
            child.animate()
                .alpha(0f)
                .setDuration(160L)
                .withEndAction { container.removeView(child) }
                .start()
        }
    }

    /** Guidance state shown when the Xposed service is not connected. */
    private fun unavailableView(): LinearLayout {
        val accent = UiTheme.accent
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32.dp, 16.dp, 32.dp, 16.dp)
            addView(ImageView(this@MainActivity).apply {
                setImageResource(R.drawable.ic_sec_verification)
                setColorFilter(accent)
                layoutParams = LinearLayout.LayoutParams(56.dp, 56.dp).apply {
                    bottomMargin = 24.dp
                    gravity = Gravity.CENTER_HORIZONTAL
                }
            })
            addView(TextView(this@MainActivity).apply {
                setText(R.string.xposed_service_unavailable)
                typeface = UiTheme.mono(this@MainActivity, UiTheme.WEIGHT_BOLD)
                setTextSize(16f)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    bottomMargin = 8.dp
                }
            })
            addView(TextView(this@MainActivity).apply {
                setText(R.string.xposed_service_unavailable_summary)
                setTextColor(themeColor(R.attr.subTextColor))
                typeface = UiTheme.mono(this@MainActivity, UiTheme.WEIGHT_REGULAR)
                setTextSize(13f)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            })
        }
    }

    private fun styledTitle(): CharSequence {
        // "RR▌" — the block is the terminal cursor.
        return SpannableString("RR▌").apply {
            setSpan(
                BrandTypefaceSpan(UiTheme.mono(this@MainActivity, UiTheme.WEIGHT_BOLD)),
                0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(UiTheme.accent),
                0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    /** Applies a bundled typeface to the text it spans (metric-affecting). */
    private class BrandTypefaceSpan(private val typeface: Typeface) : TypefaceSpan("") {
        override fun updateDrawState(ds: TextPaint) = apply(ds)
        override fun updateMeasureState(textPaint: TextPaint) = apply(textPaint)
        private fun apply(paint: TextPaint) {
            paint.typeface = typeface
        }
    }

    override fun onStop() {
        super.onStop()
        reloadListener = {}
    }
}
