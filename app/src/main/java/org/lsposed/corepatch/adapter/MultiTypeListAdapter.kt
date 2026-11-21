package org.lsposed.corepatch.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import org.lsposed.corepatch.Config
import org.lsposed.corepatch.R
import org.lsposed.corepatch.data.ListItem
import org.lsposed.corepatch.data.SwitchData
import org.lsposed.corepatch.ui.CustomSwitchLayout
import org.lsposed.corepatch.ui.dp

class MultiTypeListAdapter(private val items: List<ListItem>) : BaseAdapter() {
    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): ListItem {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getViewTypeCount(): Int = 2

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ListItem.Header -> 0
        is ListItem.Switch -> 1
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val context = parent!!.context
        return when (val item = items[position]) {
            is ListItem.Header -> {
                (convertView as? TextView ?: createHeaderView(context)).apply {
                    text = if (item.icon != 0) iconSpanned(context, item.icon, item.title) else item.title
                }
            }
            is ListItem.Switch -> {
                val data = item.data
                val view = convertView as? CustomSwitchLayout ?: CustomSwitchLayout(context)
                view.titleView.text = data.title
                view.subtitleView.text = data.description

                view.setOnCheckListener {}
                view.switchView.isChecked = Config.getConfig(data.key)
                view.setOnCheckListener { isChecked ->
                    Config.setConfig(data.key, isChecked)
                    if (isChecked && data.warning != null) {
                        AlertDialog.Builder(context)
                            .setMessage(data.warning)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    }
                }
                view
            }
        }
    }

    private fun createHeaderView(context: Context): TextView = TextView(context).apply {
        typeface = Typeface.create(
            context.resources.getFont(R.font.montserrat_semibold), Typeface.NORMAL
        )
        setTextColor(context.getColor(R.color.title_accent))
        textSize = 14f
        letterSpacing = 0.08f
        setPadding(16.dp, 20.dp, 16.dp, 4.dp)
    }

    /** Builds "icon + title" with the icon tinted to the accent color. */
    private fun iconSpanned(context: Context, iconRes: Int, title: String): CharSequence {
        val iconSize = 18.dp
        val drawable = context.getDrawable(iconRes)!!.apply {
            setTint(context.getColor(R.color.title_accent))
            setBounds(0, 0, iconSize, iconSize)
        }
        return SpannableString("  ${title}").apply {
            setSpan(ImageSpan(drawable, ImageSpan.ALIGN_CENTER), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}
