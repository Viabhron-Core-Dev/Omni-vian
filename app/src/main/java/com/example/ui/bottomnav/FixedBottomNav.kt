package com.example.ui.bottomnav

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

enum class AppTab {
    CHAT, CODE
}

@Composable
fun FixedBottomNav(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    onMoreClick: () -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(32.dp))
    ) {
        NavigationBarItem(
            selected = currentTab == AppTab.CHAT,
            onClick = { onTabSelected(AppTab.CHAT) },
            icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
            label = { Text("Chat") }
        )
        NavigationBarItem(
            selected = currentTab == AppTab.CODE,
            onClick = { onTabSelected(AppTab.CODE) },
            icon = { Icon(Icons.Default.Code, contentDescription = "Code") },
            label = { Text("Code") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onMoreClick,
            icon = { Icon(Icons.Default.MoreVert, contentDescription = "More") },
            label = { Text("More") }
        )
    }
}
