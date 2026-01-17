package com.frequentsee.tv.agent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.frequentsee.tv.agent.server.CastServer
import kotlinx.coroutines.delay

class PlayerActivity : ComponentActivity() {

    private var player: ExoPlayer? = null
    private var streamUrl: String? = null
    private var title: String? = null
    private var subtitle: String? = null

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == CastServer.ACTION_STOP_PLAYBACK) {
                Log.d(TAG, "Received stop broadcast")
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "PlayerActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get stream info from intent
        streamUrl = intent.getStringExtra("streamUrl")
        title = intent.getStringExtra("title")
        subtitle = intent.getStringExtra("subtitle")

        if (streamUrl == null) {
            Log.e(TAG, "No stream URL provided")
            finish()
            return
        }

        // Register stop broadcast receiver
        ContextCompat.registerReceiver(
            this,
            stopReceiver,
            IntentFilter(CastServer.ACTION_STOP_PLAYBACK),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build().apply {
            val mediaItem = MediaItem.fromUri(streamUrl!!)
            setMediaItem(mediaItem)
            playWhenReady = true
            prepare()

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_ENDED -> {
                            Log.d(TAG, "Playback ended")
                            finish()
                        }
                        Player.STATE_READY -> Log.d(TAG, "Player ready")
                        Player.STATE_BUFFERING -> Log.d(TAG, "Buffering...")
                        Player.STATE_IDLE -> Log.d(TAG, "Player idle")
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    Log.e(TAG, "Playback error: ${error.message}", error)
                }
            })
        }

        // Handle back button press using OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "Back button pressed, stopping playback")
                finish()
            }
        })

        setContent {
            PlayerScreen(
                player = player!!,
                title = title ?: "",
                subtitle = subtitle ?: ""
            )
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_ESCAPE -> {
                Log.d(TAG, "Escape key pressed, stopping playback")
                finish()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(stopReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
        player?.release()
        player = null
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerScreen(
    player: ExoPlayer,
    title: String,
    subtitle: String
) {
    var showOverlay by remember { mutableStateOf(true) }

    // Auto-hide overlay after 5 seconds
    LaunchedEffect(Unit) {
        delay(5000)
        showOverlay = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ExoPlayer View
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    this.player = player
                    useController = false // Hide default controls
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay UI
        AnimatedVisibility(
            visible = showOverlay,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(48.dp)
                ) {
                    if (title.isNotEmpty()) {
                        Text(
                            text = title,
                            fontSize = 32.sp,
                            color = Color.White
                        )
                    }
                    if (subtitle.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = subtitle,
                            fontSize = 24.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
