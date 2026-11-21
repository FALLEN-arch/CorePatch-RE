package org.lsposed.corepatch.data

/** Entries of the settings list: category headers and toggle rows. */
sealed interface ListItem {
    /** @param icon optional drawable resource shown before the title. */
    data class Header(val title: String, val icon: Int = 0) : ListItem
    data class Switch(val data: SwitchData) : ListItem
}
