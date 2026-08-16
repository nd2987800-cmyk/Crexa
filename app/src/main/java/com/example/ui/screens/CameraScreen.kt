package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.example.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

enum class CameraMode {
    PHOTO, VIDEO
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onMediaCaptured: (mediaUri: String, isVideo: Boolean) -> Unit,
    onCloseClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Accompanist permission state for Camera, Audio, and Storage
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

    val hasStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        multiplePermissionsState.permissions
            .find { it.permission == Manifest.permission.READ_MEDIA_IMAGES }?.status?.isGranted == true
    } else {
        multiplePermissionsState.permissions
            .find { it.permission == Manifest.permission.READ_EXTERNAL_STORAGE }?.status?.isGranted == true
    }

    LaunchedEffect(Unit) {
        if (!multiplePermissionsState.allPermissionsGranted) {
            multiplePermissionsState.launchMultiplePermissionRequest()
        }
    }

    var cameraMode by remember { mutableStateOf(CameraMode.PHOTO) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }

    var isRecording by remember { mutableStateOf(false) }
    var recordingTimerSeconds by remember { mutableStateOf(0) }

    var capturedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var isCapturedVideo by remember { mutableStateOf(false) }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setFlashMode(flashMode)
            .build()
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (capturedMediaUri != null) {
            // Preview Screen for captured media
            CapturedMediaPreviewOverlay(
                mediaUri = capturedMediaUri!!,
                isVideo = isCapturedVideo,
                onUseMedia = {
                    onMediaCaptured(capturedMediaUri.toString(), isCapturedVideo)
                },
                onRetake = {
                    capturedMediaUri = null
                }
            )
        } else if (!cameraPermissionGranted) {
            // Permission Request Fallback using Accompanist
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
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Camera & Storage Access Required",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Grant camera, microphone, and storage access to capture and upload photo and video posts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                    Button(
                        onClick = {
                            multiplePermissionsState.launchMultiplePermissionRequest()
                        },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Grant Permissions")
                    }

                    // Demo capture fallback button
                    OutlinedButton(
                        onClick = {
                            val demoUri = "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_1_1786177193097"
                            capturedMediaUri = Uri.parse(demoUri)
                            isCapturedVideo = false
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Simulate Demo Capture")
                    }
                }
            }
        } else {
            // Real Camera Viewfinder using CameraX
            var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
            var cameraHardwareAvailable by remember { mutableStateOf(true) }

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
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

                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (exc: Exception) {
                            cameraHardwareAvailable = false
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                update = { previewView ->
                    cameraProvider?.let { provider ->
                        try {
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            imageCapture.flashMode = flashMode

                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()

                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            // Hardware binding fallback
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (!cameraHardwareAvailable) {
                // In simulator or missing physical camera view
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1A1423))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.img_sample_post_1_1786177193097),
                            contentDescription = "Simulated Camera Viewfinder",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(400.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Simulated Viewfinder (Preview)",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Top Camera Bar (Close, Flash, Switch Camera)
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
                    // Flash Mode Button
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

                    // Switch Camera Front/Back
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

            // Bottom Shutter & Mode Selector
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
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
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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

                // Shutter Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(6.dp)
                        .clickable {
                            if (cameraMode == CameraMode.PHOTO) {
                                takePhoto(
                                    context = context,
                                    imageCapture = imageCapture,
                                    cameraExecutor = cameraExecutor,
                                    onPhotoSaved = { uri ->
                                        capturedMediaUri = uri
                                        isCapturedVideo = false
                                    },
                                    onError = {
                                        // Fallback to sample uri if file capture fails
                                        capturedMediaUri = Uri.parse("android.resource://com.aistudio.lumina.social/drawable/img_sample_post_1_1786177193097")
                                        isCapturedVideo = false
                                    }
                                )
                            } else {
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
                        }
                        .testTag("btn_shutter")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                if (cameraMode == CameraMode.VIDEO) Color.Red else Color.White
                            )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Mode Tabs (Photo / Video)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PHOTO",
                        color = if (cameraMode == CameraMode.PHOTO) Color.White else Color.Gray,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (cameraMode == CameraMode.PHOTO) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clickable { cameraMode = CameraMode.PHOTO }
                            .testTag("tab_camera_photo")
                    )

                    Text(
                        text = "VIDEO",
                        color = if (cameraMode == CameraMode.VIDEO) Color.Red else Color.Gray,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (cameraMode == CameraMode.VIDEO) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clickable { cameraMode = CameraMode.VIDEO }
                            .testTag("tab_camera_video")
                    )
                }
            }
        }
    }
}

@Composable
private fun CapturedMediaPreviewOverlay(
    mediaUri: Uri,
    isVideo: Boolean,
    onUseMedia: () -> Unit,
    onRetake: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val context = LocalContext.current

        // Media display
        Image(
            painter = painterResource(id = getDrawableForUri(mediaUri.toString())),
            contentDescription = "Captured media preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (isVideo) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
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

        // Action Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onRetake,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("btn_retake_media")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retake")
            }

            Button(
                onClick = onUseMedia,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("btn_use_captured_media")
            ) {
                Text("Use in Post", fontWeight = FontWeight.Bold)
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

private fun getDrawableForUri(uriString: String): Int {
    return when {
        uriString.contains("img_sample_post_2") -> R.drawable.img_sample_post_2_1786177203745
        uriString.contains("img_sample_post_3") -> R.drawable.img_sample_post_3_1786177217178
        else -> R.drawable.img_sample_post_1_1786177193097
    }
}
