package org.lsposed.corepatch

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.lsposed.corepatch.App.Companion.mService
import org.lsposed.corepatch.App.Companion.reloadListener
import org.lsposed.corepatch.data.SwitchData
import org.lsposed.corepatch.ui.TerminalRow
import org.lsposed.corepatch.ui.dp
import org.lsposed.corepatch.ui.themeColor

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        UiTheme.apply(this)
        super.onCreate(savedInstanceState)
        title = styledTitle()
        reloadListener = {
            runOnUiThread {
                showContent()
            }
        }
        showContent()
    }

    private fun showContent() {
        val service = mService
        if (service == null || "system" !in service.scope) {
            setContentView(unavailableView())
            return
        }
        loadPrefs()
    }

    /** Guidance state shown when the Xposed service is not connected. */
    private fun unavailableView(): LinearLayout {
        val accent = UiTheme.accent
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            fitsSystemWindows = true
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
                typeface = resources.getFont(R.font.jetbrainsmono_bold)
                setTextSize(16f)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    bottomMargin = 8.dp
                }
            })
            addView(TextView(this@MainActivity).apply {
                setText(R.string.xposed_service_unavailable_summary)
                setTextColor(themeColor(R.attr.subTextColor))
                typeface = resources.getFont(R.font.jetbrainsmono_regular)
                setTextSize(13f)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            })
        }
    }

    private fun loadPrefs() {
        val rows = listOf(
            getString(R.string.section_installation) to listOf(
                SwitchData(getString(R.string.bypass_downgrade), getString(R.string.bypass_downgrade_summary), Config.BYPASS_DOWNGRADE),
                SwitchData(getString(R.string.bypass_min_sdk), getString(R.string.bypass_min_sdk_summary), Config.BYPASS_MIN_SDK),
                SwitchData(getString(R.string.bypass_resource_arsc_restrictions), getString(R.string.bypass_resource_arsc_restrictions_summary), Config.BYPASS_RESOURCE_ARSC_RESTRICTIONS),
                SwitchData(getString(R.string.bypass_shared_user), getString(R.string.bypass_shared_user_summary), Config.BYPASS_SHARED_USER),
            ),
            getString(R.string.section_signature_checks) to listOf(
                SwitchData(getString(R.string.bypass_verification), getString(R.string.bypass_verification_summary), Config.BYPASS_VERIFICATION),
                SwitchData(getString(R.string.bypass_digest), getString(R.string.bypass_digest_summary), Config.BYPASS_DIGEST),
                SwitchData(getString(R.string.bypass_exact_signature_match), getString(R.string.bypass_exact_signature_match_summary), Config.BYPASS_EXACT_SIGNATURE_MATCH),
                SwitchData(
                    getString(R.string.use_previous_signatures), getString(R.string.use_previous_signatures_summary), Config.USE_PREVIOUS_SIGNATURES,
                    if (isMiui()) {
                        getString(R.string.miui_usepresig_warn) + "\n\n" + getString(R.string.use_previous_signatures_warning)
                    } else {
                        getString(R.string.use_previous_signatures_warning)
                    }
                ),
            ),
            getString(R.string.section_verification_services) to listOf(
                SwitchData(getString(R.string.disable_verification_agent), getString(R.string.disable_verification_agent_summary), Config.DISABLE_VERIFICATION_AGENT),
                SwitchData(getString(R.string.disable_developer_verification), getString(R.string.disable_developer_verification_summary), Config.DISABLE_DEVELOPER_VERIFICATION),
                SwitchData(getString(R.string.bypass_block), getString(R.string.bypass_block_summary), Config.BYPASS_BLOCK),
            ),
            getString(R.string.section_system_behavior) to listOf(
                SwitchData(getString(R.string.allow_hidden_apis_for_system_apps), getString(R.string.allow_hidden_apis_for_system_apps_summary), Config.ALLOW_HIDDEN_APIS_FOR_SYSTEM_APPS),
            ),
        )

        val accent = UiTheme.accent
        val subText = themeColor(R.attr.subTextColor)

        val scroll = ScrollView(this).apply {
            fitsSystemWindows = true
        }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp, 16.dp, 16.dp, 24.dp)
        }
        scroll.addView(page)

        fun addSection(title: String, section: List<SwitchData>) {
            page.addView(sectionHeader(title, accent))
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = getDrawable(R.drawable.card_bg)
            }
            section.forEach { data ->
                card.addView(TerminalRow(this).apply {
                    setRow(data.title, data.description, Config.getConfig(data.key), accent, subText) { isChecked ->
                        Config.setConfig(data.key, isChecked)
                        if (isChecked && data.warning != null) {
                            AlertDialog.Builder(this@MainActivity)
                                .setMessage(data.warning)
                                .setPositiveButton(android.R.string.ok, null)
                                .show()
                        }
                    }
                })
            }
            page.addView(card)
        }

        rows.forEach { (header, section) -> addSection(header, section) }

        setContentView(scroll)
    }

    /** ASCII section rule above each card: `── SIGNATURE CHECKS` */
    private fun sectionHeader(title: String, accent: Int): TextView {
        return TextView(this).apply {
            typeface = resources.getFont(R.font.jetbrainsmono_medium)
            setTextSize(12f)
            letterSpacing = 0.1f
            setTextColor(accent)
            setPadding(20.dp, 24.dp, 20.dp, 8.dp)
            text = "── ${title.uppercase()}"
        }
    }

    private fun styledTitle(): CharSequence {
        // "RR▌" — the block is the terminal cursor.
        return SpannableString("RR▌").apply {
            setSpan(
                BrandTypefaceSpan(resources.getFont(R.font.jetbrainsmono_bold)),
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

    @SuppressLint("PrivateApi")
    private fun isMiui(): Boolean = try {
        val systemProperties = Class.forName("android.os.SystemProperties")
        val get = systemProperties.getMethod("get", String::class.java)
        (get.invoke(null, "ro.miui.ui.version.code") as String).isNotEmpty()
    } catch (_: ReflectiveOperationException) {
        false
    }

    override fun onStop() {
        super.onStop()
        reloadListener = {}
    }
}
