package org.lsposed.corepatch.data

/** Entries of the settings list: category headers and toggle rows. */
sealed interface ListItem {
    /** @param icon optional Nerd Font glyph shown before the title. */
    data class Header(val title: String, val icon: String = "") : ListItem
    data class Switch(val data: SwitchData) : ListItem
}
