package com.example.next.features.home.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.next.core.ui.components.AppNavBar
import com.example.next.core.ui.components.AppTopBar
import com.example.next.core.ui.components.EmptyContent
import com.example.next.core.ui.components.ErrorContent
import com.example.next.core.ui.components.LoadingContent
import com.example.next.features.home.domain.model.HomeItem
import com.example.next.features.home.presentation.state.HomeEvent
import com.example.next.features.home.presentation.state.HomeUiState
import com.example.next.ui.theme.NeXTTheme

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    onItemClick: (String) -> Unit,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(title = "NeXT Dashboard")
        },
        bottomBar = {
            AppNavBar(
                currentRoute = "home",
                onNavigateToRoute = onNavigateToRoute
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.categories) { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { onEvent(HomeEvent.SelectCategory(category)) },
                        label = { Text(text = category) }
                    )
                }
            }

            // Main Content States
            when {
                uiState.isLoading && uiState.items.isEmpty() -> {
                    LoadingContent(message = "Fetching Architecture Topics...")
                }
                uiState.error != null && uiState.items.isEmpty() -> {
                    ErrorContent(
                        errorMessage = uiState.error,
                        onRetry = { onEvent(HomeEvent.Retry) }
                    )
                }
                uiState.items.isEmpty() -> {
                    EmptyContent(
                        title = "No Topics Available",
                        message = "No items match category '${uiState.selectedCategory}'."
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.items,
                            key = { it.id }
                        ) { item ->
                            HomeItemCard(
                                item = item,
                                onClick = { onItemClick(item.id) },
                                onFavoriteToggle = { onEvent(HomeEvent.ToggleFavorite(item.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeItemCard(
    item: HomeItem,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.category.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun HomeScreenPreview() {
    val sampleItems = listOf(
        HomeItem(
            id = "item-1",
            title = "Clean Architecture with Jetpack Compose",
            description = "Comprehensive overview of layer separation, MVI state flow, and repository pattern.",
            category = "Architecture",
            isFavorite = true,
            timestamp = "2026-09-30 10:00"
        ),
        HomeItem(
            id = "item-2",
            title = "Room Database Persistence & KSP Configuration",
            description = "Best practices for configuring Room compiler with KSP and type-safe DAOs.",
            category = "Persistence",
            isFavorite = false,
            timestamp = "2026-09-30 11:30"
        )
    )

    NeXTTheme {
        HomeScreen(
            uiState = HomeUiState(
                isLoading = false,
                items = sampleItems,
                categories = listOf("All", "Architecture", "Persistence", "State Management"),
                selectedCategory = "All"
            ),
            onEvent = {},
            onItemClick = {},
            onNavigateToRoute = {}
        )
    }
}

