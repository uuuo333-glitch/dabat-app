package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StoreSettings
import com.example.service.shareStore
import com.example.ui.theme.WhatsAppGreen

@Composable
fun FooterSection(
  settings: StoreSettings,
  onOpenOwnerPortal: () -> Unit,
  onSubmitComplaint: (name: String, contact: String, type: String, content: String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showAboutDialog by remember { mutableStateOf(false) }
  var showComplaintDialog by remember { mutableStateOf(false) }
  var showPolicyDialog by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
      .padding(horizontal = 20.dp, vertical = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Store Title
    Text(
      text = settings.storeName,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 17.sp,
      color = MaterialTheme.colorScheme.primary,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = settings.footerDescription,
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )

    IconButton(
      onClick = { shareStore(context, settings.storeName, settings.storeShareLink) },
      modifier = Modifier.testTag("share_store_footer_button")
    ) {
      Icon(
        imageVector = Icons.Default.Share,
        contentDescription = "مشاركة المتجر",
        tint = MaterialTheme.colorScheme.primary
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Social Media Icons Row
    Text(
      text = "تابعنا وتواصل معنا على شبكات التواصل الاجتماعي:",
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(10.dp))

    Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      SocialButton(label = "واتساب", icon = Icons.Default.Chat, color = WhatsAppGreen) {
        val whatsappUrl = settings.whatsappUrl.ifBlank {
          "https://api.whatsapp.com/send?phone=${settings.whatsappNumber}"
        }
        openUrl(context, whatsappUrl)
      }
      SocialButton(label = "إنستغرام", icon = Icons.Default.AlternateEmail, color = Color(0xFFE1306C)) {
        openUrl(context, settings.instagramUrl)
      }
      SocialButton(label = "تيك توك", icon = Icons.Default.Send, color = Color(0xFF00F2FE)) {
        openUrl(context, settings.tiktokUrl)
      }
      SocialButton(label = "يوتيوب", icon = Icons.Default.Feedback, color = Color(0xFFFF0000)) {
        openUrl(context, settings.youtubeUrl)
      }
      SocialButton(label = "سناب شات", icon = Icons.Default.AlternateEmail, color = Color(0xFFFFFC00)) {
        openUrl(context, settings.snapchatUrl)
      }
      SocialButton(label = "إكس", icon = Icons.Default.AlternateEmail, color = Color(0xFF1DA1F2)) {
        openUrl(context, settings.xTwitterUrl)
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    Spacer(modifier = Modifier.height(16.dp))

    // Direct Contact Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Direct Email
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clickable {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
              data = Uri.parse("mailto:${settings.storeEmail}")
              putExtra(Intent.EXTRA_SUBJECT, "استفسار من تطبيق متجر دبات التلوث")
            }
            context.startActivity(intent)
          }
          .padding(4.dp)
      ) {
        Icon(imageVector = Icons.Default.AlternateEmail, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = settings.storeEmail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
      }

      // Direct Call
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clickable {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${settings.storePhone}"))
            context.startActivity(intent)
          }
          .padding(4.dp)
      ) {
        Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = settings.storePhone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Nav Links (About Us, Suggestions/Complaints, Policies)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "من نحن",
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        modifier = Modifier
          .clickable {
            if (settings.aboutUsUrl.isNotBlank()) openUrl(context, settings.aboutUsUrl)
            else showAboutDialog = true
          }
          .padding(8.dp)
      )
      Text(text = " • ", color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(
        text = "الاقتراحات والشكاوى",
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        modifier = Modifier
          .clickable {
            if (settings.complaintsUrl.isNotBlank()) openUrl(context, settings.complaintsUrl)
            else showComplaintDialog = true
          }
          .padding(8.dp)
      )
      Text(text = " • ", color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(
        text = "شروط وسياسة الإرجاع",
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        modifier = Modifier
          .clickable {
            if (settings.returnPolicyUrl.isNotBlank()) openUrl(context, settings.returnPolicyUrl)
            else showPolicyDialog = true
          }
          .padding(8.dp)
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Owner Login Link
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .clickable(onClick = onOpenOwnerPortal)
        .padding(8.dp)
        .testTag("owner_portal_link")
    ) {
      Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = "بوابة المالك",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "دخول مالك المتجر (المدير والذكاء الاصطناعي)",
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "جميع الحقوق محفوظة © 2026 متجر دبات التلوث في الديزل والبنزين",
      fontSize = 10.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
      textAlign = TextAlign.Center
    )
  }

  // About Us Dialog
  if (showAboutDialog) {
    Dialog(onDismissRequest = { showAboutDialog = false }) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "من نحن - متجر دبات التلوث", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }
          Spacer(modifier = Modifier.height(12.dp))
          Text(text = settings.aboutUsText, fontSize = 13.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface)
          Spacer(modifier = Modifier.height(16.dp))
          Button(onClick = { showAboutDialog = false }, modifier = Modifier.align(Alignment.End)) {
            Text("إغلاق")
          }
        }
      }
    }
  }

  // Return Policy Dialog
  if (showPolicyDialog) {
    Dialog(onDismissRequest = { showPolicyDialog = false }) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Policy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "شروط وسياسة الاستبدال والاسترجاع", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }
          Spacer(modifier = Modifier.height(12.dp))
          Text(text = settings.returnPolicyText, fontSize = 13.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurface)
          Spacer(modifier = Modifier.height(16.dp))
          Button(onClick = { showPolicyDialog = false }, modifier = Modifier.align(Alignment.End)) {
            Text("موافق")
          }
        }
      }
    }
  }

  // Suggestions & Complaints Dialog
  if (showComplaintDialog) {
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("اقتراح") }
    var contentText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { showComplaintDialog = false }) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(text = "نموذج الاقتراحات والشكاوى", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Text(text = "صوتك يهمنا لتطوير خدمات المتجر وتوفير القطع النادرة", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(10.dp))

          // Type toggle
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
              onClick = { type = "اقتراح" },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (type == "اقتراح") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
              ),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("💡 تقديم اقتراح", color = if (type == "اقتراح") Color.Black else MaterialTheme.colorScheme.onSurface)
            }
            Button(
              onClick = { type = "شكوى" },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (type == "شكوى") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant
              ),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("⚠️ تسجيل شكوى", color = if (type == "شكوى") Color.White else MaterialTheme.colorScheme.onSurface)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("الاسم الكريم") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = contact,
            onValueChange = { contact = it },
            label = { Text("رقم الجوال أو البريد الإلكتروني") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = contentText,
            onValueChange = { contentText = it },
            label = { Text("نص الاقتراح أو الشكوى") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(12.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = { showComplaintDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
              Text("إلغاء", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                if (contentText.isNotBlank()) {
                  onSubmitComplaint(name.ifBlank { "عميل" }, contact.ifBlank { "غير محدد" }, type, contentText)
                  Toast.makeText(context, "تم إرسال رسالتك بنجاح، شكراً لتواصلك معنا", Toast.LENGTH_LONG).show()
                  showComplaintDialog = false
                } else {
                  Toast.makeText(context, "يرجى كتابة نص الرسالة أولاً", Toast.LENGTH_SHORT).show()
                }
              }
            ) {
              Text("إرسال الآن")
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SocialButton(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  color: Color,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .size(42.dp)
      .clip(CircleShape)
      .background(color.copy(alpha = 0.18f))
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = color,
      modifier = Modifier.size(20.dp)
    )
  }
}

private fun openUrl(context: Context, url: String) {
  try {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
  } catch (e: Exception) {
    Toast.makeText(context, "تعذر فتح الرابط", Toast.LENGTH_SHORT).show()
  }
}
