package com.example.next.features.detail.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ElevatedCard
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
import com.example.next.core.ui.components.AppTopBar
import com.example.next.core.ui.components.ErrorContent
import com.example.next.core.ui.components.LoadingContent
import com.example.next.features.detail.presentation.state.DetailEvent
import com.example.next.features.detail.presentation.state.DetailUiState
import com.example.next.features.home.domain.model.HomeItem
import com.example.next.ui.theme.NeXTTheme

@Composable
fun DetailScreen(
    uiState: DetailUiState,
    onEvent: (DetailEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = "Topic Detail",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingContent(message = "Loading topic details...")
                }
                uiState.error != null -> {
                    ErrorContent(
                        errorMessage = uiState.error,
                        onRetry = { onEvent(DetailEvent.Retry) }
                    )
                }
                uiState.item != null -> {
                    val item = uiState.item
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
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
                                IconButton(onClick = { onEvent(DetailEvent.ToggleFavorite) }) {
                                    Icon(
                                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (item.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Last updated: ${item.timestamp}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun DetailScreenPreview() {
    val sampleItem = HomeItem(
        id = "item-1",
        title = "Clean Architecture with Jetpack Compose",
        description = "Comprehensive overview of layer separation, MVI state flow, and repository pattern.",
        category = "Architecture",
        isFavorite = true,
        timestamp = "2026-09-30 10:00"
    )

    NeXTTheme {
        DetailScreen(
            uiState = DetailUiState(
                isLoading = false,
                item = sampleItem,
                error = null
            ),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}
