package com.example.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import kotlinx.coroutines.delay

@Composable
fun VideoBannerCard(
  title: String,
  description: String,
  badgeText: String = "مقطع فيديو حصري",
  placeholderDrawableName: String = "hero_store_banner",
  videoSource: String = "",
  backgroundImageSource: String = "",
  modifier: Modifier = Modifier,
  testTag: String = "video_banner_card"
) {
  val context = LocalContext.current
  var isPlaying by remember { mutableStateOf(false) }
  var playbackProgress by remember { mutableFloatStateOf(0.15f) }

  // Simulate video playback progress
  LaunchedEffect(isPlaying) {
    if (isPlaying) {
      while (isPlaying) {
        delay(300)
        playbackProgress = if (playbackProgress >= 1f) 0.05f else playbackProgress + 0.03f
      }
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .testTag(testTag),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(210.dp)
    ) {
      // Dynamic banner/video background image
      val drawableId = remember(placeholderDrawableName) {
        val id = context.resources.getIdentifier(placeholderDrawableName, "drawable", context.packageName)
        if (id != 0) id else R.drawable.hero_store_banner_1794448546017
      }

      if (backgroundImageSource.isBlank()) {
        Image(
          painter = painterResource(id = drawableId),
          contentDescription = title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      } else {
        AsyncImage(
          model = backgroundImageSource,
          contentDescription = title,
          placeholder = painterResource(id = drawableId),
          error = painterResource(id = drawableId),
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }

      // Dark gradient overlay
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color.Black.copy(alpha = 0.35f),
                Color.Black.copy(alpha = 0.88f)
              )
            )
          )
      )

      // Top Badge
      Surface(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(12.dp),
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(20.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = badgeText,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        }
      }

      // Center Play / Pause Button
      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .size(56.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.65f))
          .clickable {
            if (videoSource.isBlank()) {
              isPlaying = !isPlaying
            } else {
              try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(videoSource)))
              } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, "لا يوجد تطبيق لتشغيل الفيديو أو فتح الرابط", Toast.LENGTH_SHORT).show()
              } catch (_: IllegalArgumentException) {
                Toast.makeText(context, "رابط الفيديو غير صالح", Toast.LENGTH_SHORT).show()
              }
            }
          }
          .testTag("${testTag}_play_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
          contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل الفيديو",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(34.dp)
        )
      }

      // Bottom Content & Progress
      Column(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .fillMaxWidth()
          .padding(14.dp)
      ) {
        Text(
          text = title,
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          maxLines = 2
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = description,
          color = Color(0xFFE2E8F0),
          fontSize = 12.sp,
          maxLines = 2
        )

        AnimatedVisibility(visible = isPlaying) {
          Column(modifier = Modifier.padding(top = 8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "جاري تشغيل الفيديو التوضيحي عالي الدقة...",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp
              )
              Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
              progress = { playbackProgress },
              modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
              color = MaterialTheme.colorScheme.primary,
              trackColor = Color.White.copy(alpha = 0.2f)
            )
          }
        }
      }
    }
  }
}
