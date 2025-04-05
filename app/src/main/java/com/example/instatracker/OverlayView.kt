package com.example.instatracker

import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.content.Context
import android.view.LayoutInflater
import android.widget.TwoLineListItem

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

//fun createOverlayView(context: Context, counter: Int, timeSpent: Long): android.view.View {
//    val inflater = LayoutInflater.from(context)
//    val view = inflater.inflate(android.R.layout.simple_list_item_2, null) as TwoLineListItem // Cast to TwoLineListItem
//    val counterText = view.text1
//    val timerText = view.text2
//    counterText.text = "Reels: $counter"
//    timerText.text = "Time: ${timeSpent}s"
//    view.setBackgroundColor(android.graphics.Color.argb(128, 0, 0, 0)) // Semi-transparent black
//    return view
//}

fun createOverlayView(context: Context, counter: Int, timeSpent: Long): android.view.View {
    val view = TextView(context)
    view.text = "Reels: $counter\nTime: ${timeSpent}s"
    view.setBackgroundColor(android.graphics.Color.argb(128, 0, 0, 0)) // Semi-transparent black
    view.setTextColor(android.graphics.Color.WHITE) // Ensure text is visible
    return view
}