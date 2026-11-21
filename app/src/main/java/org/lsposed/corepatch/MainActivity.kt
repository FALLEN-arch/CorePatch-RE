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
import android.text.style.ImageSpan
import android.text.style.TypefaceSpan
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.lsposed.corepatch.App.Companion.mService
import org.lsposed.corepatch.App.Companion.reloadListener
import org.lsposed.corepatch.data.ListItem
import org.lsposed.corepatch.data.SwitchData
import org.lsposed.corepatch.ui.CustomSwitchLayout
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
        val accent = themeColor(R.attr.accentColor)
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
                typeface = resources.getFont(R.font.montserrat_semibold)
                setTextSize(18f)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    bottomMargin = 8.dp
                }
            })
            addView(TextView(this@MainActivity).apply {
                setText(R.string.xposed_service_unavailable_summary)
                setTextColor(themeColor(R.attr.subTextColor))
                setTextSize(14f)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            })
        }
    }

    private fun loadPrefs() {
        val bypassDowngrade = SwitchData(
            getString(R.string.bypass_downgrade), getString(R.string.bypass_downgrade_summary), Config.BYPASS_DOWNGRADE
        )
        val bypassMinSdk = SwitchData(
            getString(R.string.bypass_min_sdk), getString(R.string.bypass_min_sdk_summary), Config.BYPASS_MIN_SDK
        )
        val bypassVerification = SwitchData(
            getString(R.string.bypass_verification), getString(R.string.bypass_verification_summary), Config.BYPASS_VERIFICATION
        )
        val bypassResourceArscRestrictions = SwitchData(
            getString(R.string.bypass_resource_arsc_restrictions),
            getString(R.string.bypass_resource_arsc_restrictions_summary),
            Config.BYPASS_RESOURCE_ARSC_RESTRICTIONS
        )
        val bypassDigest = SwitchData(
            getString(R.string.bypass_digest), getString(R.string.bypass_digest_summary), Config.BYPASS_DIGEST
        )
        val bypassExactSignatureMatch = SwitchData(
            getString(R.string.bypass_exact_signature_match), getString(R.string.bypass_exact_signature_match_summary), Config.BYPASS_EXACT_SIGNATURE_MATCH
        )
        val usePreviousSignatures = SwitchData(
            getString(R.string.use_previous_signatures), getString(R.string.use_previous_signatures_summary), Config.USE_PREVIOUS_SIGNATURES,
            if (isMiui()) {
                getString(R.string.miui_usepresig_warn) + "\n\n" + getString(R.string.use_previous_signatures_warning)
            } else {
                getString(R.string.use_previous_signatures_warning)
            }
        )
        val allowHiddenApisForSystemApps = SwitchData(
            getString(R.string.allow_hidden_apis_for_system_apps),
            getString(R.string.allow_hidden_apis_for_system_apps_summary),
            Config.ALLOW_HIDDEN_APIS_FOR_SYSTEM_APPS
        )
        val bypassSharedUser = SwitchData(
            getString(R.string.bypass_shared_user), getString(R.string.bypass_shared_user_summary), Config.BYPASS_SHARED_USER
        )
        val disableVerificationAgent = SwitchData(
            getString(R.string.disable_verification_agent), getString(R.string.disable_verification_agent_summary), Config.DISABLE_VERIFICATION_AGENT
        )
        val disableDeveloperVerification = SwitchData(
            getString(R.string.disable_developer_verification),
            getString(R.string.disable_developer_verification_summary),
            Config.DISABLE_DEVELOPER_VERIFICATION
        )
        val bypassBlock = SwitchData(
            getString(R.string.bypass_block), getString(R.string.bypass_block_summary), Config.BYPASS_BLOCK
        )

        val sections = listOf(
            getString(R.string.section_installation) to listOf(
                bypassDowngrade,
                bypassMinSdk,
                bypassResourceArscRestrictions,
                bypassSharedUser,
            ),
            getString(R.string.section_signature_checks) to listOf(
                bypassVerification,
                bypassDigest,
                bypassExactSignatureMatch,
                usePreviousSignatures,
            ),
            getString(R.string.section_verification_services) to listOf(
                disableVerificationAgent,
                disableDeveloperVerification,
                bypassBlock,
            ),
            getString(R.string.section_system_behavior) to listOf(
                allowHiddenApisForSystemApps,
            ),
        )

        // Section header icons, tinted at render time.
        val iconsByTitle = mapOf(
            R.string.section_installation to R.drawable.ic_sec_install,
            R.string.section_signature_checks to R.drawable.ic_sec_signature,
            R.string.section_verification_services to R.drawable.ic_sec_verification,
            R.string.section_system_behavior to R.drawable.ic_sec_system,
            R.string.section_appearance to R.drawable.ic_sec_theme,
        ).entries.associate { getString(it.key) to it.value }

        val currentTheme = UiTheme.get(this)
        val appearance = listOf(
            getString(R.string.theme_system) to UiTheme.SYSTEM,
            getString(R.string.theme_light) to UiTheme.LIGHT,
            getString(R.string.theme_dark) to UiTheme.DARK,
            getString(R.string.theme_amoled) to UiTheme.AMOLED,
        )

        val scroll = ScrollView(this).apply {
            fitsSystemWindows = true
        }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp, 16.dp, 16.dp, 24.dp)
        }
        scroll.addView(page)

        fun addSection(title: String, rows: List<ListItem>, badge: String? = null) {
            page.addView(sectionHeader(title, iconsByTitle[title] ?: 0, badge))
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = getDrawable(R.drawable.card_bg)
            }
            rows.forEach { card.addView(bindRow(it)) }
            page.addView(card)
        }

        sections.forEach { (header, switches) ->
            addSection(header, switches.map { ListItem.Switch(it) })
        }

        addSection(
            getString(R.string.section_appearance),
            appearance.map { (label, value) ->
                ListItem.Choice(
                    title = label,
                    description = "",
                    selected = currentTheme == value,
                    onSelect = {
                        UiTheme.set(this, value)
                        recreate()
                    },
                )
            },
            badge = "!",
        )

        setContentView(scroll)
    }

    /**
     * Accent-colored section header with icon, above its card.
     * [badge] prefixes the title (e.g. "!" marks the Appearance section
     * as separate from the module's core function).
     */
    private fun sectionHeader(title: String, icon: Int, badge: String? = null): TextView {
        val header = TextView(this).apply {
            typeface = resources.getFont(R.font.montserrat_semibold)
            setTextSize(14f)
            letterSpacing = 0.08f
            setTextColor(themeColor(R.attr.accentColor))
            setPadding(20.dp, 24.dp, 20.dp, 8.dp)
        }
        val prefix = badge?.let { "$it  " } ?: ""
        if (icon != 0) {
            val iconSize = 18.dp
            val drawable = getDrawable(icon)!!.apply {
                setTint(themeColor(R.attr.accentColor))
                setBounds(0, 0, iconSize, iconSize)
            }
            val text = SpannableString(prefix + "\u00a0 " + title).apply {
                // Bold the badge so the separator reads before the icon/title.
                if (badge != null) {
                    setSpan(
                        BrandTypefaceSpan(resources.getFont(R.font.montserrat_bold)),
                        0, badge.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                // The icon replaces the space right after the prefix.
                val iconStart = prefix.length
                setSpan(ImageSpan(drawable, ImageSpan.ALIGN_CENTER), iconStart, iconStart + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            header.text = text
        } else {
            header.text = prefix + title
        }
        return header
    }

    /** Builds a row view for a list item (toggle or theme choice). */
    private fun bindRow(item: ListItem): CustomSwitchLayout {
        val view = CustomSwitchLayout(this)
        when (item) {
            is ListItem.Switch -> {
                val data = item.data
                view.titleView.text = data.title
                view.subtitleView.text = data.description
                view.setRowDescription(data.title, data.description)
                view.setOnCheckListener {}
                view.switchView.isChecked = Config.getConfig(data.key)
                view.setOnCheckListener { isChecked ->
                    Config.setConfig(data.key, isChecked)
                    if (isChecked && data.warning != null) {
                        AlertDialog.Builder(this)
                            .setMessage(data.warning)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    }
                }
            }
            is ListItem.Choice -> {
                view.titleView.text = item.title
                view.subtitleView.text = item.description
                view.setRowDescription(item.title, item.description)
                view.setOnCheckListener {}
                view.switchView.isChecked = item.selected
                view.setOnCheckListener { isChecked ->
                    if (isChecked) {
                        item.onSelect()
                    } else {
                        // The active theme can't be switched off; snap back to checked.
                        view.switchView.setOnCheckedChangeListener(null)
                        view.switchView.isChecked = true
                        view.setOnCheckListener { again -> if (again) item.onSelect() }
                    }
                }
            }
            is ListItem.Header -> Unit // headers are rendered by addSection
        }
        return view
    }

    private fun styledTitle(): CharSequence {
        val base = getString(R.string.app_name)
        return SpannableString(base).apply {
            setSpan(
                BrandTypefaceSpan(resources.getFont(R.font.montserrat_bold)),
                0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(themeColor(R.attr.accentColor)),
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
