package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StoryGradient

@Composable
fun UserAvatar(
    avatarUrl: String,
    username: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    hasStory: Boolean = false,
    storySeen: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var avatarModifier = modifier
        .size(size)
        .clip(CircleShape)

    if (onClick != null) {
        avatarModifier = avatarModifier.clickable { onClick() }
    }

    Box(contentAlignment = Alignment.Center) {
        if (hasStory) {
            val ringBrush = if (storySeen) {
                Brush.linearGradient(listOf(Color.LightGray, Color.Gray))
            } else {
                StoryGradient
            }
            Box(
                modifier = Modifier
                    .size(size + 6.dp)
                    .clip(CircleShape)
                    .background(ringBrush)
                    .padding(2.5.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
            )
        }

        // Fallback styled avatar icon or image
        if (avatarUrl.isNotBlank() && avatarUrl.startsWith("http")) {
            // Simulated web avatar or placeholder
            Box(
                modifier = avatarModifier.background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.4).sp
                )
            }
        } else {
            Box(
                modifier = avatarModifier.background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.45).sp
                )
            }
        }
    }
}
