package com.example.instatracker

import android.content.Context
import android.view.MotionEvent
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.view.WindowManager

@Composable
fun OverlayContent(counter: Int, timeSpent: Long) {
    Column(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(8.dp)
    ) {
        Text(text = "Reels: $counter", color = Color.White)
        Text(text = "Time: ${timeSpent}s", color = Color.White)
    }
}

fun createOverlayView(context: Context, counter: Int, timeSpent: Long, params: WindowManager.LayoutParams, windowManager: WindowManager): android.view.View {
    val view = TextView(context).apply {
        text = "Reels Watched: $counter\nTime Spent: ${timeSpent}s"
        setBackgroundColor(android.graphics.Color.argb(128, 0, 0, 255))
        setTextColor(android.graphics.Color.WHITE)
        textSize = 16f
        setPadding(10, 10, 10, 10)
    }

    var initialX = 0f
    var initialY = 0f
    var initialTouchX = 0f
    var initialTouchY = 0f

    view.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params.x.toFloat()
                initialY = params.y.toFloat()
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                true
            }
            MotionEvent.ACTION_MOVE -> {
                params.x = (initialX + (event.rawX - initialTouchX)).toInt()
                params.y = (initialY + (event.rawY - initialTouchY)).toInt()
                windowManager.updateViewLayout(view, params)
                true
            }
            else -> false
        }
    }
    return view
}

fun updateOverlayView(view: TextView, counter: Int, timeSpent: Long) {
    view.text = "Reels Watched: $counter\nTime Spent: ${timeSpent}s"
}