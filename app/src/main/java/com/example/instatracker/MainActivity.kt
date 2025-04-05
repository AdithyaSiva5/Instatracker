package com.example.instatracker

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.provider.Settings
import com.example.instatracker.ui.theme.InstaTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InstaTrackerTheme{
                InstaTrackerScreen()
            }
        }
    }
}

@Composable
fun InstaTrackerScreen() {
    val context = LocalContext.current
    var isServiceRunning by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            try {
                if (!Settings.canDrawOverlays(context)) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                    context.startActivity(intent)
                } else if (!hasUsageStatsPermission(context)) {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    context.startActivity(intent)
                } else {
                    if (!isServiceRunning) {
                        Log.d("MainActivity", "Starting TrackingService")
                        context.startService(Intent(context, TrackingService::class.java))
                        isServiceRunning = true
                    } else {
                        Log.d("MainActivity", "Stopping TrackingService")
                        context.stopService(Intent(context, TrackingService::class.java))
                        isServiceRunning = false
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error starting/stopping service: ${e.message}", e)
            }
        }) {
            Text(if (isServiceRunning) "Disable" else "Enable")
        }
    }
}

private fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}