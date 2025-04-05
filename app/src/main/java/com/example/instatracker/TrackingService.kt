package com.example.instatracker

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
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
    private var lastScrollTime = 0L // Track last scroll event time
    private val debounceInterval = 500L // 500ms debounce interval

    private val scrollReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.instatracker.SCROLL_DETECTED") {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastScrollTime >= debounceInterval) { // Check if enough time has passed
                    counter += 1 // Increase reel count only if debounced
                    lastScrollTime = currentTime // Update last scroll time
                    updateOverlay()
                    Log.d("TrackingService", "Scroll detected, Counter: $counter")
                } else {
                    Log.d("TrackingService", "Scroll ignored (debounced)")
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val intentFilter = IntentFilter("com.example.instatracker.SCROLL_DETECTED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // API 26+
            registerReceiver(
                scrollReceiver,
                intentFilter,
                RECEIVER_NOT_EXPORTED
            )
        } else { // API 24 and 25
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(scrollReceiver, intentFilter)
        }
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
            gravity = Gravity.TOP or Gravity.START // Use 'or' instead of '|' for Kotlin, or clarify with parentheses
            x = 100 // Start near the left, adjustable
            y = 100
        }

        overlayView = TextView(this).apply {
            // Premium styling
            text = "Reels Watched: $counter\nTime Spent: $timeSpent s"
            setBackgroundResource(R.drawable.premium_background)
            setTextColor(android.graphics.Color.parseColor("#333333"))
            textSize = 16f
            setPadding(24, 16, 24, 16)
            alpha = 0.9f
            elevation = 8f

        }

        var initialX = 0f
        var initialY = 0f
        var initialTouchX = 0f
        var initialTouchY = 0f

        overlayView?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x.toFloat()
                    initialY = params.y.toFloat()
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Update positions for both horizontal and vertical movement
                    params.x = (initialX + (event.rawX - initialTouchX)).toInt()
                    params.y = (initialY + (event.rawY - initialTouchY)).toInt()

                    // Apply boundary constraints if needed
                    if (params.x < 0) params.x = 0
                    if (params.x > resources.displayMetrics.widthPixels - 250)
                        params.x = resources.displayMetrics.widthPixels - 250

                    windowManager.updateViewLayout(overlayView, params)
                    true
                }
                else -> false
            }
        }

        windowManager.addView(overlayView, params)
        isOverlayVisible = true
        Log.d("TrackingService", "Premium overlay added successfully")
    }

    private fun updateOverlay() {
        overlayView?.text = "Reels Watched: $counter\nTime Spent: $timeSpent s"
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
                    timeSpent += 1 // Only increase time here
                    updateOverlay()
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
        unregisterReceiver(scrollReceiver)
        Log.d("TrackingService", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}