package com.example.next.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val defaultNavItems = listOf(
    NavItem("tasks", "Tasks", Icons.Filled.Checklist, Icons.Outlined.Checklist),
    NavItem("pomodoro", "Pomodoro", Icons.Filled.Timer, Icons.Outlined.Timer),
    NavItem("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
fun AppNavBar(
    currentRoute: String,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<NavItem> = defaultNavItems
) {
    NavigationBar(modifier = modifier) {
        items.forEach { item ->
            val selected = currentRoute == item.route || currentRoute.startsWith("${item.route}?")
            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) onNavigateToRoute(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(text = item.label) }
            )
        }
    }
}
