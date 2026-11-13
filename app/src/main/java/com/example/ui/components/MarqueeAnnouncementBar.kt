package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarqueeSpeed

@Composable
fun MarqueeAnnouncementBar(
  text: String,
  speed: MarqueeSpeed,
  bgColorHex: String,
  modifier: Modifier = Modifier
) {
  val bgColor = try {
    Color(android.graphics.Color.parseColor(bgColorHex))
  } catch (_: Exception) {
    Color(0xFFD4AF37)
  }

  val marqueeVelocity = when (speed) {
    MarqueeSpeed.SLOW -> 35.dp
    MarqueeSpeed.MEDIUM -> 75.dp
    MarqueeSpeed.FAST -> 150.dp
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(bgColor)
      .padding(vertical = 8.dp, horizontal = 12.dp)
      .testTag("marquee_announcement_bar"),
    contentAlignment = Alignment.CenterStart
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .basicMarquee(
          iterations = Int.MAX_VALUE,
          velocity = marqueeVelocity,
          initialDelayMillis = 300
        ),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Campaign,
        contentDescription = "إعلان",
        tint = Color.Black,
        modifier = Modifier.padding(end = 8.dp)
      )
      Text(
        text = text,
        color = Color.Black,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        maxLines = 1,
        softWrap = false
      )
      Text(
        text = "     ⚡ دبات التلوث في الديزل والبنزين 0565911414 ⚡     $text",
        color = Color.Black,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        maxLines = 1,
        softWrap = false
      )
    }
  }
}
