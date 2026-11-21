package org.lsposed.corepatch

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
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
        showContent()
    }

    /** Terminal header: `RR▌` on the left, tab labels with underlines right. */
    private fun buildHeader(): View {
        val accent = UiTheme.accent
        val subText = themeColor(R.attr.subTextColor)
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16.dp, 12.dp, 16.dp, 0)
        }
        header.addView(TextView(this).apply {
            text = styledTitle()
            setTextSize(20f)
        })
        header.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f))
        val tabs = arrayOf(R.string.nav_home, R.string.nav_patches, R.string.nav_settings)
        tabs.forEachIndexed { index, resId ->
            header.addView(tabView(getString(resId), index, accent, subText))
        }
        return header
    }

    private fun tabView(label: String, index: Int, accent: Int, subText: Int): View {
        val active = lastTab == index
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            minimumHeight = 44.dp
            layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                marginStart = 12.dp
            }
            contentDescription = label
            setOnClickListener {
                if (lastTab != index) {
                    lastTab = index
                    showContent()
                }
            }
            addView(TextView(this@MainActivity).apply {
                text = label
                typeface = UiTheme.mono(this@MainActivity, UiTheme.WEIGHT_MEDIUM)
                setTextSize(13f)
                letterSpacing = 0.05f
                setTextColor(if (active) accent else subText)
                setPadding(6.dp, 10.dp, 6.dp, 4.dp)
            })
            addView(View(this@MainActivity).apply {
                setBackgroundColor(if (active) accent else Color.TRANSPARENT)
            }, LinearLayout.LayoutParams(MATCH_PARENT, 2.dp))
        }
    }

    private fun showContent() {
        val service = mService
        val available = service != null && "system" in service.scope
        val view = when (lastTab) {
            TAB_HOME -> homeScreen(this)
            TAB_PATCHES -> if (available) patchesScreen(this) else unavailableView()
            TAB_SETTINGS -> settingsScreen(this) { recreate() }
            else -> homeScreen(this)
        }
        contentLayout?.let { container ->
            container.removeAllViews()
            container.addView(view, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
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
