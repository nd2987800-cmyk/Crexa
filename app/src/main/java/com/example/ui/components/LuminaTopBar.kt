package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CrexaMagenta
import com.example.ui.theme.CrexaPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrexaHeaderBar(
    onOpenSearch: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenCreate: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_crexa_brand_logo_1786179516858),
                    contentDescription = "Crexa Logo",
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = "Crexa",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            IconButton(
                onClick = onOpenCreate,
                modifier = Modifier.testTag("btn_header_create")
            ) {
                Icon(Icons.Default.AddBox, contentDescription = "Create Post")
            }
            IconButton(
                onClick = onOpenSearch,
                modifier = Modifier.testTag("btn_header_search")
            ) {
                Icon(Icons.Outlined.Search, contentDescription = "Search")
            }
            IconButton(
                onClick = onOpenMessages,
                modifier = Modifier.testTag("btn_header_messages")
            ) {
                BadgedBox(badge = { Badge { Text("1") } }) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Direct Messages",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun LuminaHeaderBar(
    onOpenSearch: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenCreate: () -> Unit
) = CrexaHeaderBar(onOpenSearch, onOpenMessages, onOpenCreate)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("btn_topbar_back")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
