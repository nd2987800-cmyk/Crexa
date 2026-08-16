package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.AutoAwesome
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
    onOpenCreate: () -> Unit,
    onOpenAiStudio: () -> Unit = {}
) {
    Column {
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
                    onClick = onOpenAiStudio,
                    modifier = Modifier.testTag("btn_header_aistudio")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Studio",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onOpenCreate,
                    modifier = Modifier.testTag("btn_header_create")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddBox,
                        contentDescription = "Create Post",
                        tint = Color(0xFF0F172A)
                    )
                }
                IconButton(
                    onClick = onOpenSearch,
                    modifier = Modifier.testTag("btn_header_search")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF0F172A)
                    )
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
                containerColor = Color.White
            )
        )
        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
    }
}

@Composable
fun LuminaHeaderBar(
    onOpenSearch: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenCreate: () -> Unit,
    onOpenAiStudio: () -> Unit = {}
) = CrexaHeaderBar(onOpenSearch, onOpenMessages, onOpenCreate, onOpenAiStudio)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color(0xFF0F172A)
                )
            },
            navigationIcon = {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("btn_topbar_back")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )
        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
    }
}
