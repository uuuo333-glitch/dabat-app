package com.example.ui.components

import android.content.ActivityNotFoundException
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.CatalyticCondition
import com.example.data.model.UsedAdCustomerSubscription
import com.example.data.model.UsedAdSubscriptionStatus
import com.example.data.model.UsedCatalyticAd
import com.example.data.model.UsedCatalyticAdStatus
import kotlinx.coroutines.delay

@Composable
fun UsedCatalyticAdsSection(
  ads: List<UsedCatalyticAd>,
  isLoading: Boolean,
  error: String?,
  whatsappNumber: String,
  onCheckSubscription: (String, (UsedAdCustomerSubscription?, String?) -> Unit) -> Unit,
  onSubmitAd: (UsedCatalyticAd, (String?) -> Unit) -> Unit
) {
  val context = LocalContext.current
  var showSubmitDialog by remember { mutableStateOf(false) }
  var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
  var subscriptionPhone by remember { mutableStateOf("") }
  var checkedSubscription by remember { mutableStateOf<UsedAdCustomerSubscription?>(null) }
  var subscriptionCheckError by remember { mutableStateOf<String?>(null) }
  var isCheckingSubscription by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    while (true) {
      delay(30_000)
      currentTime = System.currentTimeMillis()
    }
  }

  val activeAds = remember(ads, currentTime) {
    ads.filter {
      it.status == UsedCatalyticAdStatus.ACTIVE && it.expiresAt > currentTime
    }
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .testTag("used_catalytic_ads_section"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Sell,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          "دبات مستعملة معروضة من العملاء",
          fontWeight = FontWeight.ExtraBold,
          fontSize = 17.sp,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        "سوق مستقل لمنتجات العملاء المستعملة. تواصل مع المالك للاتفاق على عدد المنتجات والسعر.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onPrimaryContainer
      )
      Spacer(modifier = Modifier.height(10.dp))
      OutlinedTextField(
        value = subscriptionPhone,
        onValueChange = {
          subscriptionPhone = it
          checkedSubscription = null
          subscriptionCheckError = null
        },
        label = { Text("رقم جوالك للتحقق من الاشتراك") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(6.dp))
      Button(
        onClick = {
          isCheckingSubscription = true
          onCheckSubscription(subscriptionPhone) { subscription, checkError ->
            isCheckingSubscription = false
            checkedSubscription = subscription
            subscriptionCheckError = checkError
          }
        },
        enabled = !isCheckingSubscription,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(if (isCheckingSubscription) "جارٍ التحقق..." else "تحقق من حالة اشتراكي")
      }
      subscriptionCheckError?.let {
        Spacer(modifier = Modifier.height(6.dp))
        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
      }
      checkedSubscription?.let { subscription ->
        Spacer(modifier = Modifier.height(6.dp))
        val statusText = if (
          subscription.status == UsedAdSubscriptionStatus.ACTIVE &&
          subscription.expiresAt > System.currentTimeMillis()
        ) {
          "حالة الاشتراك: مشترك • المتبقي ${subscription.remainingProducts} من ${subscription.productLimit} منتجات"
        } else {
          "حالة الاشتراك: ${subscription.status.labelArabic}"
        }
        Text(statusText, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 12.sp)
      }
      Button(
        onClick = {
          openWhatsApp(
            context,
            whatsappNumber,
            "السلام عليكم، أرغب بالاشتراك في سوق المنتجات المستعملة لعرض منتجاتي. رقم جوالي: ${subscriptionPhone.trim()}"
          )
        },
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("هل لديك منتجات تريد عرضها؟ اشترك عبر واتساب")
      }
      Button(
        onClick = { showSubmitDialog = true },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
      ) {
        Text("أضف منتجاً مستعملاً للبيع")
      }
      error?.let {
        Spacer(modifier = Modifier.height(6.dp))
        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
      }

      when {
        isLoading -> {
          Spacer(modifier = Modifier.height(8.dp))
          Text("جارٍ تحميل الإعلانات...", color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        activeAds.isEmpty() -> {
          Spacer(modifier = Modifier.height(8.dp))
          Text("لا توجد دبات مستعملة معروضة حالياً.", color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        else -> {
          Spacer(modifier = Modifier.height(12.dp))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(activeAds, key = { it.id }) { ad ->
              UsedCatalyticAdCard(ad = ad)
            }
          }
        }
      }
    }
  }

  if (showSubmitDialog) {
    SubmitUsedCatalyticAdDialog(
      onCheckSubscription = onCheckSubscription,
      whatsappNumber = whatsappNumber,
      onSubmitAd = onSubmitAd,
      onDismiss = { showSubmitDialog = false }
    )
  }
}

@Composable
private fun UsedCatalyticAdCard(ad: UsedCatalyticAd) {
  val context = LocalContext.current
  Card(
    modifier = Modifier.width(270.dp),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column {
      if (ad.imageUrl.isNotBlank()) {
        AsyncImage(
          model = ad.imageUrl,
          contentDescription = ad.catalyticCondition.labelArabic,
          modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
        )
      } else {
        Surface(
          modifier = Modifier.fillMaxWidth().height(100.dp),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Icon(
            imageVector = Icons.Default.Photo,
            contentDescription = null,
            modifier = Modifier.padding(30.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
      Column(modifier = Modifier.padding(12.dp)) {
        Text(
          "${ad.vehicleType} ${ad.vehicleModel}",
          fontWeight = FontWeight.Bold,
          maxLines = 2
        )
        Text("${ad.city} • ${ad.catalyticCondition.labelArabic}", fontSize = 12.sp)
        Text("البائع: ${ad.sellerName}", fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          "${ad.price} ر.س",
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 17.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
          onClick = { openWhatsApp(context, ad.sellerPhone, "السلام عليكم، أرغب بالاستفسار عن ${ad.vehicleType} ${ad.vehicleModel} المعروض بسعر ${ad.price} ر.س") },
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("تواصل مع البائع واتساب")
        }
      }
    }
  }
}

@Composable
private fun SubmitUsedCatalyticAdDialog(
  onCheckSubscription: (String, (UsedAdCustomerSubscription?, String?) -> Unit) -> Unit,
  whatsappNumber: String,
  onSubmitAd: (UsedCatalyticAd, (String?) -> Unit) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var sellerName by remember { mutableStateOf("") }
  var sellerPhone by remember { mutableStateOf("") }
  var subscription by remember { mutableStateOf<UsedAdCustomerSubscription?>(null) }
  var subscriptionCheckError by remember { mutableStateOf<String?>(null) }
  var isCheckingSubscription by remember { mutableStateOf(false) }
  var city by remember { mutableStateOf("") }
  var vehicleType by remember { mutableStateOf("") }
  var vehicleModel by remember { mutableStateOf("") }
  var condition by remember { mutableStateOf(CatalyticCondition.USED) }
  var askingPrice by remember { mutableStateOf("") }
  var imageUrl by remember { mutableStateOf("") }
  var isSubmitting by remember { mutableStateOf(false) }
  var formError by remember { mutableStateOf<String?>(null) }

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
        Text("اعرض دبتك القديمة للبيع", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          "أدخل رقم الجوال المرتبط باشتراكك. يراجع المالك الإعلان قبل عرضه في السوق.",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(sellerName, { sellerName = it }, label = { Text("اسم البائع") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
          value = sellerPhone,
          onValueChange = {
            sellerPhone = it
            subscription = null
            subscriptionCheckError = null
          },
          label = { Text("رقم الجوال المرتبط بالاشتراك") },
          modifier = Modifier.fillMaxWidth()
        )
        Button(
          onClick = {
            isCheckingSubscription = true
            onCheckSubscription(sellerPhone) { result, checkError ->
              isCheckingSubscription = false
              subscription = result
              subscriptionCheckError = checkError
            }
          },
          enabled = !isCheckingSubscription,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(if (isCheckingSubscription) "جارٍ التحقق..." else "تحقق من الاشتراك")
        }
        subscriptionCheckError?.let {
          Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
        subscription?.let { checked ->
          val isActive = checked.status == UsedAdSubscriptionStatus.ACTIVE &&
            checked.expiresAt > System.currentTimeMillis()
          Text(
            when {
              !isActive -> "حالة الاشتراك: ${checked.status.labelArabic}"
              checked.remainingProducts == 0 -> "الاشتراك نشط، لكن اكتمل عدد المنتجات (${checked.productLimit})."
              else -> "الاشتراك نشط • المتبقي ${checked.remainingProducts} من ${checked.productLimit} منتجات"
            },
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp
          )
        }
        Button(
          onClick = {
            openWhatsApp(
              context,
              whatsappNumber,
              "السلام عليكم، أرغب بالاشتراك في سوق المنتجات المستعملة. رقم جوالي: ${sellerPhone.trim()}"
            )
          },
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
          Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("التواصل مع المالك للاشتراك عبر واتساب")
        }
        OutlinedTextField(city, { city = it }, label = { Text("المدينة") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(vehicleType, { vehicleType = it }, label = { Text("نوع السيارة") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(vehicleModel, { vehicleModel = it }, label = { Text("موديل السيارة") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Text("حالة الدبة", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          CatalyticCondition.values().forEach { option ->
            Button(
              onClick = { condition = option },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (condition == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
              )
            ) {
              Text(
                option.labelArabic,
                color = if (condition == option) Color.White else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
        OutlinedTextField(
          value = askingPrice,
          onValueChange = { askingPrice = it },
          label = { Text("سعر البيع (ر.س)") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = imageUrl,
          onValueChange = { imageUrl = it },
          label = { Text("رابط صورة الدبة") },
          modifier = Modifier.fillMaxWidth()
        )
        formError?.let {
          Spacer(modifier = Modifier.height(6.dp))
          Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = onDismiss,
            enabled = !isSubmitting,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Text("إلغاء", color = MaterialTheme.colorScheme.onSurface)
          }
          Button(
            onClick = {
              val parsedPrice = askingPrice.toDoubleOrNull()
              val verifiedSubscription = subscription
              if (
                sellerName.isBlank() || sellerPhone.isBlank() || city.isBlank() ||
                vehicleType.isBlank() || vehicleModel.isBlank() || imageUrl.isBlank() ||
                parsedPrice == null || !parsedPrice.isFinite() || parsedPrice <= 0.0
              ) {
                formError = "أكمل جميع البيانات وأدخل سعراً صحيحاً ورابط صورة."
              } else if (
                verifiedSubscription == null ||
                verifiedSubscription.status != UsedAdSubscriptionStatus.ACTIVE ||
                verifiedSubscription.expiresAt <= System.currentTimeMillis()
              ) {
                formError = "تحقق من اشتراك نشط قبل إرسال المنتج."
              } else if (verifiedSubscription.remainingProducts <= 0) {
                formError = "اكتمل عدد المنتجات المحدد في اشتراكك. تواصل مع المالك لزيادة العدد."
              } else {
                formError = null
                isSubmitting = true
                onSubmitAd(
                  UsedCatalyticAd(
                    sellerName = sellerName.trim(),
                    sellerPhone = sellerPhone.trim(),
                    city = city.trim(),
                    vehicleType = vehicleType.trim(),
                    vehicleModel = vehicleModel.trim(),
                    catalyticCondition = condition,
                    price = parsedPrice,
                    imageUrl = imageUrl.trim(),
                    subscriptionDays = verifiedSubscription.durationDays,
                    subscriptionId = verifiedSubscription.id
                  )
                ) { error ->
                  isSubmitting = false
                  if (error == null) {
                    Toast.makeText(
                      context,
                      "تم إرسال المنتج، وسيظهر بعد مراجعة المالك",
                      Toast.LENGTH_LONG
                    ).show()
                    onDismiss()
                  } else {
                    formError = error
                  }
                }
              }
            },
            enabled = !isSubmitting,
            modifier = Modifier.weight(1f)
          ) {
            Text(if (isSubmitting) "جارٍ الإرسال..." else "إرسال الطلب")
          }
        }
      }
    }
  }
}

private fun openWhatsApp(context: android.content.Context, phone: String, message: String) {
  val digits = phone.filter(Char::isDigit)
  val internationalPhone = when {
    digits.startsWith("0") -> "966${digits.drop(1)}"
    digits.startsWith("966") -> digits
    else -> "966$digits"
  }
  if (internationalPhone.length < 10) {
    Toast.makeText(context, "رقم البائع غير صالح", Toast.LENGTH_SHORT).show()
    return
  }
  try {
    context.startActivity(
      Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$internationalPhone?text=${Uri.encode(message)}"))
    )
  } catch (_: ActivityNotFoundException) {
    Toast.makeText(context, "تعذر فتح واتساب أو المتصفح", Toast.LENGTH_SHORT).show()
  } catch (_: IllegalArgumentException) {
    Toast.makeText(context, "رابط واتساب غير صالح", Toast.LENGTH_SHORT).show()
  }
}

