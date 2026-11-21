package org.lsposed.corepatch.data

/** Entries of the settings list: category headers, toggle and choice rows. */
sealed interface ListItem {
    /** @param icon optional drawable resource shown before the title. */
    data class Header(val title: String, val icon: Int = 0) : ListItem
    data class Switch(val data: SwitchData) : ListItem

    /** Mutually exclusive selection row (e.g. theme picker). */
    data class Choice(
        val title: String,
        val description: String,
        val selected: Boolean,
        val onSelect: () -> Unit,
    ) : ListItem
}
