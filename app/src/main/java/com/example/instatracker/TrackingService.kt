package com.example.instatracker

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.view.View

class TrackingService : Service() {
    private var overlayView: View? = null
    private var counter = 0
    private var timeSpent = 0L
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var windowManager: WindowManager
    private lateinit var params: WindowManager.LayoutParams

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TrackingService", "Service started")
        try {
            showOverlay()
            startTracking()
        } catch (e: Exception) {
            Log.e("TrackingService", "Error in onStartCommand: ${e.message}", e)
        }
        return START_STICKY
    }

    private fun showOverlay() {
        Log.d("TrackingService", "Showing overlay")
        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            overlayView = createOverlayView(this, counter, timeSpent)
            Log.d("TrackingService", "Overlay view created: ${overlayView != null}")

            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                android.graphics.PixelFormat.TRANSLUCENT
            )
            params.gravity = android.view.Gravity.TOP or android.view.Gravity.LEFT
            params.x = 100 // Offset from left edge
            params.y = 100
            windowManager.addView(overlayView, params)
            Log.d("TrackingService", "Overlay added successfully")
        } catch (e: Exception) {
            Log.e("TrackingService", "Error adding overlay: ${e.message}", e)
        }
    }

//    private fun showOverlay() {
//        Log.d("TrackingService", "Showing overlay")
//        Thread {
//            try {
//                windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
//                overlayView = createOverlayView(this, counter, timeSpent)
//                Log.d("TrackingService", "Overlay view created: ${overlayView != null}")
//
//                params = WindowManager.LayoutParams(
//                    WindowManager.LayoutParams.WRAP_CONTENT,
//                    WindowManager.LayoutParams.WRAP_CONTENT,
//                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                    android.graphics.PixelFormat.TRANSLUCENT
//                )
//                params.gravity = android.view.Gravity.TOP or android.view.Gravity.LEFT
//                params.x = 100
//                params.y = 100
//                windowManager.addView(overlayView, params)
//                Log.d("TrackingService", "Overlay added successfully")
//            } catch (e: Exception) {
//                Log.e("TrackingService", "Error adding overlay: ${e.message}", e)
//            }
//        }.start()
//    }

    private fun startTracking() {
        Log.d("TrackingService", "Starting tracking")
        handler.post(object : Runnable {
            override fun run() {
                try {
                    val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
                    val time = System.currentTimeMillis()
                    val stats = usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY, time - 1000 * 60 * 60 * 24 * 7, time // 7 days
                    )
                    Log.d("TrackingService", "Usage stats size: ${stats.size}")
                    val topApp = stats.maxByOrNull { it.lastTimeUsed }?.packageName
                    Log.d("TrackingService", "Top app: $topApp, Last used: ${stats.maxByOrNull { it.lastTimeUsed }?.lastTimeUsed}")

                    if (topApp == "com.instagram.android") {
                        timeSpent += 1
                        if (timeSpent % 5 == 0L) counter += 1
                        overlayView?.let { windowManager.removeView(it) }
                        overlayView = createOverlayView(this@TrackingService, counter, timeSpent)
                        windowManager.addView(overlayView, params)
                        overlayView?.visibility = View.VISIBLE
                        Log.d("TrackingService", "Instagram detected, counter: $counter, time: $timeSpent")
                    } else {
                        overlayView?.visibility = View.GONE
                        Log.d("TrackingService", "Not Instagram, current top app: $topApp")
                    }
                } catch (e: Exception) {
                    Log.e("TrackingService", "Error in tracking: ${e.message}", e)
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        overlayView?.let { windowManager.removeView(it) }
        Log.d("TrackingService", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}