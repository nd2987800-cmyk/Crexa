package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R

@Composable
fun SmartMediaImage(
    mediaUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val isLocalDrawableResource = remember(mediaUrl) {
        mediaUrl.contains("drawable/") || 
        mediaUrl.contains("img_sample_post") || 
        mediaUrl.contains("img_crexa_brand_logo") ||
        mediaUrl.contains("img_lumina_logo")
    }

    if (isLocalDrawableResource) {
        val resId = remember(mediaUrl) {
            when {
                mediaUrl.contains("img_sample_post_1") -> R.drawable.img_sample_post_1_1786177193097
                mediaUrl.contains("img_sample_post_2") -> R.drawable.img_sample_post_2_1786177203745
                mediaUrl.contains("img_sample_post_3") -> R.drawable.img_sample_post_3_1786177217178
                else -> R.drawable.img_crexa_brand_logo_1786179516858
            }
        }
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(if (mediaUrl.startsWith("content://") || mediaUrl.startsWith("file://")) Uri.parse(mediaUrl) else mediaUrl)
                .crossfade(true)
                .error(R.drawable.img_sample_post_1_1786177193097)
                .placeholder(R.drawable.img_sample_post_1_1786177193097)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}
