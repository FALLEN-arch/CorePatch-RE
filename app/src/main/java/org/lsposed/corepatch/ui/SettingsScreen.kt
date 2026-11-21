package org.lsposed.corepatch.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.lsposed.corepatch.R
import org.lsposed.corepatch.UiTheme

/**
 * Settings: appearance controls (theme, accent tone, font, language) and
 * project links. Every change persists immediately; [onChange] lets the
 * activity recreate so the new look applies at once.
 */
fun settingsScreen(activity: Activity, onChange: () -> Unit): View {
    val accent = UiTheme.accent
    val subText = activity.themeColor(R.attr.subTextColor)
    val primary = activity.textColorPrimary()

    fun chip(label: String, active: Boolean, onClick: () -> Unit): TextView =
        TextView(activity).apply {
            text = label
            typeface = UiTheme.mono(activity, UiTheme.WEIGHT_MEDIUM)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(if (active) accent else subText)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 10.dp.toFloat()
                setColor(Color.TRANSPARENT)
                setStroke(if (active) 1.dp else 0, if (active) accent else Color.TRANSPARENT)
            }
            setPadding(14.dp, 8.dp, 14.dp, 8.dp)
            layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                marginEnd = 8.dp
            }
            setOnClickListener { onClick() }
        }

    fun swatch(color: Int, label: String, active: Boolean, onClick: () -> Unit): View =
        View(activity).apply {
            contentDescription = label
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8.dp.toFloat()
                setColor(color)
                setStroke(if (active) 2.dp else 0, if (active) primary else Color.TRANSPARENT)
            }
            layoutParams = LinearLayout.LayoutParams(26.dp, 26.dp).apply {
                marginEnd = 12.dp
                gravity = Gravity.CENTER_VERTICAL
            }
            setOnClickListener { onClick() }
        }

    fun settingRow(label: String, content: View): LinearLayout = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(20.dp, 14.dp, 20.dp, 14.dp)
        minimumHeight = 52.dp
        addView(TextView(activity).apply {
            text = label
            typeface = UiTheme.mono(activity, UiTheme.WEIGHT_REGULAR)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(primary)
        }, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        addView(content, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            topMargin = 8.dp
        })
    }

    fun chipScroller(vararg chips: View): HorizontalScrollView = HorizontalScrollView(activity).apply {
        isHorizontalScrollBarEnabled = false
        addView(LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            chips.forEach { addView(it) }
        })
    }

    fun valueRow(label: String, value: String): LinearLayout = LinearLayout(activity).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        minimumHeight = 52.dp
        setPadding(20.dp, 14.dp, 20.dp, 14.dp)
        addView(TextView(activity).apply {
            text = label
            typeface = UiTheme.mono(activity, UiTheme.WEIGHT_REGULAR)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(primary)
        })
        addView(TextView(activity).apply {
            text = ".".repeat(64)
            maxLines = 1
            typeface = UiTheme.mono(activity, UiTheme.WEIGHT_REGULAR)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(subText.withAlpha(0.4f))
        }, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f).apply {
            marginStart = 8.dp
            marginEnd = 8.dp
        })
        addView(TextView(activity).apply {
            text = value
            typeface = UiTheme.mono(activity, UiTheme.WEIGHT_BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(subText)
        })
    }

    fun linkRow(title: String, subtitle: String, url: String): LinearLayout = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        minimumHeight = 52.dp
        setPadding(20.dp, 14.dp, 20.dp, 14.dp)
        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        setBackgroundResource(outValue.resourceId)
        val line = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        line.addView(TextView(activity).apply {
            text = title
            typeface = UiTheme.mono(activity, UiTheme.WEIGHT_REGULAR)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(primary)
        })
        line.addView(TextView(activity).apply {
            text = ".".repeat(64)
            maxLines = 1
            typeface = UiTheme.mono(activity, UiTheme.WEIGHT_REGULAR)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(subText.withAlpha(0.4f))
        }, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f).apply {
            marginStart = 8.dp
            marginEnd = 8.dp
        })
        line.addView(TextView(activity).apply {
            text = "→"
            typeface = UiTheme.mono(activity, UiTheme.WEIGHT_BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(accent)
        })
        addView(line)
        addView(TextView(activity).apply {
            text = subtitle
            typeface = UiTheme.mono(activity, UiTheme.WEIGHT_REGULAR)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(subText)
        })
        setOnClickListener {
            runCatching { activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
        }
    }

    val currentTheme = UiPrefs.theme(activity)
    val currentAccent = UiPrefs.accentIndex(activity)
    val currentFont = UiPrefs.font(activity)
    val currentLang = UiPrefs.language(activity)
    val accentLabels = arrayOf(
        R.string.accent_rr, R.string.accent_phosphor, R.string.accent_amber, R.string.accent_cyan,
    )

    val scroll = ScrollView(activity)
    val page = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(16.dp, 16.dp, 16.dp, 24.dp)
    }
    scroll.addView(page)

    page.addView(sectionHeader(activity, activity.getString(R.string.settings_section_appearance), accent))
    val appearanceCard = card(activity)

    appearanceCard.addView(settingRow(activity.getString(R.string.setting_theme), chipScroller(
        chip(activity.getString(R.string.theme_system), currentTheme == UiPrefs.THEME_SYSTEM) {
            UiPrefs.setTheme(activity, UiPrefs.THEME_SYSTEM); onChange()
        },
        chip(activity.getString(R.string.theme_light), currentTheme == UiPrefs.THEME_LIGHT) {
            UiPrefs.setTheme(activity, UiPrefs.THEME_LIGHT); onChange()
        },
        chip(activity.getString(R.string.theme_dark), currentTheme == UiPrefs.THEME_DARK) {
            UiPrefs.setTheme(activity, UiPrefs.THEME_DARK); onChange()
        },
    )))
    appearanceCard.addView(settingRow(activity.getString(R.string.setting_accent), chipScroller(
        *UiTheme.ACCENTS.mapIndexed { index, tone ->
            swatch(if (UiTheme.isDark) tone.dark else tone.light, activity.getString(accentLabels[index]), index == currentAccent) {
                UiPrefs.setAccentIndex(activity, index); onChange()
            }
        }.toTypedArray()
    )))
    appearanceCard.addView(settingRow(activity.getString(R.string.setting_font), chipScroller(
        chip(activity.getString(R.string.font_jetbrains), currentFont == UiPrefs.FONT_JETBRAINS) {
            UiPrefs.setFont(activity, UiPrefs.FONT_JETBRAINS); onChange()
        },
        chip(activity.getString(R.string.font_ibmplex), currentFont == UiPrefs.FONT_IBM_PLEX) {
            UiPrefs.setFont(activity, UiPrefs.FONT_IBM_PLEX); onChange()
        },
    )))
    appearanceCard.addView(settingRow(activity.getString(R.string.setting_language), chipScroller(
        chip(activity.getString(R.string.language_system), currentLang == UiPrefs.LANG_SYSTEM) {
            UiPrefs.setLanguage(activity, UiPrefs.LANG_SYSTEM); onChange()
        },
        chip(activity.getString(R.string.language_en), currentLang == "en") {
            UiPrefs.setLanguage(activity, "en"); onChange()
        },
        chip(activity.getString(R.string.language_es), currentLang == "es") {
            UiPrefs.setLanguage(activity, "es"); onChange()
        },
        chip(activity.getString(R.string.language_ru), currentLang == "ru") {
            UiPrefs.setLanguage(activity, "ru"); onChange()
        },
    )))
    page.addView(appearanceCard)

    page.addView(sectionHeader(activity, activity.getString(R.string.settings_section_about), accent))
    val aboutCard = card(activity)
    val version = try {
        activity.packageManager.getPackageInfo(activity.packageName, 0).versionName ?: "?"
    } catch (_: Exception) {
        "?"
    }
    aboutCard.addView(valueRow(activity.getString(R.string.about_version), version))
    aboutCard.addView(linkRow(activity.getString(R.string.about_repo), "github.com/FALLEN-arch/RR", "https://github.com/FALLEN-arch/RR"))
    aboutCard.addView(linkRow(activity.getString(R.string.about_upstream), "github.com/LSPosed/CorePatch", "https://github.com/LSPosed/CorePatch"))
    page.addView(aboutCard)
    return scroll
}
