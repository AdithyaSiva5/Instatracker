package com.example.instatracker

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import android.provider.Settings

class TrackingService : Service() {
    private var overlayView: TextView? = null
    private var counter = 0
    private var timeSpent = 0L
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var windowManager: WindowManager
    private lateinit var params: WindowManager.LayoutParams
    private var isOverlayVisible = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TrackingService", "Service started")
        startTracking()
        return START_STICKY
    }

    private fun showOverlay() {
        if (!Settings.canDrawOverlays(this) || isOverlayVisible) return
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            x = 100
            y = 100
        }

        overlayView = TextView(this).apply {
            text = "Reels Watched: $counter\nTime Spent: $timeSpent s"
            setBackgroundColor(android.graphics.Color.argb(128, 0, 0, 255))
            setTextColor(android.graphics.Color.WHITE)
            textSize = 16f
            setPadding(10, 10, 10, 10)
        }

        windowManager.addView(overlayView, params)
        isOverlayVisible = true
        Log.d("TrackingService", "Overlay added successfully")
    }

    private fun hideOverlay() {
        if (!isOverlayVisible) return
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
            isOverlayVisible = false
            Log.d("TrackingService", "Overlay removed")
        }
    }

    fun incrementCounter() {
        counter += 1
        timeSpent += 1
        overlayView?.text = "Reels Watched: $counter\nTime Spent: $timeSpent s"
        Log.d("TrackingService", "Counter: $counter, Time: $timeSpent")
    }

    private fun startTracking() {
        handler.post(object : Runnable {
            override fun run() {
                val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val time = System.currentTimeMillis()
                val stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    time - 1000 * 60 * 60, // Last hour
                    time
                )
                val topApp = stats.maxByOrNull { it.lastTimeUsed }?.packageName
                if (topApp == "com.instagram.android") {
                    showOverlay()
                    incrementCounter() // Increment time while Instagram is open
                } else {
                    hideOverlay()
                }
                handler.postDelayed(this, 1000) // Check every second
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        hideOverlay()
        Log.d("TrackingService", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}