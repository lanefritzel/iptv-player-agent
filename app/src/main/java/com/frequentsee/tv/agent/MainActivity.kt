package com.frequentsee.tv.agent

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import java.net.Inet4Address
import java.net.NetworkInterface
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.frequentsee.tv.agent.ui.theme.IpTvPlayerAgentTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start the receiver service
        val serviceIntent = Intent(this, ReceiverService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        val ipAddress = getIpAddress()

        setContent {
            IpTvPlayerAgentTheme {
                ReceiverScreen(ipAddress = ipAddress)
            }
        }
    }

    private fun getIpAddress(): String? {
        return try {
            NetworkInterface.getNetworkInterfaces()?.asSequence()
                ?.filter { it.isUp && !it.isLoopback }
                ?.flatMap { it.inetAddresses.asSequence() }
                ?.filterIsInstance<Inet4Address>()
                ?.map { it.hostAddress }
                ?.firstOrNull { it != "127.0.0.1" }
        } catch (e: Exception) {
            null
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ReceiverScreen(ipAddress: String?) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "FrequentSee TV Agent",
                fontSize = 48.sp,
                color = Color.White
            )

            if (ipAddress != null) {
                Text(
                    text = "Ready to receive streams",
                    fontSize = 28.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Cast endpoint:",
                    fontSize = 24.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Text(
                    text = "http://$ipAddress:8080/cast",
                    fontSize = 32.sp,
                    color = Color(0xFF4CAF50)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Test button with a sample HLS stream
                Button(
                    onClick = {
                        val intent = Intent(context, PlayerActivity::class.java).apply {
                            putExtra("streamUrl", "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8")
                            putExtra("title", "Test Stream")
                            putExtra("subtitle", "Apple Sample HLS Stream")
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Test Playback", fontSize = 20.sp)
                }
            } else {
                Text(
                    text = "Connecting to network...",
                    fontSize = 28.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}