package com.example.instatracker

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.content.Intent

class ScrollDetectorService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.packageName?.toString() == "com.instagram.android" && event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            Log.d("ScrollDetector", "Scroll detected in Instagram")
            val intent = Intent("com.example.instatracker.SCROLL_DETECTED")
            sendBroadcast(intent)
        }
    }

    override fun onInterrupt() {
        Log.d("ScrollDetector", "Accessibility service interrupted")
    }
}