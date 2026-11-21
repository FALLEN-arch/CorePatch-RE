package org.lsposed.corepatch.data

import android.annotation.SuppressLint
import android.content.Context
import org.lsposed.corepatch.Config
import org.lsposed.corepatch.R

/** The patch sections, shared by the home dashboard and the patches screen. */
fun patchSections(context: Context): List<Pair<String, List<SwitchData>>> = listOf(
    context.getString(R.string.section_installation) to listOf(
        SwitchData(context.getString(R.string.bypass_downgrade), context.getString(R.string.bypass_downgrade_summary), Config.BYPASS_DOWNGRADE),
        SwitchData(context.getString(R.string.bypass_min_sdk), context.getString(R.string.bypass_min_sdk_summary), Config.BYPASS_MIN_SDK),
        SwitchData(context.getString(R.string.bypass_resource_arsc_restrictions), context.getString(R.string.bypass_resource_arsc_restrictions_summary), Config.BYPASS_RESOURCE_ARSC_RESTRICTIONS),
        SwitchData(context.getString(R.string.bypass_shared_user), context.getString(R.string.bypass_shared_user_summary), Config.BYPASS_SHARED_USER),
    ),
    context.getString(R.string.section_signature_checks) to listOf(
        SwitchData(context.getString(R.string.bypass_verification), context.getString(R.string.bypass_verification_summary), Config.BYPASS_VERIFICATION),
        SwitchData(context.getString(R.string.bypass_digest), context.getString(R.string.bypass_digest_summary), Config.BYPASS_DIGEST),
        SwitchData(context.getString(R.string.bypass_exact_signature_match), context.getString(R.string.bypass_exact_signature_match_summary), Config.BYPASS_EXACT_SIGNATURE_MATCH),
        SwitchData(
            context.getString(R.string.use_previous_signatures),
            context.getString(R.string.use_previous_signatures_summary),
            Config.USE_PREVIOUS_SIGNATURES,
            if (isMiui()) {
                context.getString(R.string.miui_usepresig_warn) + "\n\n" + context.getString(R.string.use_previous_signatures_warning)
            } else {
                context.getString(R.string.use_previous_signatures_warning)
            }
        ),
    ),
    context.getString(R.string.section_verification_services) to listOf(
        SwitchData(context.getString(R.string.disable_verification_agent), context.getString(R.string.disable_verification_agent_summary), Config.DISABLE_VERIFICATION_AGENT),
        SwitchData(context.getString(R.string.disable_developer_verification), context.getString(R.string.disable_developer_verification_summary), Config.DISABLE_DEVELOPER_VERIFICATION),
        SwitchData(context.getString(R.string.bypass_block), context.getString(R.string.bypass_block_summary), Config.BYPASS_BLOCK),
    ),
    context.getString(R.string.section_system_behavior) to listOf(
        SwitchData(context.getString(R.string.allow_hidden_apis_for_system_apps), context.getString(R.string.allow_hidden_apis_for_system_apps_summary), Config.ALLOW_HIDDEN_APIS_FOR_SYSTEM_APPS),
    ),
)

@SuppressLint("PrivateApi")
private fun isMiui(): Boolean = try {
    val systemProperties = Class.forName("android.os.SystemProperties")
    val get = systemProperties.getMethod("get", String::class.java)
    (get.invoke(null, "ro.miui.ui.version.code") as String).isNotEmpty()
} catch (_: ReflectiveOperationException) {
    false
}
