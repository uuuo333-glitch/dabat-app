package com.example.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.CommercialAdDurationUnit
import com.example.data.model.CommercialStoreAd
import kotlinx.coroutines.delay

@Composable
fun CommercialStoreAdsSection(
  ads: List<CommercialStoreAd>,
  isLoading: Boolean,
  error: String?,
  onSubmitAd: (CommercialStoreAd, (String?) -> Unit) -> Unit
) {
  var showSubmitDialog by remember { mutableStateOf(false) }
  var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
  LaunchedEffect(Unit) {
    while (true) {
      delay(30_000)
      currentTime = System.currentTimeMillis()
    }
  }
  val currentAds = remember(ads, currentTime) {
    ads.filter { it.expiryTimestamp > currentTime }
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Text(
        "دليل وإعلانات المحلات التجارية",
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onSecondaryContainer
      )
      Spacer(modifier = Modifier.height(5.dp))
      Text(
        "اكتشف المحلات والورش التجارية القريبة منك.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSecondaryContainer
      )
      Spacer(modifier = Modifier.height(10.dp))
      Button(onClick = { showSubmitDialog = true }, modifier = Modifier.fillMaxWidth()) {
        Text("أعلن عن محلك التجاري هنا")
      }
      error?.let {
        Spacer(modifier = Modifier.height(6.dp))
        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
      }

      when {
        isLoading -> {
          Spacer(modifier = Modifier.height(8.dp))
          Text("جارٍ تحميل المحلات...", color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        currentAds.isEmpty() -> {
          Spacer(modifier = Modifier.height(8.dp))
          Text("لا توجد إعلانات محلات نشطة حالياً.", color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        else -> {
          Spacer(modifier = Modifier.height(12.dp))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(currentAds, key = { it.id }) { ad -> CommercialStoreAdCard(ad) }
          }
        }
      }
    }
  }

  if (showSubmitDialog) {
    CommercialStoreAdRequestDialog(
      onSubmitAd = onSubmitAd,
      onDismiss = { showSubmitDialog = false }
    )
  }
}

@Composable
private fun CommercialStoreAdCard(ad: CommercialStoreAd) {
  val context = LocalContext.current
  Card(
    modifier = Modifier.width(310.dp),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(ad.storeName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
      Text(ad.goodsType, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text("${ad.region} • ${ad.city}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      if (ad.imageUrls.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(ad.imageUrls.take(4)) { imageUrl ->
            AsyncImage(
              model = imageUrl,
              contentDescription = "صورة ${ad.storeName}",
              modifier = Modifier
                .size(width = 260.dp, height = 145.dp)
                .clip(RoundedCornerShape(8.dp))
            )
          }
        }
      }
      if (ad.videoUrl.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Button(
          onClick = { openCommercialLink(context, ad.videoUrl) },
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Text("مشاهدة مقطع المحل", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Button(
          onClick = { openCommercialLink(context, ad.locationUrl) },
          enabled = ad.locationUrl.isNotBlank(),
          modifier = Modifier.weight(1f)
        ) { Text("الموقع") }
        Button(
          onClick = {
            openCommercialLink(context, whatsappUrl(ad.contactPhone))
          },
          modifier = Modifier.weight(1f),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) { Text("واتساب") }
        Button(
          onClick = {
            openCommercialLink(context, "tel:${Uri.encode(ad.contactPhone)}")
          },
          modifier = Modifier.weight(1f),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) { Text("اتصال") }
      }
    }
  }
}

@Composable
private fun CommercialStoreAdRequestDialog(
  onSubmitAd: (CommercialStoreAd, (String?) -> Unit) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var storeName by remember { mutableStateOf("") }
  var goodsType by remember { mutableStateOf("") }
  var region by remember { mutableStateOf("") }
  var city by remember { mutableStateOf("") }
  var locationUrl by remember { mutableStateOf("") }
  var contactPhone by remember { mutableStateOf("") }
  var image1 by remember { mutableStateOf("") }
  var image2 by remember { mutableStateOf("") }
  var image3 by remember { mutableStateOf("") }
  var image4 by remember { mutableStateOf("") }
  var videoUrl by remember { mutableStateOf("") }
  var durationValue by remember { mutableStateOf("30") }
  var durationUnit by remember { mutableStateOf(CommercialAdDurationUnit.DAYS) }
  var formError by remember { mutableStateOf<String?>(null) }
  var isSubmitting by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = { if (!isSubmitting) onDismiss() },
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(18.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text("طلب إعلان محل تجاري أو ورشة", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          "يراجع المالك الطلب ويتواصل معك لإتمام الدفع قبل تفعيل الإعلان.",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(storeName, { storeName = it }, label = { Text("اسم المحل") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(goodsType, { goodsType = it }, label = { Text("نوع السلع المعروضة") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(region, { region = it }, label = { Text("المنطقة") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(city, { city = it }, label = { Text("المدينة") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(locationUrl, { locationUrl = it }, label = { Text("رابط الموقع في خرائط جوجل") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(contactPhone, { contactPhone = it }, label = { Text("رقم التواصل/واتساب") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Text("روابط الصور (حتى 4 صور)", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(image1, { image1 = it }, label = { Text("رابط الصورة الأولى") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(image2, { image2 = it }, label = { Text("رابط الصورة الثانية") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(image3, { image3 = it }, label = { Text("رابط الصورة الثالثة") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(image4, { image4 = it }, label = { Text("رابط الصورة الرابعة") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(videoUrl, { videoUrl = it }, label = { Text("رابط مقطع الفيديو") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = durationValue,
          onValueChange = { durationValue = it },
          label = { Text("مدة الإعلان المطلوبة") },
          modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          CommercialAdDurationUnit.values().forEach { unit ->
            Button(
              onClick = { durationUnit = unit },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (durationUnit == unit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
              )
            ) {
              Text(
                unit.labelArabic,
                color = if (durationUnit == unit) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
        formError?.let {
          Spacer(modifier = Modifier.height(6.dp))
          Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = onDismiss,
            enabled = !isSubmitting,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) { Text("إلغاء", color = MaterialTheme.colorScheme.onSurface) }
          Button(
            onClick = {
              val parsedDuration = durationValue.toIntOrNull()
              if (
                storeName.isBlank() || goodsType.isBlank() || region.isBlank() ||
                city.isBlank() || locationUrl.isBlank() || contactPhone.isBlank() ||
                parsedDuration == null || parsedDuration <= 0
              ) {
                formError = "أكمل البيانات الأساسية وأدخل مدة صحيحة."
              } else {
                formError = null
                isSubmitting = true
                onSubmitAd(
                  CommercialStoreAd(
                    storeName = storeName.trim(),
                    goodsType = goodsType.trim(),
                    imageUrls = listOf(image1, image2, image3, image4)
                      .map(String::trim)
                      .filter(String::isNotBlank),
                    videoUrl = videoUrl.trim(),
                    region = region.trim(),
                    city = city.trim(),
                    locationUrl = locationUrl.trim(),
                    contactPhone = contactPhone.trim(),
                    durationValue = parsedDuration,
                    durationUnit = durationUnit
                  )
                ) { submitError ->
                  isSubmitting = false
                  if (submitError == null) {
                    Toast.makeText(context, "تم إرسال طلب الإعلان للمراجعة", Toast.LENGTH_LONG).show()
                    onDismiss()
                  } else {
                    formError = submitError
                  }
                }
              }
            },
            enabled = !isSubmitting,
            modifier = Modifier.weight(1f)
          ) { Text(if (isSubmitting) "جارٍ الإرسال..." else "إرسال الطلب") }
        }
      }
    }
  }
}

private fun whatsappUrl(phone: String): String {
  val digits = phone.filter(Char::isDigit)
  val normalized = when {
    digits.startsWith("00966") -> digits.drop(2)
    digits.startsWith("0") -> "966${digits.drop(1)}"
    digits.startsWith("966") -> digits
    else -> "966$digits"
  }
  return "https://wa.me/$normalized"
}

private fun openCommercialLink(context: Context, link: String) {
  if (link.isBlank()) return
  try {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
  } catch (_: ActivityNotFoundException) {
    Toast.makeText(context, "لا يوجد تطبيق مناسب لفتح الرابط", Toast.LENGTH_SHORT).show()
  } catch (_: IllegalArgumentException) {
    Toast.makeText(context, "الرابط غير صالح", Toast.LENGTH_SHORT).show()
  }
}
