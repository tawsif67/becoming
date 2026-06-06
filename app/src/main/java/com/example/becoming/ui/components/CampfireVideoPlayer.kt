package com.example.becoming.ui.components

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CampfireVideoPlayer(modifier: Modifier = Modifier, streakCount: Int) {
    val context = LocalContext.current
    val day = streakCount.coerceIn(1, 30)
    val videoName = "day$day"
    val resId = context.resources.getIdentifier(videoName, "raw", context.packageName)
    
    if (resId != 0) {
        val videoUri = Uri.parse("android.resource://${context.packageName}/$resId")
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(videoUri)
                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                        // Basic green removal via setAlpha if needed, 
                        // but since these are "vector videos" they might have alpha or 
                        // we can try a simple ColorFilter approach in Compose.
                        // Standard VideoView doesn't support transparency well, 
                        // so we might need a TextureView + custom shader for premium feel.
                        start()
                    }
                }
            },
            update = { view ->
                // Check if URI needs update if streakCount changes
            }
        )
    }
}
