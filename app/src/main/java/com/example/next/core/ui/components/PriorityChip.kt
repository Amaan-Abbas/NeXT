package com.example.next.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.next.features.tasks.domain.model.TaskPriority
import com.example.next.ui.theme.PriorityHighColor
import com.example.next.ui.theme.PriorityHighContainer
import com.example.next.ui.theme.PriorityHighDarkColor
import com.example.next.ui.theme.PriorityHighDarkContainer
import com.example.next.ui.theme.PriorityLowColor
import com.example.next.ui.theme.PriorityLowContainer
import com.example.next.ui.theme.PriorityLowDarkColor
import com.example.next.ui.theme.PriorityLowDarkContainer
import com.example.next.ui.theme.PriorityMediumColor
import com.example.next.ui.theme.PriorityMediumContainer
import com.example.next.ui.theme.PriorityMediumDarkColor
import com.example.next.ui.theme.PriorityMediumDarkContainer

@Composable
fun PriorityChip(
    priority: TaskPriority,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val (backgroundColor, textColor, icon) = when (priority) {
        TaskPriority.LOW -> Triple(
            if (isDarkTheme) PriorityLowDarkContainer else PriorityLowContainer,
            if (isDarkTheme) PriorityLowDarkColor else PriorityLowColor,
            Icons.Default.ArrowDownward
        )
        TaskPriority.MEDIUM -> Triple(
            if (isDarkTheme) PriorityMediumDarkContainer else PriorityMediumContainer,
            if (isDarkTheme) PriorityMediumDarkColor else PriorityMediumColor,
            Icons.Default.Remove
        )
        TaskPriority.HIGH -> Triple(
            if (isDarkTheme) PriorityHighDarkContainer else PriorityHighContainer,
            if (isDarkTheme) PriorityHighDarkColor else PriorityHighColor,
            Icons.Default.ArrowUpward
        )
    }

    val chipShape = MaterialTheme.shapes.small

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(chipShape)
            .background(backgroundColor)
            .border(BorderStroke(1.dp, textColor.copy(alpha = 0.25f)), chipShape)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "${priority.name} Priority",
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = priority.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
