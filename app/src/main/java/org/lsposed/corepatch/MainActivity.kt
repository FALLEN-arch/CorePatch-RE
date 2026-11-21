package org.lsposed.corepatch

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import org.lsposed.corepatch.App.Companion.mService
import org.lsposed.corepatch.App.Companion.reloadListener
import org.lsposed.corepatch.adapter.MultiTypeListAdapter
import org.lsposed.corepatch.data.ListItem
import org.lsposed.corepatch.data.SwitchData
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
            setContentView(LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                fitsSystemWindows = true
                addView(TextView(this@MainActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                    setText(R.string.xposed_service_unavailable)
                    setTextAppearance(android.R.style.TextAppearance_Medium)
                    gravity = Gravity.CENTER
                })
                addView(TextView(this@MainActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                    setText(R.string.xposed_service_unavailable_summary)
                    gravity = Gravity.CENTER
                })
            })
            return
        }
        loadPrefs()
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
        val icons = mapOf(
            R.string.section_installation to R.drawable.ic_sec_install,
            R.string.section_signature_checks to R.drawable.ic_sec_signature,
            R.string.section_verification_services to R.drawable.ic_sec_verification,
            R.string.section_system_behavior to R.drawable.ic_sec_system,
        )

        val appearance = listOf(
            getString(R.string.theme_system) to UiTheme.SYSTEM,
            getString(R.string.theme_light) to UiTheme.LIGHT,
            getString(R.string.theme_dark) to UiTheme.DARK,
            getString(R.string.theme_amoled) to UiTheme.AMOLED,
        )
        val currentTheme = UiTheme.get(this)
        val themeSection = listOf(
            ListItem.Header(getString(R.string.section_appearance), R.drawable.ic_sec_theme)
        ) + appearance.map { (label, value) ->
            ListItem.Choice(
                title = label,
                description = "",
                selected = currentTheme == value,
                onSelect = {
                    UiTheme.set(this, value)
                    recreate()
                },
            )
        }

        val items = sections.flatMap { (header, switches) ->
            val icon = icons.entries.firstOrNull { getString(it.key) == header }?.value ?: 0
            listOf(ListItem.Header(header, icon)) + switches.map { ListItem.Switch(it) }
        } + themeSection

        val adapter = MultiTypeListAdapter(items)

        val listView = ListView(this)
        listView.adapter = adapter
        listView.fitsSystemWindows = true
        setContentView(listView)
    }

    /**
     * Builds the action bar title: the app name set in Space Grotesk Bold
     * and fully colored in the icon's periwinkle accent.
     */
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
