package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.StoryGradient
import com.example.ui.theme.UserThemeManager

@Composable
fun UserAvatar(
    avatarUrl: String,
    username: String,
    modifier: Modifier = Modifier,
    userId: String = "",
    size: Dp = 40.dp,
    showRing: Boolean = true,
    hasStory: Boolean = false,
    storySeen: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val identity = remember(userId, username) {
        UserThemeManager.getColorForUser(if (userId.isNotBlank()) userId else username, username)
    }

    val ringThickness = if (hasStory) 2.5.dp else if (showRing) 2.dp else 0.dp
    val ringGap = if (hasStory || showRing) 2.dp else 0.dp
    val totalSize = size + ((ringThickness + ringGap) * 2)

    val ringBrush = when {
        hasStory && storySeen -> Brush.linearGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))
        hasStory && !storySeen -> StoryGradient
        showRing -> identity.ringGradient
        else -> null
    }

    var avatarModifier = modifier
        .size(size)
        .clip(CircleShape)

    if (onClick != null) {
        avatarModifier = avatarModifier.clickable { onClick() }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = if (onClick != null && (showRing || hasStory)) {
            Modifier.clickable { onClick() }
        } else Modifier
    ) {
        // Distinct Colorful Identity Ring
        if (ringBrush != null) {
            Box(
                modifier = Modifier
                    .size(totalSize)
                    .clip(CircleShape)
                    .background(ringBrush)
                    .padding(ringThickness)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(ringGap)
            )
        }

        // Inner Avatar Image or Initials styled with user's distinct identity
        val drawableId = remember(avatarUrl) {
            when {
                avatarUrl.contains("img_sample_post_1") -> R.drawable.img_sample_post_1_1786177193097
                avatarUrl.contains("img_sample_post_2") -> R.drawable.img_sample_post_2_1786177203745
                avatarUrl.contains("img_sample_post_3") -> R.drawable.img_sample_post_3_1786177217178
                avatarUrl.contains("img_crexa_brand_logo") || avatarUrl.contains("drawable/img_crexa_brand_logo") -> R.drawable.img_crexa_brand_logo_1786179516858
                else -> null
            }
        }

        if (drawableId != null) {
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = username,
                contentScale = ContentScale.Crop,
                modifier = avatarModifier
            )
        } else {
            // Elegant gradient avatar with initials from user's distinct identity
            Box(
                modifier = avatarModifier.background(
                    Brush.linearGradient(listOf(identity.primary, identity.secondary))
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (username.isNotBlank()) username.take(1).uppercase() else "U",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.44).sp
                )
            }
        }
    }
}

