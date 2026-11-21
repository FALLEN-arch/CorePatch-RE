package org.lsposed.corepatch.data

/** Entries of the settings list: category headers and toggle rows. */
sealed interface ListItem {
    data class Header(val title: String) : ListItem
    data class Switch(val data: SwitchData) : ListItem
}
