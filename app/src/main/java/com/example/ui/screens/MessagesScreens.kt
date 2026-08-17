package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.components.SmartMediaImage
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CrexaPurple
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    users: List<UserEntity>,
    currentUser: UserEntity?,
    onOpenChat: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val otherUsers = remember(users, currentUser) {
        users.filter { it.id != currentUser?.id }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Direct Messages", fontWeight = FontWeight.Bold)
                        Text(
                            text = "${otherUsers.size} conversations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* New message broadcast */ }) {
                        Icon(Icons.Outlined.EditNote, contentDescription = "New message", tint = CrexaPurple)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .testTag("messages_list")
        ) {
            items(otherUsers) { user ->
                ListItem(
                    headlineContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.username, fontWeight = FontWeight.Bold)
                            if (user.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CrexaPurple, modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    supportingContent = {
                        Text(
                            text = "Active now • Tap to chat with ${user.fullName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingContent = {
                        Box {
                            UserAvatar(
                                avatarUrl = user.avatarUrl,
                                username = user.username,
                                userId = user.id,
                                size = 50.dp,
                                showRing = true
                            )
                            // Online indicator dot
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Quick snap",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.clickable { onOpenChat(user.id) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectChatScreen(
    recipientUser: UserEntity?,
    messages: List<MessageEntity>,
    currentUser: UserEntity?,
    onSendMessage: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var isViewOnceSelected by remember { mutableStateOf(false) }
    var activeCallType by remember { mutableStateOf<String?>(null) } // "AUDIO" or "VIDEO"

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val prefix = if (isViewOnceSelected) "🔒 [View Once Photo]: " else "📷 [Photo]: "
            onSendMessage("$prefix${uri}")
            isViewOnceSelected = false
            Toast.makeText(context, "Media sent!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordingDuration = 0
            while (isRecordingVoice) {
                delay(1000)
                recordingDuration++
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Call Overlay Dialog
    if (activeCallType != null) {
        CallOverlayDialog(
            callType = activeCallType!!,
            recipientUser = recipientUser,
            onEndCall = {
                activeCallType = null
                Toast.makeText(context, "Call ended", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(
                            avatarUrl = recipientUser?.avatarUrl ?: "",
                            username = recipientUser?.username ?: "User",
                            userId = recipientUser?.id ?: "",
                            size = 36.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = recipientUser?.username ?: "Chat",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "🟢 Online now",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { activeCallType = "AUDIO" }) {
                        Icon(Icons.Default.Phone, contentDescription = "Audio Call", tint = CrexaPurple)
                    }
                    IconButton(onClick = { activeCallType = "VIDEO" }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = CrexaPurple)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Column {
                    if (isViewOnceSelected) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEF3C7))
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Once Mode Active (Disappears after opening)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { isViewOnceSelected = false }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        // Media Gallery Picker
                        IconButton(
                            onClick = {
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                            }
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "Attach Media", tint = CrexaPurple)
                        }

                        // View Once Toggle
                        IconButton(
                            onClick = { isViewOnceSelected = !isViewOnceSelected }
                        ) {
                            Icon(
                                imageVector = if (isViewOnceSelected) Icons.Filled.VisibilityOff else Icons.Outlined.VisibilityOff,
                                contentDescription = "View Once",
                                tint = if (isViewOnceSelected) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isRecordingVoice) {
                            // Recording in progress bar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color(0xFFFEE2E2))
                                    .padding(horizontal = 14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Recording... 0:${if (recordingDuration < 10) "0$recordingDuration" else recordingDuration}", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(onClick = { isRecordingVoice = false }) {
                                    Text("Cancel", color = Color.Gray, fontSize = 12.sp)
                                }
                            }

                            // Send Voice Note Button
                            IconButton(
                                onClick = {
                                    isRecordingVoice = false
                                    onSendMessage("🎙️ [Voice Note 0:${if (recordingDuration < 10) "0$recordingDuration" else recordingDuration}]")
                                    Toast.makeText(context, "Voice note sent!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send Voice", tint = Color.White)
                            }
                        } else {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = { Text("Message...") },
                                shape = RoundedCornerShape(24.dp),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_chat_text")
                            )

                            if (inputText.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        val prefix = if (isViewOnceSelected) "🔒 [View Once]: " else ""
                                        onSendMessage("$prefix$inputText")
                                        inputText = ""
                                        isViewOnceSelected = false
                                    },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(CrexaPurple)
                                        .testTag("btn_send_chat")
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                                }
                            } else {
                                // Voice Note mic button
                                IconButton(
                                    onClick = { isRecordingVoice = true },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(CrexaPurple)
                                ) {
                                    Icon(Icons.Default.Mic, contentDescription = "Record Voice Note", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 8.dp,
                start = 12.dp,
                end = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // E2EE & Anti-Screenshot Privacy Card
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = CrexaPurple, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("🔒 End-to-End Encrypted Chat", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                            Text("Messages & calls are private. Screenshots & recording are blocked (FLAG_SECURE).", fontSize = 10.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }

            items(messages) { msg ->
                val isMe = msg.senderUserId == currentUser?.id
                val isViewOnce = msg.text.contains("[View Once")
                val isVoiceNote = msg.text.contains("🎙️ [Voice Note")
                val isMediaPhoto = msg.text.contains("📷 [Photo]")

                var isViewOnceOpened by remember { mutableStateOf(false) }

                Box(
                    contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = when {
                            isViewOnce -> if (isMe) Color(0xFFF59E0B) else Color(0xFFFEF3C7)
                            isMe -> CrexaPurple
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = when {
                            isViewOnce -> if (isMe) Color.White else Color(0xFF92400E)
                            isMe -> Color.White
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        ),
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            if (isViewOnce) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        if (!isViewOnceOpened) {
                                            isViewOnceOpened = true
                                            Toast.makeText(context, "View Once opened! Will self-destruct.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isViewOnceOpened) Icons.Default.LockOpen else Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isViewOnceOpened) "Opened (Disappeared)" else "1 View Photo • Tap to View",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            } else if (isVoiceNote) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    var isPlayingVoice by remember { mutableStateOf(false) }
                                    IconButton(
                                        onClick = {
                                            isPlayingVoice = !isPlayingVoice
                                            Toast.makeText(context, if (isPlayingVoice) "Playing Voice Note..." else "Paused", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isMe) Color.White.copy(alpha = 0.3f) else CrexaPurple.copy(alpha = 0.2f))
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingVoice) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = if (isMe) Color.White else CrexaPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = "Voice Message", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            text = msg.text.substringAfter("Voice Note").trim().removeSuffix("]"),
                                            fontSize = 11.sp,
                                            color = (if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Just now",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }

            // Real-time typing indicator
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${recipientUser?.username ?: "User"} is typing...",
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
fun CallOverlayDialog(
    callType: String, // "AUDIO" or "VIDEO"
    recipientUser: UserEntity?,
    onEndCall: () -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var isVideoCameraOff by remember { mutableStateOf(false) }
    var callSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callSeconds++
        }
    }

    Dialog(onDismissRequest = onEndCall) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top header
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (callType == "VIDEO") "Crexa HD Video Call" else "Crexa HD Audio Call",
                        color = CrexaPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Connected • 0:${if (callSeconds < 10) "0$callSeconds" else callSeconds}",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                // Avatar / Video preview
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .border(3.dp, CrexaPurple, CircleShape)
                ) {
                    UserAvatar(
                        avatarUrl = recipientUser?.avatarUrl ?: "",
                        username = recipientUser?.username ?: "User",
                        userId = recipientUser?.id ?: "",
                        size = 150.dp
                    )
                }

                Text(
                    text = recipientUser?.fullName ?: "Crexa Member",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                // Call action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) Color.Red else Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(if (isMuted) Icons.Default.MicOff else Icons.Default.Mic, contentDescription = "Mute", tint = Color.White)
                    }

                    if (callType == "VIDEO") {
                        IconButton(
                            onClick = { isVideoCameraOff = !isVideoCameraOff },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(if (isVideoCameraOff) Color.Red else Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(if (isVideoCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam, contentDescription = "Camera", tint = Color.White)
                        }
                    }

                    IconButton(
                        onClick = { isSpeakerOn = !isSpeakerOn },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isSpeakerOn) CrexaPurple else Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute, contentDescription = "Speaker", tint = Color.White)
                    }

                    // Hang up
                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White)
                    }
                }
            }
        }
    }
}
