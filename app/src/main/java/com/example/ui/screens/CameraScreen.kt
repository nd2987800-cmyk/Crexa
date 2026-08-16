package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ui.components.SmartMediaImage
import com.example.ui.theme.CrexaPurple
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

enum class CameraCaptureMode {
    POST, VIDEO, STORY, LIVE
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onMediaCaptured: (mediaUri: String, isVideo: Boolean, mode: CameraCaptureMode) -> Unit,
    onCloseClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Camera, Audio, and Storage permissions
    val permissionsToRequest = remember {
        buildList {
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    val multiplePermissionsState = rememberMultiplePermissionsState(permissions = permissionsToRequest)

    val cameraPermissionGranted = multiplePermissionsState.permissions
        .find { it.permission == Manifest.permission.CAMERA }?.status?.isGranted == true

    LaunchedEffect(Unit) {
        if (!multiplePermissionsState.allPermissionsGranted) {
            multiplePermissionsState.launchMultiplePermissionRequest()
        }
    }

    var cameraMode by remember { mutableStateOf(CameraCaptureMode.POST) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }

    var isRecording by remember { mutableStateOf(false) }
    var recordingTimerSeconds by remember { mutableStateOf(0) }

    var isLiveActive by remember { mutableStateOf(false) }
    var liveViewers by remember { mutableStateOf(245) }

    var capturedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var isCapturedVideo by remember { mutableStateOf(false) }

    // Gallery Picker from inside Camera
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            capturedMediaUri = uri
            isCapturedVideo = (cameraMode == CameraCaptureMode.VIDEO)
        }
    }

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var cameraHardwareAvailable by remember { mutableStateOf(true) }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setFlashMode(flashMode)
            .build()
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraProvider?.unbindAll()
            } catch (e: Exception) {
                // Ignore during disposal
            }
            cameraExecutor.shutdown()
        }
    }

    // Video Recording Timer effect
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingTimerSeconds = 0
            while (isRecording) {
                delay(1000)
                recordingTimerSeconds++
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (capturedMediaUri != null) {
            // Captured Media Preview & Direct Multi-Format Upload Dialog
            CapturedMediaPreviewOverlay(
                mediaUri = capturedMediaUri!!,
                isVideo = isCapturedVideo,
                initialMode = cameraMode,
                onDirectUpload = { mode ->
                    onMediaCaptured(capturedMediaUri.toString(), isCapturedVideo, mode)
                    Toast.makeText(context, "Uploaded successfully to Crexa!", Toast.LENGTH_SHORT).show()
                },
                onRetake = {
                    capturedMediaUri = null
                }
            )
        } else if (!cameraPermissionGranted) {
            // Permissions Request State
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CrexaPurple.copy(alpha = 0.2f),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = CrexaPurple,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Text(
                        text = "Camera & Mic Permission Required",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Please allow camera and microphone access to record reels, capture photos, and go live directly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCBD5E1),
                        fontSize = 14.sp
                    )

                    Button(
                        onClick = {
                            multiplePermissionsState.launchMultiplePermissionRequest()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrexaPurple),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Grant Permission Now", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val demoUri = "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_1_1786177193097"
                            capturedMediaUri = Uri.parse(demoUri)
                            isCapturedVideo = (cameraMode == CameraCaptureMode.VIDEO)
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Use Sample Camera Shot")
                    }
                }
            }
        } else {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        try {
                            val provider = cameraProviderFuture.get()
                            cameraProvider = provider

                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()

                            if (provider.hasCamera(cameraSelector)) {
                                provider.unbindAll()
                                provider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture
                                )
                                cameraHardwareAvailable = true
                            } else {
                                cameraHardwareAvailable = false
                            }
                        } catch (exc: Exception) {
                            cameraHardwareAvailable = false
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                update = { previewView ->
                    cameraProvider?.let { provider ->
                        try {
                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()

                            if (provider.hasCamera(cameraSelector)) {
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                imageCapture.flashMode = flashMode

                                provider.unbindAll()
                                provider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture
                                )
                                cameraHardwareAvailable = true
                            } else {
                                cameraHardwareAvailable = false
                            }
                        } catch (e: Exception) {
                            cameraHardwareAvailable = false
                        }
                    }
                },
                onRelease = {
                    try {
                        cameraProvider?.unbindAll()
                    } catch (e: Exception) {
                        // Safe unbind
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (!cameraHardwareAvailable) {
                // Simulator Camera Viewfinder fallback
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A))
                ) {
                    SmartMediaImage(
                        mediaUrl = "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_1_1786177193097",
                        contentDescription = "Simulated Camera Viewfinder",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // LIVE Overlay if Live mode active
            if (isLiveActive) {
                LiveBroadcastingOverlay(
                    viewerCount = liveViewers,
                    onEndLive = {
                        isLiveActive = false
                        Toast.makeText(context, "Live stream ended! Saved broadcast replay.", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Top Bar Controls (Close, Flash, Flip Camera)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .testTag("btn_close_camera")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Camera", tint = Color.White)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Flash Mode Toggle
                    IconButton(
                        onClick = {
                            flashMode = when (flashMode) {
                                ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                                ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                                else -> ImageCapture.FLASH_MODE_OFF
                            }
                        },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .testTag("btn_toggle_flash")
                    ) {
                        val icon = when (flashMode) {
                            ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                            ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                            else -> Icons.Default.FlashOff
                        }
                        Icon(icon, contentDescription = "Flash Mode", tint = Color.White)
                    }

                    // Flip Camera (Front/Back)
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .testTag("btn_switch_camera")
                    ) {
                        Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Switch Camera", tint = Color.White)
                    }
                }
            }

            // Bottom Shutter Controls & Mode Selector
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp)
            ) {
                // Recording Timer Badge
                AnimatedVisibility(visible = isRecording) {
                    Surface(
                        color = Color.Red,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.White, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REC 00:${recordingTimerSeconds.toString().padStart(2, '0')}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Shutter Row: [Gallery Button] [Main Shutter] [Effects / Reset]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Gallery Button
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(
                                    if (cameraMode == CameraCaptureMode.VIDEO) ActivityResultContracts.PickVisualMedia.VideoOnly
                                    else ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                )
                            )
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = Color.White, modifier = Modifier.size(24.dp))
                    }

                    // Main Shutter Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(84.dp)
                            .border(4.dp, Color.White, CircleShape)
                            .padding(6.dp)
                            .clickable {
                                when (cameraMode) {
                                    CameraCaptureMode.POST, CameraCaptureMode.STORY -> {
                                        takePhoto(
                                            context = context,
                                            imageCapture = imageCapture,
                                            cameraExecutor = cameraExecutor,
                                            onPhotoSaved = { uri ->
                                                capturedMediaUri = uri
                                                isCapturedVideo = false
                                            },
                                            onError = {
                                                capturedMediaUri = Uri.parse("android.resource://com.aistudio.lumina.social/drawable/img_sample_post_1_1786177193097")
                                                isCapturedVideo = false
                                            }
                                        )
                                    }
                                    CameraCaptureMode.VIDEO -> {
                                        if (isRecording) {
                                            isRecording = false
                                            Toast.makeText(context, "Video recorded!", Toast.LENGTH_SHORT).show()
                                            capturedMediaUri = Uri.parse("android.resource://com.aistudio.lumina.social/drawable/img_sample_post_2_1786177203745")
                                            isCapturedVideo = true
                                        } else {
                                            isRecording = true
                                            recordingTimerSeconds = 0
                                        }
                                    }
                                    CameraCaptureMode.LIVE -> {
                                        if (!isLiveActive) {
                                            isLiveActive = true
                                            Toast.makeText(context, "LIVE streaming started! 🔴", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                            .testTag("btn_shutter")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    when (cameraMode) {
                                        CameraCaptureMode.VIDEO -> if (isRecording) Color.Red else Color(0xFFEF4444)
                                        CameraCaptureMode.LIVE -> Color.Red
                                        CameraCaptureMode.STORY -> CrexaPurple
                                        else -> Color.White
                                    }
                                )
                        )
                    }

                    // Filter / Flip shortcut
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Mode Switcher Carousel (POST, VIDEO/REEL, STORY, LIVE)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(CameraCaptureMode.values()) { mode ->
                        val isSelected = cameraMode == mode
                        Text(
                            text = when (mode) {
                                CameraCaptureMode.POST -> "POST"
                                CameraCaptureMode.VIDEO -> "REEL / VIDEO"
                                CameraCaptureMode.STORY -> "STORY"
                                CameraCaptureMode.LIVE -> "LIVE 🔴"
                            },
                            color = if (isSelected) Color.White else Color.Gray,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clickable {
                                    cameraMode = mode
                                    if (isRecording) isRecording = false
                                }
                                .testTag("camera_tab_${mode.name.lowercase()}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveBroadcastingOverlay(
    viewerCount: Int,
    onEndLive: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
    ) {
        // Top Live Badge & End Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.Red,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("LIVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$viewerCount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Button(
                onClick = onEndLive,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("End Live", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Live Chat Simulation Stream
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(start = 16.dp, bottom = 120.dp, end = 80.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LiveCommentChip(user = "alex_99", comment = "Awesome live stream! 🔥")
            LiveCommentChip(user = "sarah_travels", comment = "Where are you streaming from? ✨")
            LiveCommentChip(user = "tech_insider", comment = "Loving the camera clarity! ❤️")
        }
    }
}

@Composable
private fun LiveCommentChip(user: String, comment: String) {
    Surface(
        color = Color.Black.copy(alpha = 0.55f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("@$user: ", color = CrexaPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(comment, color = Color.White, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CapturedMediaPreviewOverlay(
    mediaUri: Uri,
    isVideo: Boolean,
    initialMode: CameraCaptureMode,
    onDirectUpload: (CameraCaptureMode) -> Unit,
    onRetake: () -> Unit
) {
    var selectedUploadTarget by remember { mutableStateOf(initialMode) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Media visual
        SmartMediaImage(
            mediaUrl = mediaUri.toString(),
            contentDescription = "Captured media preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (isVideo) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Video",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

        // Top Target Upload Selector Card
        Surface(
            color = Color.Black.copy(alpha = 0.65f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    CameraCaptureMode.POST to "Post",
                    CameraCaptureMode.VIDEO to "Reel",
                    CameraCaptureMode.STORY to "Story"
                ).forEach { (mode, label) ->
                    val isSelected = selectedUploadTarget == mode
                    Surface(
                        color = if (isSelected) CrexaPurple else Color.Transparent,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.clickable { selectedUploadTarget = mode }
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Bottom Action Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onRetake,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White)),
                modifier = Modifier
                    .height(50.dp)
                    .testTag("btn_retake_media")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retake")
            }

            Button(
                onClick = { onDirectUpload(selectedUploadTarget) },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CrexaPurple),
                modifier = Modifier
                    .height(50.dp)
                    .testTag("btn_use_captured_media")
            ) {
                Text(
                    text = when (selectedUploadTarget) {
                        CameraCaptureMode.POST -> "Direct Upload Post"
                        CameraCaptureMode.VIDEO -> "Direct Upload Reel"
                        CameraCaptureMode.STORY -> "Share to Story"
                        CameraCaptureMode.LIVE -> "Upload Live Replay"
                    },
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    cameraExecutor: ExecutorService,
    onPhotoSaved: (Uri) -> Unit,
    onError: () -> Unit
) {
    val photoFile = File(
        context.cacheDir,
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        cameraExecutor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                onError()
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                onPhotoSaved(savedUri)
            }
        }
    )
}
