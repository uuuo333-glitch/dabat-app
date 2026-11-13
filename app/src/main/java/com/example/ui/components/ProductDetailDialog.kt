package com.example.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.Order
import com.example.data.model.OrderProduct
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.service.GoogleSeoHelper
import com.example.service.shareProduct
import com.example.ui.theme.StarGold
import com.example.ui.theme.WhatsAppDarkGreen
import com.example.ui.theme.WhatsAppGreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductDetailDialog(
  product: Product,
  whatsappNumber: String = "0565911414",
  customerName: String = "",
  customerContact: String = "",
  storeName: String = "المتجر",
  storeShareLink: String = "https://dabat-altalawuth.sa",
  whatsappAlertsEnabled: Boolean = false,
  onCreateOrder: (Order, (String?) -> Unit) -> Unit,
  onNotifyOwner: (String, String, String, (String?) -> Unit) -> Unit = { _, _, _, onComplete ->
    onComplete("تنبيهات واتساب غير متاحة.")
  },
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  // Selected state for the 4 customizable fields
  var selectedOpt1 by remember(product) { mutableStateOf(product.option1Values.firstOrNull() ?: "") }
  var selectedOpt2 by remember(product) { mutableStateOf(product.option2Values.firstOrNull() ?: "") }
  var selectedOpt3 by remember(product) { mutableStateOf(product.option3Values.firstOrNull() ?: "") }
  var selectedOpt4 by remember(product) { mutableStateOf(product.option4Values.firstOrNull() ?: "") }

  // Customer contact notes
  var customerNameInput by remember(customerName) { mutableStateOf(customerName) }
  var customerContactInput by remember(customerContact) { mutableStateOf(customerContact) }
  var buyerNote by remember { mutableStateOf("") }
  var showSeoModal by remember { mutableStateOf(false) }
  var isSubmittingOrder by remember { mutableStateOf(false) }
  var isSendingOwnerAlert by remember { mutableStateOf(false) }

  val fallbackDrawableId = remember(product.imageDrawableName) {
    val id = context.resources.getIdentifier(product.imageDrawableName, "drawable", context.packageName)
    if (id != 0) id else R.drawable.prod_cat_converter_1794448832780
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .testTag("product_detail_dialog"),
      color = MaterialTheme.colorScheme.background
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar with Close and Share
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(40.dp)
              .background(MaterialTheme.colorScheme.surface, CircleShape)
          ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = MaterialTheme.colorScheme.onSurface)
          }

          Text(
            text = "تفاصيل ومواصفات المنتج",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onSurface
          )

          IconButton(
            onClick = { shareProduct(context, product, storeName, storeShareLink) },
            modifier = Modifier
              .size(40.dp)
              .background(MaterialTheme.colorScheme.surface, CircleShape)
              .testTag("share_product_detail_button")
          ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = "مشاركة", tint = MaterialTheme.colorScheme.primary)
          }
        }

        // Scrollable Body
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
        ) {
          // Large High-Res Image
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(260.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFF0B1320))
          ) {
            Image(
              painter = painterResource(id = fallbackDrawableId),
              contentDescription = product.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )

            // Category overlay
            Surface(
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
              color = MaterialTheme.colorScheme.primary,
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(
                text = product.category,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Price & Rating Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "${product.price.toInt()} ر.س",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp
              )
              if (product.originalPrice != null && product.originalPrice > product.price) {
                Text(
                  text = "${product.originalPrice.toInt()} ر.س (قبل الخصم)",
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 13.sp,
                  textDecoration = TextDecoration.LineThrough
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = StarGold, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${product.rating} (${product.reviewCount} تقييم حقيقي)",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Title
          Text(
            text = product.title,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 26.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Description Card
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "الوصف والمواصفات الفنية:",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = product.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // The 4 Custom Options Section
          Text(
            text = "تحديد الخيارات والمواصفات المطلوبة:",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "يرجى اختيار المواصفات المناسبة لمركبتك قبل إرسال الطلب",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Option 1
          if (product.option1Values.isNotEmpty()) {
            OptionSelector(
              label = product.option1Label,
              options = product.option1Values,
              selectedOption = selectedOpt1,
              onSelect = { selectedOpt1 = it }
            )
            Spacer(modifier = Modifier.height(10.dp))
          }

          // Option 2
          if (product.option2Values.isNotEmpty()) {
            OptionSelector(
              label = product.option2Label,
              options = product.option2Values,
              selectedOption = selectedOpt2,
              onSelect = { selectedOpt2 = it }
            )
            Spacer(modifier = Modifier.height(10.dp))
          }

          // Option 3
          if (product.option3Values.isNotEmpty()) {
            OptionSelector(
              label = product.option3Label,
              options = product.option3Values,
              selectedOption = selectedOpt3,
              onSelect = { selectedOpt3 = it }
            )
            Spacer(modifier = Modifier.height(10.dp))
          }

          // Option 4
          if (product.option4Values.isNotEmpty()) {
            OptionSelector(
              label = product.option4Label,
              options = product.option4Values,
              selectedOption = selectedOpt4,
              onSelect = { selectedOpt4 = it }
            )
            Spacer(modifier = Modifier.height(10.dp))
          }

          // Customer Note Input
          OutlinedTextField(
            value = customerNameInput,
            onValueChange = { customerNameInput = it },
            label = { Text("اسمك") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(10.dp)
          )
          OutlinedTextField(
            value = customerContactInput,
            onValueChange = { customerContactInput = it },
            label = { Text("رقم جوالك") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(10.dp)
          )
          OutlinedTextField(
            value = buyerNote,
            onValueChange = { buyerNote = it },
            label = { Text("ملاحظة إضافية للطلب أو نوع السيارة (اختياري)") },
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            shape = RoundedCornerShape(10.dp)
          )

          // Button to view Google SEO indexing schema
          Button(
            onClick = { showSeoModal = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(
              text = "🔍 معاينة كود الأرشفة في بحث جوجل (Google Schema)",
              color = MaterialTheme.colorScheme.onSurface,
              fontSize = 12.sp
            )
          }

          Spacer(modifier = Modifier.height(16.dp))
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Bottom WhatsApp Order Bar
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              if (whatsappAlertsEnabled) {
                "عند تسجيل الطلب أو إرسال التنبيه، ستُشارك بيانات التواصل وملخصه مع المتجر لمتابعته."
              } else {
                "عند تسجيل الطلب، ستُشارك بيانات التواصل وملخصه مع المتجر لمتابعته."
              },
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (whatsappAlertsEnabled) {
              Button(
                onClick = {
                  if (customerNameInput.isBlank() || customerContactInput.isBlank()) {
                    Toast.makeText(context, "أدخل اسمك ورقم جوالك لإرسال التنبيه", Toast.LENGTH_SHORT).show()
                  } else {
                    isSendingOwnerAlert = true
                    onNotifyOwner(
                      customerNameInput.trim(),
                      customerContactInput.trim(),
                      buildProductRequestSummary(
                        product,
                        selectedOpt1,
                        selectedOpt2,
                        selectedOpt3,
                        selectedOpt4,
                        buyerNote
                      )
                    ) { alertError ->
                      isSendingOwnerAlert = false
                      Toast.makeText(
                        context,
                        alertError ?: "تم إرسال التنبيه إلى المالك",
                        Toast.LENGTH_LONG
                      ).show()
                    }
                  }
                },
                enabled = !isSendingOwnerAlert,
                modifier = Modifier.fillMaxWidth().testTag("notify_owner_whatsapp_button")
              ) {
                Text(if (isSendingOwnerAlert) "جارٍ إرسال التنبيه..." else "تنبيه المالك بطلبي")
              }
            }
            Button(
              onClick = {
                sendLeadSummaryToWhatsApp(
                  context = context,
                  whatsappNumber = whatsappNumber,
                  customerName = customerNameInput,
                  customerContact = customerContactInput,
                  summary = buildProductRequestSummary(
                    product,
                    selectedOpt1,
                    selectedOpt2,
                    selectedOpt3,
                    selectedOpt4,
                    buyerNote
                  )
                )
              },
              modifier = Modifier.fillMaxWidth(),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
              Text("إرسال ملخص الطلب إلى واتساب المتجر", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
              onClick = {
                if (!isSubmittingOrder) {
                  isSubmittingOrder = true
                  val orderNumber = "DB-${UUID.randomUUID().toString().uppercase()}"
                  val order = Order(
                    orderNumber = orderNumber,
                    customerName = customerNameInput.ifBlank { "عميل" },
                    customerContact = customerContactInput.ifBlank { "غير محدد" },
                    products = listOf(
                      OrderProduct(
                        productId = product.id,
                        productName = product.title,
                        unitPrice = product.price,
                        selectedOptions = listOf(selectedOpt1, selectedOpt2, selectedOpt3, selectedOpt4)
                          .filter(String::isNotBlank),
                        customerNote = buyerNote
                      )
                    ),
                    status = OrderStatus.RECEIVED
                  )
                  onCreateOrder(order) { error ->
                    isSubmittingOrder = false
                    Toast.makeText(
                      context,
                      error ?: "تم تسجيل طلبك. رقم الطلب: $orderNumber",
                      Toast.LENGTH_LONG
                    ).show()
                    if (error == null && whatsappAlertsEnabled &&
                      customerNameInput.isNotBlank() && customerContactInput.isNotBlank()
                    ) {
                      onNotifyOwner(
                        customerNameInput.trim(),
                        customerContactInput.trim(),
                        "طلب جديد رقم $orderNumber. " + buildProductRequestSummary(
                          product,
                          selectedOpt1,
                          selectedOpt2,
                          selectedOpt3,
                          selectedOpt4,
                          buyerNote
                        )
                      ) { alertError ->
                        if (alertError != null) {
                          Toast.makeText(context, alertError, Toast.LENGTH_LONG).show()
                        }
                      }
                    }
                    sendOrderToWhatsApp(
                      context = context,
                      whatsappNumber = whatsappNumber,
                      product = product,
                      opt1 = selectedOpt1,
                      opt2 = selectedOpt2,
                      opt3 = selectedOpt3,
                      opt4 = selectedOpt4,
                      note = buyerNote,
                      customerContact = customerContactInput.ifBlank { "عميل مباشر من التطبيق" },
                      orderNumber = orderNumber
                    )
                  }
                }
              },
              enabled = !isSubmittingOrder,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("whatsapp_order_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Send,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isSubmittingOrder) "جارٍ تسجيل الطلب..." else "تسجيل الطلب والمتابعة عبر واتساب",
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                )
              }
            }
          }
        }
      }
    }
  }

  // Google SEO Schema Modal
  if (showSeoModal) {
    Dialog(onDismissRequest = { showSeoModal = false }) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "بيانات الأرشفة المهيأة لمحرك بحث جوجل",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "هذا الكود منظم بصيغة Schema JSON-LD الرسمية لظهور المنتج في Google Search وGoogle Shopping:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
              .padding(8.dp)
              .verticalScroll(rememberScrollState())
          ) {
            Text(
              text = GoogleSeoHelper.generateProductSchemaJsonLd(product),
              color = Color(0xFF34D399),
              fontSize = 11.sp,
              fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
          }
          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = {
              GoogleSeoHelper.shareProductToWebAndGoogle(context, product)
              showSeoModal = false
            },
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("مشاركة الرابط والأرشفة الآن")
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptionSelector(
  label: String,
  options: List<String>,
  selectedOption: String,
  onSelect: (String) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = label,
      fontSize = 13.sp,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(6.dp))
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      options.forEach { option ->
        val isSelected = option == selectedOption
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
          modifier = Modifier
            .border(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
              shape = RoundedCornerShape(8.dp)
            )
            .clickable { onSelect(option) }
        ) {
          Text(
            text = option,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }
    }
  }
}

private fun buildProductRequestSummary(
  product: Product,
  option1: String,
  option2: String,
  option3: String,
  option4: String,
  note: String
): String = buildString {
  appendLine("المنتج: ${product.title}")
  appendLine("السعر: ${product.price.toInt()} ر.س")
  appendLine("${product.option1Label}: $option1")
  appendLine("${product.option2Label}: $option2")
  appendLine("${product.option3Label}: $option3")
  appendLine("${product.option4Label}: $option4")
  if (note.isNotBlank()) append("ملاحظة العميل: $note")
}

private fun sendLeadSummaryToWhatsApp(
  context: Context,
  whatsappNumber: String,
  customerName: String,
  customerContact: String,
  summary: String
) {
  val cleanNumber = whatsappNumber.filter(Char::isDigit)
  if (cleanNumber.isBlank()) {
    Toast.makeText(context, "رقم واتساب المتجر غير متوفر", Toast.LENGTH_LONG).show()
    return
  }
  val formattedNumber = when {
    cleanNumber.startsWith("00966") -> cleanNumber.drop(2)
    cleanNumber.startsWith("0") -> "966${cleanNumber.drop(1)}"
    cleanNumber.startsWith("966") -> cleanNumber
    else -> "966$cleanNumber"
  }
  val message = """
السلام عليكم، أود الاستفسار عن المنتج.
الاسم: ${customerName.ifBlank { "عميل" }}
رقم الجوال: ${customerContact.ifBlank { "غير محدد" }}
$summary
  """.trimIndent()
  try {
    val encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
    context.startActivity(
      Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://wa.me/$formattedNumber?text=$encoded")
      )
    )
  } catch (_: ActivityNotFoundException) {
    Toast.makeText(context, "تعذر فتح واتساب المتجر", Toast.LENGTH_LONG).show()
  }
}

private fun sendOrderToWhatsApp(
  context: Context,
  whatsappNumber: String,
  product: Product,
  opt1: String,
  opt2: String,
  opt3: String,
  opt4: String,
  note: String,
  customerContact: String,
  orderNumber: String
) {
  val cleanNumber = whatsappNumber.replace("+", "").replace(" ", "").trim()
  val formattedNumber = if (cleanNumber.startsWith("0")) {
    "966" + cleanNumber.substring(1)
  } else if (!cleanNumber.startsWith("966")) {
    "966$cleanNumber"
  } else {
    cleanNumber
  }

  val message = """
مرحباً، أود طلب المنتج التالي من متجر دبات التلوث في الديزل والبنزين:
▪ رقم الطلب للتتبع: $orderNumber
▪ المنتج: ${product.title}
▪ السعر: ${product.price.toInt()} ر.س
▪ ${product.option1Label}: $opt1
▪ ${product.option2Label}: $opt2
▪ ${product.option3Label}: $opt3
▪ ${product.option4Label}: $opt4
${if (note.isNotBlank()) "▪ ملاحظة إضافية: $note\n" else ""}▪ بيانات العميل: $customerContact
▪ رابط المنتج للمعاينة: https://dabat-altalawuth.sa/products/${product.id}
  """.trimIndent()

  try {
    val encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber&text=$encoded")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
  } catch (e: Exception) {
    Toast.makeText(context, "تعذر فتح تطبيق الواتساب، تأكد من تثبيته في جهازك", Toast.LENGTH_LONG).show()
  }
}
