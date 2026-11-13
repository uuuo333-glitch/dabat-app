package com.example.ui.components

import android.content.Context
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Complaint
import com.example.data.model.CommercialAdDurationUnit
import com.example.data.model.CommercialStoreAd
import com.example.data.model.CommercialStoreAdStatus
import com.example.data.model.MarqueeSpeed
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.OwnerWallet
import com.example.data.model.OwnerWalletTransaction
import com.example.data.model.OwnerWalletTransactionType
import com.example.data.model.OwnerAuthenticationResult
import com.example.data.model.Product
import com.example.data.model.StoreSettings
import com.example.data.model.UsedAdCustomerSubscription
import com.example.data.model.UsedAdSubscriptionStatus
import com.example.data.model.UsedCatalyticAd
import com.example.data.model.UsedCatalyticAdStatus
import com.example.data.model.VisitorStats
import com.example.service.AiDescriptionGenerator
import com.example.ui.theme.StoreThemePalette
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date
import java.util.Locale

@Composable
fun OwnerAdminPortal(
  isOwnerLoggedIn: Boolean,
  settings: StoreSettings,
  products: List<Product>,
  complaints: List<Complaint>,
  orders: List<Order>,
  ordersLoading: Boolean,
  ordersSyncError: String?,
  visitorStats: VisitorStats,
  visitorStatsError: String?,
  usedCatalyticAds: List<UsedCatalyticAd>,
  usedAdsLoading: Boolean,
  usedAdsError: String?,
  usedAdCustomerSubscriptions: List<UsedAdCustomerSubscription>,
  usedAdSubscriptionsLoading: Boolean,
  usedAdSubscriptionsError: String?,
  commercialStoreAds: List<CommercialStoreAd>,
  commercialStoreAdsLoading: Boolean,
  commercialStoreAdsError: String?,
  ownerWallet: OwnerWallet,
  ownerWalletTransactions: List<OwnerWalletTransaction>,
  ownerWalletLoading: Boolean,
  ownerWalletError: String?,
  onAuthenticateOwner: (String, Boolean, (OwnerAuthenticationResult) -> Unit) -> Unit,
  onVerifyOwnerCode: (String, String, (Boolean, String) -> Unit) -> Unit,
  onResetOwnerPassword: ((String) -> Unit) -> Unit,
  onLogout: () -> Unit,
  onUpdateSettings: (StoreSettings) -> Unit,
  onAddProduct: (Product) -> Unit,
  onUpdateProduct: (Product) -> Unit,
  onDeleteProduct: (String) -> Unit,
  onUpdateOrder: (Order, (String?) -> Unit) -> Unit,
  onResetVisitorCounts: ((String?) -> Unit) -> Unit,
  onSaveUsedAdCustomerSubscription: (UsedAdCustomerSubscription, (String?) -> Unit) -> Unit,
  onUpdateUsedAdCustomerSubscriptionStatus: (UsedAdCustomerSubscription, UsedAdSubscriptionStatus, (String?) -> Unit) -> Unit,
  onApproveUsedCatalyticAd: (UsedCatalyticAd, (String?) -> Unit) -> Unit,
  onAddCommercialStoreAd: (CommercialStoreAd, (String?) -> Unit) -> Unit,
  onApproveOrRenewCommercialStoreAd: (CommercialStoreAd, (String?) -> Unit) -> Unit,
  onExpireCommercialStoreAd: (CommercialStoreAd, (String?) -> Unit) -> Unit,
  onSaveOwnerBankDetails: (String, String, (String?) -> Unit) -> Unit,
  onAddOwnerWalletTransaction: (OwnerWalletTransactionType, String, String, Long, (String?) -> Unit) -> Unit,
  onImportUrl: suspend (String) -> Product,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var ownerPasswordInput by remember { mutableStateOf("") }
  var ownerOtpInput by remember { mutableStateOf("") }
  var ownerLoginChallengeId by remember { mutableStateOf<String?>(null) }
  var isOwnerAuthenticating by remember { mutableStateOf(false) }
  var selectedTab by remember { mutableIntStateOf(0) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .testTag("owner_admin_portal"),
      color = MaterialTheme.colorScheme.background
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Admin Top Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "لوحة تحكم مالك المتجر الحصرية",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "الذكاء الاصطناعي، إدارة المنتجات، الثيم، والفيديوهات",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }

          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = MaterialTheme.colorScheme.onSurface)
          }
        }

        if (!isOwnerLoggedIn) {
          // Owner Login Gate
          OwnerLoginGate(
            passwordInput = ownerPasswordInput,
            onPasswordChange = { ownerPasswordInput = it },
            otpInput = ownerOtpInput,
            onOtpChange = { ownerOtpInput = it.filter(Char::isDigit).take(6) },
            hasPendingOtp = ownerLoginChallengeId != null,
            isAuthenticating = isOwnerAuthenticating,
            onPasswordReset = {
              if (!isOwnerAuthenticating) {
                isOwnerAuthenticating = true
                onResetOwnerPassword { message ->
                  isOwnerAuthenticating = false
                  Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
              }
            },
            onVerifyCode = {
              val challengeId = ownerLoginChallengeId
              if (!isOwnerAuthenticating && challengeId != null) {
                isOwnerAuthenticating = true
                onVerifyOwnerCode(challengeId, ownerOtpInput) { authenticated, message ->
                  isOwnerAuthenticating = false
                  Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                  if (authenticated) {
                    ownerLoginChallengeId = null
                    ownerOtpInput = ""
                  }
                }
              }
            },
            onAuthenticationAttempt = { createAccount ->
              if (!isOwnerAuthenticating) {
                isOwnerAuthenticating = true
                onAuthenticateOwner(ownerPasswordInput, createAccount) { result ->
                  isOwnerAuthenticating = false
                  if (result.challengeId != null) {
                    ownerLoginChallengeId = result.challengeId
                    ownerOtpInput = ""
                  }
                  Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
              }
            }
          )
        } else {
          // Tabs for Owner Management
          val tabs = listOf("🤖 المنتجات والذكاء الاصطناعي", "🔗 سحب بالرابط", "🎨 الاسم والثيم", "📢 الشريط والفيديو", "⚙️ محتوى الفوتر والخلفيات", "🔍 سيو وتنزيل APK", "📦 إدارة الطلبات (${orders.size})", "📩 الشكاوى (${complaints.size})", "🚗 سوق المستعمل (${usedCatalyticAds.size})", "🏬 إعلانات المحلات (${commercialStoreAds.size})", "💰 محفظتي وخزينة المتجر")

          ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surface
          ) {
            tabs.forEachIndexed { index, title ->
              Tab(
                selected = selectedTab == index,
                onClick = { selectedTab = index },
                text = { Text(text = title, fontSize = 12.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
              )
            }
          }

          VisitorStatsCard(
            stats = visitorStats,
            error = visitorStatsError,
            onReset = onResetVisitorCounts
          )

          Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
              0 -> AiProductsTab(
                products = products,
                onAddProduct = onAddProduct,
                onUpdateProduct = onUpdateProduct,
                onDeleteProduct = onDeleteProduct
              )
              1 -> ImportByUrlTab(onImportUrl = onImportUrl)
              2 -> ThemeAndNameTab(settings = settings, onUpdateSettings = onUpdateSettings)
              3 -> MarqueeAndVideoTab(settings = settings, onUpdateSettings = onUpdateSettings)
              4 -> ContentSettingsTab(settings = settings, onUpdateSettings = onUpdateSettings)
              5 -> GoogleSeoAndApkTab(products = products, settings = settings)
              6 -> OrdersManagementTab(
                orders = orders,
                isLoading = ordersLoading,
                syncError = ordersSyncError,
                onUpdateOrder = onUpdateOrder
              )
              7 -> ComplaintsTab(complaints = complaints)
              8 -> UsedCatalyticAdsAdminTab(
                ads = usedCatalyticAds,
                isLoading = usedAdsLoading,
                error = usedAdsError,
                subscriptions = usedAdCustomerSubscriptions,
                subscriptionsLoading = usedAdSubscriptionsLoading,
                subscriptionsError = usedAdSubscriptionsError,
                onSaveSubscription = onSaveUsedAdCustomerSubscription,
                onUpdateSubscriptionStatus = onUpdateUsedAdCustomerSubscriptionStatus,
                onApproveAd = onApproveUsedCatalyticAd
              )
              9 -> CommercialStoreAdsAdminTab(
                ads = commercialStoreAds,
                isLoading = commercialStoreAdsLoading,
                error = commercialStoreAdsError,
                onAddAd = onAddCommercialStoreAd,
                onApproveOrRenewAd = onApproveOrRenewCommercialStoreAd,
                onExpireAd = onExpireCommercialStoreAd
              )
              10 -> OwnerWalletTab(
                wallet = ownerWallet,
                transactions = ownerWalletTransactions,
                isLoading = ownerWalletLoading,
                error = ownerWalletError,
                onSaveBankDetails = onSaveOwnerBankDetails,
                onAddTransaction = onAddOwnerWalletTransaction
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun OwnerLoginGate(
  passwordInput: String,
  onPasswordChange: (String) -> Unit,
  otpInput: String,
  onOtpChange: (String) -> Unit,
  hasPendingOtp: Boolean,
  isAuthenticating: Boolean,
  onPasswordReset: () -> Unit,
  onVerifyCode: () -> Unit,
  onAuthenticationAttempt: (createAccount: Boolean) -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(60.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.VpnKey,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "تسجيل دخول مالك المتجر",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "البريد المعتمد للمالك: uuuo333@gmail.com. في أول استخدام أنشئ الحساب وحدد كلمة مرور، ثم أكد بريدك الإلكتروني.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (hasPendingOtp) {
          OutlinedTextField(
            value = otpInput,
            onValueChange = onOtpChange,
            label = { Text("رمز التحقق المرسل إلى البريد") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = onVerifyCode,
            enabled = !isAuthenticating && otpInput.length == 6,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(if (isAuthenticating) "جارٍ التحقق..." else "تأكيد الرمز والدخول")
          }
          TextButton(
            onClick = { onAuthenticationAttempt(false) },
            enabled = !isAuthenticating,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("إرسال رمز جديد")
          }
        } else {
          OutlinedTextField(
            value = passwordInput,
            onValueChange = onPasswordChange,
            label = { Text("كلمة مرور المالك") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = { onAuthenticationAttempt(false) },
            enabled = !isAuthenticating,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(if (isAuthenticating) "جارٍ التحقق..." else "إرسال رمز الدخول", fontWeight = FontWeight.Bold)
          }

          TextButton(
            onClick = { onAuthenticationAttempt(true) },
            enabled = !isAuthenticating,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("إنشاء حساب المالك وتحديد كلمة المرور")
          }
          TextButton(
            onClick = onPasswordReset,
            enabled = !isAuthenticating,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("إرسال رابط تعيين / استعادة كلمة المرور")
          }
        }
      }
    }
  }
}

@Composable
private fun AiProductsTab(
  products: List<Product>,
  onAddProduct: (Product) -> Unit,
  onUpdateProduct: (Product) -> Unit,
  onDeleteProduct: (String) -> Unit
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  var showAddDialog by remember { mutableStateOf(false) }
  var editingProduct by remember { mutableStateOf<Product?>(null) }
  var isAiGenerating by remember { mutableStateOf(false) }

  // Form Fields
  var promptInput by remember { mutableStateOf("") }
  var formTitle by remember { mutableStateOf("") }
  var formDesc by remember { mutableStateOf("") }
  var formPrice by remember { mutableStateOf("") }
  var formCategory by remember { mutableStateOf("دبات تلوث") }
  var formOpt1Label by remember { mutableStateOf("نوع الوقود والمحرك") }
  var formOpt1Values by remember { mutableStateOf("بنزين Petrol, ديزل Diesel") }
  var formOpt2Label by remember { mutableStateOf("نوع ونقاء الخلية") }
  var formOpt2Values by remember { mutableStateOf("سيراميك أصلي, معدن رياضي 200 Cell") }
  var formOpt3Label by remember { mutableStateOf("مقاس القطر") }
  var formOpt3Values by remember { mutableStateOf("2.0 بوصة, 2.5 بوصة, 3.0 بوصة") }
  var formOpt4Label by remember { mutableStateOf("توافق الموديل والفحص") }
  var formOpt4Values by remember { mutableStateOf("مطابقة للفحص الدوري 100%, مع فتحة حساس O2") }

  fun resetForm(product: Product? = null) {
    if (product != null) {
      formTitle = product.title
      formDesc = product.description
      formPrice = product.price.toInt().toString()
      formCategory = product.category
      formOpt1Label = product.option1Label
      formOpt1Values = product.option1Values.joinToString(", ")
      formOpt2Label = product.option2Label
      formOpt2Values = product.option2Values.joinToString(", ")
      formOpt3Label = product.option3Label
      formOpt3Values = product.option3Values.joinToString(", ")
      formOpt4Label = product.option4Label
      formOpt4Values = product.option4Values.joinToString(", ")
    } else {
      formTitle = ""
      formDesc = ""
      formPrice = ""
      formCategory = "دبات تلوث"
      formOpt1Label = "نوع الوقود والمحرك"
      formOpt1Values = "بنزين Petrol, ديزل Diesel"
      formOpt2Label = "نوع ونقاء الخلية"
      formOpt2Values = "سيراميك أصلي, معدن رياضي 200 Cell"
      formOpt3Label = "مقاس القطر"
      formOpt3Values = "2.0 بوصة, 2.5 بوصة, 3.0 بوصة"
      formOpt4Label = "توافق الموديل والفحص"
      formOpt4Values = "مطابقة للفحص الدوري 100%, مع فتحة حساس O2"
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(scrollState)
  ) {
    // Top banner for AI
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "المساعد الذكي الخاص بك (غير مرئي للعملاء)",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Text(
            text = "اكتب اسم أي قطعة أو سيارة وسيقوم الذكاء الاصطناعي بكتابة الوصف والمواصفات وتعبئة الخيارات الـ 4 آلياً!",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Button(
      onClick = {
        editingProduct = null
        resetForm()
        showAddDialog = true
      },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(10.dp)
    ) {
      Icon(imageVector = Icons.Default.Add, contentDescription = null)
      Spacer(modifier = Modifier.width(6.dp))
      Text("إضافة منتج جديد مع الذكاء الاصطناعي")
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "قائمة المنتجات الحالية في متجرك (${products.size}):",
      fontWeight = FontWeight.Bold,
      fontSize = 15.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    products.forEach { prod ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp),
        shape = RoundedCornerShape(10.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(text = prod.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            Text(text = "السعر: ${prod.price.toInt()} ر.س • ${prod.category}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            Text(text = "الخيارات الـ 4: ${prod.option1Label} | ${prod.option2Label} | ${prod.option3Label} | ${prod.option4Label}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }

          Row {
            IconButton(
              onClick = {
                editingProduct = prod
                resetForm(prod)
                showAddDialog = true
              }
            ) {
              Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { onDeleteProduct(prod.id) }) {
              Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
            }
          }
        }
      }
    }
  }

  // Add / Edit Product Modal with AI
  if (showAddDialog) {
    Dialog(
      onDismissRequest = { showAddDialog = false },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (editingProduct == null) "إضافة منتج وتعبئة الخيارات الـ 4" else "تعديل المنتج",
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            )
            IconButton(onClick = { showAddDialog = false }) {
              Icon(imageVector = Icons.Default.Close, contentDescription = null)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // AI Prompt Generator Box
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "كتابة الوصف والخيارات بـ Gemini 3.8 Flash الذكي:",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.primary
                )
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "مدعوم رسمياً بنموذج Google Gemini API عبر Firebase AI Logic",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(6.dp))
              OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                label = { Text("اكتب اسم القطعة (مثال: دبة تلوث ديزل لاندكروزر أو بنزين كامري)") },
                modifier = Modifier.fillMaxWidth()
              )
              Spacer(modifier = Modifier.height(8.dp))
              Button(
                onClick = {
                  if (promptInput.isNotBlank()) {
                    scope.launch {
                      isAiGenerating = true
                      val generated = AiDescriptionGenerator.generateProductDetails(promptInput)
                      formTitle = generated.title
                      formDesc = generated.description
                      formPrice = generated.suggestedPrice.toInt().toString()
                      formOpt1Label = generated.option1Label
                      formOpt1Values = generated.option1Values.joinToString(", ")
                      formOpt2Label = generated.option2Label
                      formOpt2Values = generated.option2Values.joinToString(", ")
                      formOpt3Label = generated.option3Label
                      formOpt3Values = generated.option3Values.joinToString(", ")
                      formOpt4Label = generated.option4Label
                      formOpt4Values = generated.option4Values.joinToString(", ")
                      isAiGenerating = false
                      Toast.makeText(context, "تم توليد وتعبئة الوصف والخيارات بالذكاء الاصطناعي بنجاح!", Toast.LENGTH_SHORT).show()
                    }
                  }
                },
                enabled = !isAiGenerating,
                modifier = Modifier.fillMaxWidth()
              ) {
                if (isAiGenerating) {
                  CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("جاري الصياغة والتوليد بالذكاء الاصطناعي...")
                } else {
                  Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("توليد الوصف والخيارات الأربعة الآن")
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = formTitle,
            onValueChange = { formTitle = it },
            label = { Text("عنوان المنتج") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = formDesc,
            onValueChange = { formDesc = it },
            label = { Text("الوصف التفصيلي للمنتج") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = formPrice,
              onValueChange = { formPrice = it },
              label = { Text("السعر (ر.س)") },
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = formCategory,
              onValueChange = { formCategory = it },
              label = { Text("التصنيف") },
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))
          Text(text = "الخيارات الأربعة المخصصة (يقوم المالك بتعبئتها وتظهر للعميل للاختيار):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.height(6.dp))

          // Field 1
          OutlinedTextField(value = formOpt1Label, onValueChange = { formOpt1Label = it }, label = { Text("اسم الخيار 1") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = formOpt1Values, onValueChange = { formOpt1Values = it }, label = { Text("قيم الخيار 1 (افصل بفاصلة)") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(8.dp))

          // Field 2
          OutlinedTextField(value = formOpt2Label, onValueChange = { formOpt2Label = it }, label = { Text("اسم الخيار 2") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = formOpt2Values, onValueChange = { formOpt2Values = it }, label = { Text("قيم الخيار 2 (افصل بفاصلة)") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(8.dp))

          // Field 3
          OutlinedTextField(value = formOpt3Label, onValueChange = { formOpt3Label = it }, label = { Text("اسم الخيار 3") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = formOpt3Values, onValueChange = { formOpt3Values = it }, label = { Text("قيم الخيار 3 (افصل بفاصلة)") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(8.dp))

          // Field 4
          OutlinedTextField(value = formOpt4Label, onValueChange = { formOpt4Label = it }, label = { Text("اسم الخيار 4") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = formOpt4Values, onValueChange = { formOpt4Values = it }, label = { Text("قيم الخيار 4 (افصل بفاصلة)") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              if (formTitle.isNotBlank()) {
                val p = Product(
                  id = editingProduct?.id ?: "cat-${System.currentTimeMillis() % 10000}",
                  title = formTitle,
                  description = formDesc,
                  price = formPrice.toDoubleOrNull() ?: 1200.0,
                  category = formCategory,
                  option1Label = formOpt1Label,
                  option1Values = formOpt1Values.split(",").map { it.trim() }.filter { it.isNotBlank() },
                  option2Label = formOpt2Label,
                  option2Values = formOpt2Values.split(",").map { it.trim() }.filter { it.isNotBlank() },
                  option3Label = formOpt3Label,
                  option3Values = formOpt3Values.split(",").map { it.trim() }.filter { it.isNotBlank() },
                  option4Label = formOpt4Label,
                  option4Values = formOpt4Values.split(",").map { it.trim() }.filter { it.isNotBlank() },
                  imageDrawableName = editingProduct?.imageDrawableName ?: "prod_cat_converter_1794448832780"
                )
                if (editingProduct == null) onAddProduct(p) else onUpdateProduct(p)
                showAddDialog = false
                Toast.makeText(context, "تم حفظ المنتج بنجاح!", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp)
          ) {
            Text("حفظ المنتج في المتجر", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun ImportByUrlTab(
  onImportUrl: suspend (String) -> Product
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  var urlInput by remember { mutableStateOf("") }
  var isScraping by remember { mutableStateOf(false) }
  var importedResult by remember { mutableStateOf<Product?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(rememberScrollState())
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "سحب المنتجات من أي موقع إلكتروني بالرابط", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "الصق رابط منتج دبة تلوث أو قطعة عادم من أي موقع خارجي، وسيقوم النظام آلياً باستخراج الصور والعنوان والوصف وتجهيزه لمتجرك:",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    OutlinedTextField(
      value = urlInput,
      onValueChange = { urlInput = it },
      label = { Text("رابط المنتج (مثال: https://example.com/product/123)") },
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    Button(
      onClick = {
        if (urlInput.isNotBlank()) {
          scope.launch {
            isScraping = true
            try {
              val p = onImportUrl(urlInput.trim())
              importedResult = p
              Toast.makeText(context, "تم سحب المنتج وإضافته لمتجرك بنجاح!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
              Toast.makeText(context, "حدث خطأ أثناء السحب: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
              isScraping = false
            }
          }
        }
      },
      enabled = !isScraping && urlInput.isNotBlank(),
      modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
      if (isScraping) {
        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text("جاري استخراج بيانات المنتج والصور...")
      } else {
        Icon(imageVector = Icons.Default.Download, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text("سحب واستيراد المنتج الآن")
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    if (importedResult != null) {
      val res = importedResult!!
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(text = "✅ تم استيراد المنتج بنجاح:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "العنوان: ${res.title}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          Text(text = "السعر المقترح: ${res.price.toInt()} ر.س", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
          Text(text = "الخيارات الـ 4 المخصصة مجهزة وجاهزة في المتجر للطلب عبر الواتساب.", fontSize = 11.sp)
        }
      }
    }
  }
}

@Composable
private fun ThemeAndNameTab(
  settings: StoreSettings,
  onUpdateSettings: (StoreSettings) -> Unit
) {
  val context = LocalContext.current
  var storeNameInput by remember(settings) { mutableStateOf(settings.storeName) }
  var whatsappInput by remember(settings) { mutableStateOf(settings.whatsappNumber) }
  var emailInput by remember(settings) { mutableStateOf(settings.storeEmail) }
  var phoneInput by remember(settings) { mutableStateOf(settings.storePhone) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(rememberScrollState())
  ) {
    Text(text = "تعديل اسم المتجر وهوية الألوان (Theme):", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
      value = storeNameInput,
      onValueChange = { storeNameInput = it },
      label = { Text("اسم المتجر الرئيسي") },
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
      value = whatsappInput,
      onValueChange = { whatsappInput = it },
      label = { Text("رقم الواتساب المعتمد للطلبات (0565911414)") },
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
      value = emailInput,
      onValueChange = { emailInput = it },
      label = { Text("البريد الإلكتروني للمتجر") },
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(text = "اختيار باليت ألوان الثيم للمتجر:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
    Spacer(modifier = Modifier.height(8.dp))

    StoreThemePalette.values().forEach { palette ->
      val isSelected = settings.activeTheme == palette.name
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
          .border(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
            shape = RoundedCornerShape(10.dp)
          )
          .clickable {
            onUpdateSettings(settings.copy(activeTheme = palette.name))
          },
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(Color(android.graphics.Color.parseColor(palette.primaryHex)))
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(text = palette.labelArabic, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
          if (isSelected) {
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "مفعل حالياً ✓", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
      onClick = {
        onUpdateSettings(
          settings.copy(
            storeName = storeNameInput,
            whatsappNumber = whatsappInput,
            storeEmail = emailInput,
            storePhone = phoneInput
          )
        )
        Toast.makeText(context, "تم حفظ وتحديث إعدادات واسم المتجر بنجاح", Toast.LENGTH_SHORT).show()
      },
      modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
      Text("حفظ التغييرات الآن")
    }
  }
}

@Composable
private fun MarqueeAndVideoTab(
  settings: StoreSettings,
  onUpdateSettings: (StoreSettings) -> Unit
) {
  val context = LocalContext.current
  var marqueeTextInput by remember(settings) { mutableStateOf(settings.marqueeText) }
  var selectedSpeed by remember(settings) { mutableStateOf(settings.marqueeSpeed) }
  var marqueeBgInput by remember(settings) { mutableStateOf(settings.marqueeBgColorHex) }

  var headerTitleInput by remember(settings) { mutableStateOf(settings.headerVideoTitle) }
  var headerDescInput by remember(settings) { mutableStateOf(settings.headerVideoDesc) }
  var footerTitleInput by remember(settings) { mutableStateOf(settings.footerVideoTitle) }
  var footerDescInput by remember(settings) { mutableStateOf(settings.footerVideoDesc) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(rememberScrollState())
  ) {
    Text(text = "إعدادات الشريط الإعلاني المتحرك (Top Marquee):", fontWeight = FontWeight.Bold, fontSize = 15.sp)
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = marqueeTextInput,
      onValueChange = { marqueeTextInput = it },
      label = { Text("نص الإعلان في الشريط المتحرك") },
      minLines = 2,
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(text = "سرعة حركة الشريط (مرن وقوالب للسرعات):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 6.dp)) {
      MarqueeSpeed.values().forEach { speed ->
        Button(
          onClick = { selectedSpeed = speed },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (selectedSpeed == speed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
          ),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(text = speed.labelArabic, color = if (selectedSpeed == speed) Color.Black else MaterialTheme.colorScheme.onSurface)
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(16.dp))

    Text(text = "مقاطع فيديو رأس وأسفل الصفحة (Header & Footer Videos):", fontWeight = FontWeight.Bold, fontSize = 15.sp)
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = headerTitleInput,
      onValueChange = { headerTitleInput = it },
      label = { Text("عنوان فيديو رأس الصفحة (الهيدر)") },
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(6.dp))
    OutlinedTextField(
      value = headerDescInput,
      onValueChange = { headerDescInput = it },
      label = { Text("وصف فيديو الهيدر") },
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
      value = footerTitleInput,
      onValueChange = { footerTitleInput = it },
      label = { Text("عنوان فيديو أسفل الصفحة (تجارب العملاء)") },
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(6.dp))
    OutlinedTextField(
      value = footerDescInput,
      onValueChange = { footerDescInput = it },
      label = { Text("وصف فيديو الفوتر") },
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
      onClick = {
        onUpdateSettings(
          settings.copy(
            marqueeText = marqueeTextInput,
            marqueeSpeed = selectedSpeed,
            marqueeBgColorHex = marqueeBgInput,
            headerVideoTitle = headerTitleInput,
            headerVideoDesc = headerDescInput,
            footerVideoTitle = footerTitleInput,
            footerVideoDesc = footerDescInput
          )
        )
        Toast.makeText(context, "تم حفظ إعدادات الشريط المتحرك والفيديوهات بنجاح", Toast.LENGTH_SHORT).show()
      },
      modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
      Text("حفظ التحديثات الآن")
    }
  }
}

private enum class ContentPickerTarget {
  HEADER_VIDEO,
  FOOTER_VIDEO,
  HEADER_BACKGROUND,
  FOOTER_BACKGROUND,
  MAIN_BACKGROUND
}

@Composable
private fun ContentSettingsTab(
  settings: StoreSettings,
  onUpdateSettings: (StoreSettings) -> Unit
) {
  val context = LocalContext.current
  var storeNameInput by remember(settings) { mutableStateOf(settings.storeName) }
  var storeShareLinkInput by remember(settings) { mutableStateOf(settings.storeShareLink) }
  var phoneInput by remember(settings) { mutableStateOf(settings.storePhone) }
  var emailInput by remember(settings) { mutableStateOf(settings.storeEmail) }
  var whatsappInput by remember(settings) { mutableStateOf(settings.whatsappNumber) }
  var ownerWhatsappInput by remember(settings) { mutableStateOf(settings.ownerWhatsappPhone) }
  var whatsappApiKeyInput by remember(settings) { mutableStateOf(settings.whatsappApiKey) }
  var whatsappAlertsEnabled by remember(settings) { mutableStateOf(settings.whatsappAlertsEnabled) }
  var headerVideoUrl by remember(settings) { mutableStateOf(settings.headerVideoUrl) }
  var footerVideoUrl by remember(settings) { mutableStateOf(settings.footerVideoUrl) }
  var headerBackground by remember(settings) { mutableStateOf(settings.headerVideoBackground) }
  var footerBackground by remember(settings) { mutableStateOf(settings.footerVideoBackground) }
  var mainBackground by remember(settings) { mutableStateOf(settings.mainBackgroundImage) }
  var footerDescription by remember(settings) { mutableStateOf(settings.footerDescription) }
  var whatsappUrl by remember(settings) { mutableStateOf(settings.whatsappUrl) }
  var instagramUrl by remember(settings) { mutableStateOf(settings.instagramUrl) }
  var tiktokUrl by remember(settings) { mutableStateOf(settings.tiktokUrl) }
  var snapchatUrl by remember(settings) { mutableStateOf(settings.snapchatUrl) }
  var youtubeUrl by remember(settings) { mutableStateOf(settings.youtubeUrl) }
  var xUrl by remember(settings) { mutableStateOf(settings.xTwitterUrl) }
  var aboutUsText by remember(settings) { mutableStateOf(settings.aboutUsText) }
  var aboutUsUrl by remember(settings) { mutableStateOf(settings.aboutUsUrl) }
  var complaintsUrl by remember(settings) { mutableStateOf(settings.complaintsUrl) }
  var returnPolicyText by remember(settings) { mutableStateOf(settings.returnPolicyText) }
  var returnPolicyUrl by remember(settings) { mutableStateOf(settings.returnPolicyUrl) }
  var pickerTarget by remember { mutableStateOf<ContentPickerTarget?>(null) }

  val documentPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri: Uri? ->
    if (uri != null) {
      try {
        context.contentResolver.takePersistableUriPermission(
          uri,
          Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        when (pickerTarget) {
          ContentPickerTarget.HEADER_VIDEO -> headerVideoUrl = uri.toString()
          ContentPickerTarget.FOOTER_VIDEO -> footerVideoUrl = uri.toString()
          ContentPickerTarget.HEADER_BACKGROUND -> headerBackground = uri.toString()
          ContentPickerTarget.FOOTER_BACKGROUND -> footerBackground = uri.toString()
          ContentPickerTarget.MAIN_BACKGROUND -> mainBackground = uri.toString()
          null -> Unit
        }
      } catch (error: SecurityException) {
        Toast.makeText(context, "تعذر حفظ صلاحية الوصول إلى الملف المحدد", Toast.LENGTH_LONG).show()
      }
    }
    pickerTarget = null
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(rememberScrollState())
  ) {
    Text("تعديل محتوى أسفل الصفحة والخلفيات", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      "أدخل رابطاً مباشراً أو اختر ملفاً من الجهاز. تفتح الفيديوهات في مشغل خارجي، وتستخدم الخلفيات الصورة المختارة مع الصورة الافتراضية عند تعذر تحميلها.",
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
      value = headerVideoUrl,
      onValueChange = { headerVideoUrl = it },
      label = { Text("رابط أو ملف فيديو رأس الصفحة") },
      modifier = Modifier.fillMaxWidth()
    )
    Button(
      onClick = {
        pickerTarget = ContentPickerTarget.HEADER_VIDEO
        documentPicker.launch(arrayOf("video/*"))
      },
      modifier = Modifier.fillMaxWidth()
    ) { Text("اختيار فيديو الرأس من الجهاز") }

    OutlinedTextField(
      value = footerVideoUrl,
      onValueChange = { footerVideoUrl = it },
      label = { Text("رابط أو ملف فيديو أسفل الصفحة") },
      modifier = Modifier.fillMaxWidth()
    )
    Button(
      onClick = {
        pickerTarget = ContentPickerTarget.FOOTER_VIDEO
        documentPicker.launch(arrayOf("video/*"))
      },
      modifier = Modifier.fillMaxWidth()
    ) { Text("اختيار فيديو أسفل الصفحة من الجهاز") }

    OutlinedTextField(
      value = headerBackground,
      onValueChange = { headerBackground = it },
      label = { Text("رابط أو ملف صورة خلفية فيديو الرأس") },
      modifier = Modifier.fillMaxWidth()
    )
    Button(
      onClick = {
        pickerTarget = ContentPickerTarget.HEADER_BACKGROUND
        documentPicker.launch(arrayOf("image/*"))
      },
      modifier = Modifier.fillMaxWidth()
    ) { Text("اختيار صورة خلفية الرأس من الجهاز") }

    OutlinedTextField(
      value = footerBackground,
      onValueChange = { footerBackground = it },
      label = { Text("رابط أو ملف صورة خلفية فيديو أسفل الصفحة") },
      modifier = Modifier.fillMaxWidth()
    )
    Button(
      onClick = {
        pickerTarget = ContentPickerTarget.FOOTER_BACKGROUND
        documentPicker.launch(arrayOf("image/*"))
      },
      modifier = Modifier.fillMaxWidth()
    ) { Text("اختيار صورة خلفية أسفل الصفحة من الجهاز") }

    OutlinedTextField(
      value = mainBackground,
      onValueChange = { mainBackground = it },
      label = { Text("رابط أو ملف صورة خلفية التطبيق") },
      modifier = Modifier.fillMaxWidth()
    )
    Button(
      onClick = {
        pickerTarget = ContentPickerTarget.MAIN_BACKGROUND
        documentPicker.launch(arrayOf("image/*"))
      },
      modifier = Modifier.fillMaxWidth()
    ) { Text("اختيار خلفية التطبيق من الجهاز") }

    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(16.dp))

    Text("بيانات المتجر والفوتر", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    Spacer(modifier = Modifier.height(8.dp))
    SettingsTextField("عنوان المتجر", storeNameInput, { storeNameInput = it })
    SettingsTextField("رابط مشاركة المتجر", storeShareLinkInput, { storeShareLinkInput = it })
    SettingsTextField("وصف المتجر أسفل الصفحة", footerDescription, { footerDescription = it }, minLines = 2)
    SettingsTextField("رقم الهاتف", phoneInput, { phoneInput = it })
    SettingsTextField("البريد الإلكتروني", emailInput, { emailInput = it })
    SettingsTextField("رقم واتساب الطلبات", whatsappInput, { whatsappInput = it })
    Spacer(modifier = Modifier.height(12.dp))
    Text("تنبيهات واتساب للمالك", fontWeight = FontWeight.Bold, fontSize = 15.sp)
    SettingsTextField(
      "رقم واتساب المالك لتلقي التنبيهات",
      ownerWhatsappInput,
      { ownerWhatsappInput = it }
    )
    OutlinedTextField(
      value = whatsappApiKeyInput,
      onValueChange = { whatsappApiKeyInput = it },
      label = { Text("مفتاح ربط تنبيهات واتساب API") },
      visualTransformation = PasswordVisualTransformation(),
      singleLine = true,
      modifier = Modifier.fillMaxWidth()
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("تفعيل تنبيهات واتساب")
      Switch(
        checked = whatsappAlertsEnabled,
        onCheckedChange = { whatsappAlertsEnabled = it }
      )
    }
    Text(
      "عند التفعيل، يرسل التطبيق ملخص الطلب وبيانات التواصل إلى رقم المالك عبر بوابة CallMeBot.",
      fontSize = 11.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    SettingsTextField("رابط واتساب", whatsappUrl, { whatsappUrl = it })
    SettingsTextField("رابط إنستغرام", instagramUrl, { instagramUrl = it })
    SettingsTextField("رابط تيك توك", tiktokUrl, { tiktokUrl = it })
    SettingsTextField("رابط سناب شات", snapchatUrl, { snapchatUrl = it })
    SettingsTextField("رابط يوتيوب", youtubeUrl, { youtubeUrl = it })
    SettingsTextField("رابط إكس", xUrl, { xUrl = it })

    Spacer(modifier = Modifier.height(12.dp))
    Text("صفحات الفوتر", fontWeight = FontWeight.Bold, fontSize = 15.sp)
    SettingsTextField("نص صفحة من نحن", aboutUsText, { aboutUsText = it }, minLines = 3)
    SettingsTextField("رابط صفحة من نحن (اختياري)", aboutUsUrl, { aboutUsUrl = it })
    SettingsTextField("رابط صفحة الشكاوى (اختياري)", complaintsUrl, { complaintsUrl = it })
    SettingsTextField("نص السياسة والاسترجاع", returnPolicyText, { returnPolicyText = it }, minLines = 3)
    SettingsTextField("رابط السياسة (اختياري)", returnPolicyUrl, { returnPolicyUrl = it })

    Spacer(modifier = Modifier.height(16.dp))
    Button(
      onClick = {
        if (whatsappAlertsEnabled && (ownerWhatsappInput.isBlank() || whatsappApiKeyInput.isBlank())) {
          Toast.makeText(context, "أدخل رقم المالك ومفتاح API قبل تفعيل التنبيهات", Toast.LENGTH_LONG).show()
        } else {
          onUpdateSettings(
            settings.copy(
              storeName = storeNameInput,
              storeShareLink = storeShareLinkInput,
              storePhone = phoneInput,
              storeEmail = emailInput,
              whatsappNumber = whatsappInput,
              ownerWhatsappPhone = ownerWhatsappInput.trim(),
              whatsappApiKey = whatsappApiKeyInput.trim(),
              whatsappAlertsEnabled = whatsappAlertsEnabled,
              headerVideoUrl = headerVideoUrl,
              footerVideoUrl = footerVideoUrl,
              headerVideoBackground = headerBackground,
              footerVideoBackground = footerBackground,
              mainBackgroundImage = mainBackground,
              footerDescription = footerDescription,
              whatsappUrl = whatsappUrl,
              instagramUrl = instagramUrl,
              tiktokUrl = tiktokUrl,
              snapchatUrl = snapchatUrl,
              youtubeUrl = youtubeUrl,
              xTwitterUrl = xUrl,
              aboutUsText = aboutUsText,
              aboutUsUrl = aboutUsUrl,
              complaintsUrl = complaintsUrl,
              returnPolicyText = returnPolicyText,
              returnPolicyUrl = returnPolicyUrl
            )
          )
          Toast.makeText(context, "تم حفظ إعدادات الفيديوهات والخلفيات والفوتر والتنبيهات", Toast.LENGTH_SHORT).show()
        }
      },
      modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
      Text("حفظ إعدادات المحتوى")
    }
  }
}

@Composable
private fun SettingsTextField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  minLines: Int = 1
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label) },
    minLines = minLines,
    modifier = Modifier.fillMaxWidth()
  )
}

@Composable
private fun GoogleSeoAndApkTab(
  products: List<Product>,
  settings: StoreSettings
) {
  val context = LocalContext.current

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(rememberScrollState())
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "ظهور المنتجات في بحث جوجل (Google SEO)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "تم تجهيز جميع منتجاتك بمخطط Schema.org المعتمد من محرك بحث جوجل (Rich Snippets). ستظهر منتجاتك تلقائياً مع السعر، النجوم، التقييم، وتوافر المخزون.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(text = "محاكاة شكل منتجك في بحث جوجل على الجوال:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
    Spacer(modifier = Modifier.height(8.dp))

    val firstProd = products.firstOrNull()
    if (firstProd != null) {
      // Google search result card simulation
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(text = "dabat-altalawuth.sa › products › ${firstProd.id}", color = Color(0xFF4D5156), fontSize = 11.sp)
          Spacer(modifier = Modifier.height(2.dp))
          Text(text = "${firstProd.title} - متجر دبات التلوث الأصلي", color = Color(0xFF1A0DAB), fontSize = 15.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "⭐⭐⭐⭐⭐ تقييم: ${firstProd.rating} • السعر: ${firstProd.price.toInt()} ر.س • متوفر في المخزن • اطلب عبر الواتساب 0565911414", color = Color(0xFF4D5156), fontSize = 12.sp)
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(16.dp))

    // APK Download instructions
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      Spacer(modifier = Modifier.width(8.dp))
      Text(text = "تنزيل التطبيق وتثبيته على جهازك الخاص:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }

    Spacer(modifier = Modifier.height(8.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = "📲 خطوات تثبيت التطبيق على جوالك الأندرويد:",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = """
1. افتح قائمة الإعدادات أو زر المشاركة/البناء في منصة AI Studio.
2. اضغط على خيار "Export APK" أو "تصدير حزمة الأندرويد".
3. سيتم تنزيل ملف التثبيت بصيغة APK على جوالك مباشرة.
4. اضغط على الملف ووافق على التثبيت ليظهر تطبيق متجرك "دبات التلوث" على الشاشة الرئيسية لجوالك بأيقونته الفاخرة المعتمدة.
          """.trimIndent(),
          fontSize = 12.sp,
          lineHeight = 20.sp,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }
    }
  }
}

@Composable
private fun UsedCatalyticAdsAdminTab(
  ads: List<UsedCatalyticAd>,
  isLoading: Boolean,
  error: String?,
  subscriptions: List<UsedAdCustomerSubscription>,
  subscriptionsLoading: Boolean,
  subscriptionsError: String?,
  onSaveSubscription: (UsedAdCustomerSubscription, (String?) -> Unit) -> Unit,
  onUpdateSubscriptionStatus: (UsedAdCustomerSubscription, UsedAdSubscriptionStatus, (String?) -> Unit) -> Unit,
  onApproveAd: (UsedCatalyticAd, (String?) -> Unit) -> Unit
) {
  val context = LocalContext.current
  var sellerName by remember { mutableStateOf("") }
  var sellerPhone by remember { mutableStateOf("") }
  var productLimitInput by remember { mutableStateOf("") }
  var agreedPriceInput by remember { mutableStateOf("") }
  var durationDays by remember { mutableIntStateOf(60) }
  var subscriptionFormError by remember { mutableStateOf<String?>(null) }
  var isSavingSubscription by remember { mutableStateOf(false) }
  var selectedAdForApproval by remember { mutableStateOf<UsedCatalyticAd?>(null) }
  var isApprovingAd by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(rememberScrollState())
  ) {
    Text("إدارة إعلانات الدبات المستعملة", fontWeight = FontWeight.Bold, fontSize = 17.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      "سجّل اشتراك كل عميل حسب الاتفاق: عدد المنتجات والسعر والمدة. يستطيع العميل التحقق من حالته برقم الجوال.",
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(12.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text("إضافة أو تجديد اشتراك عميل", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(sellerName, { sellerName = it }, label = { Text("اسم العميل") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(sellerPhone, { sellerPhone = it }, label = { Text("رقم جوال العميل") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
          value = productLimitInput,
          onValueChange = { productLimitInput = it },
          label = { Text("عدد المنتجات المسموح بعرضها") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = agreedPriceInput,
          onValueChange = { agreedPriceInput = it },
          label = { Text("السعر المتفق عليه (ر.س)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Text("مدة الاشتراك", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(60, 90).forEach { days ->
            Button(
              onClick = { durationDays = days },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (durationDays == days) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
              )
            ) {
              Text(
                "$days يوماً",
                color = if (durationDays == days) Color.White else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
        subscriptionFormError?.let {
          Spacer(modifier = Modifier.height(6.dp))
          Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
          onClick = {
            val productLimit = productLimitInput.toIntOrNull()
            val agreedPrice = agreedPriceInput.toDoubleOrNull()
            if (
              sellerName.isBlank() || sellerPhone.isBlank() || productLimit == null || productLimit <= 0 ||
              agreedPrice == null || !agreedPrice.isFinite() || agreedPrice <= 0.0
            ) {
              subscriptionFormError = "أدخل اسم العميل ورقم جواله وعدداً وسعراً صحيحين."
            } else {
              subscriptionFormError = null
              isSavingSubscription = true
              onSaveSubscription(
                UsedAdCustomerSubscription(
                  sellerName = sellerName.trim(),
                  sellerPhone = sellerPhone.trim(),
                  productLimit = productLimit,
                  agreedPrice = agreedPrice,
                  durationDays = durationDays
                )
              ) { saveError ->
                isSavingSubscription = false
                Toast.makeText(
                  context,
                  saveError ?: "تم تفعيل اشتراك العميل",
                  Toast.LENGTH_SHORT
                ).show()
              }
            }
          },
          enabled = !isSavingSubscription,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(if (isSavingSubscription) "جارٍ الحفظ..." else "حفظ وتفعيل الاشتراك")
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text("اشتراكات العملاء (${subscriptions.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
    subscriptionsError?.let {
      Spacer(modifier = Modifier.height(6.dp))
      Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
    }
    if (subscriptionsLoading) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
    } else if (subscriptions.isEmpty()) {
      Spacer(modifier = Modifier.height(8.dp))
      Text("لا توجد اشتراكات مسجلة.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    subscriptions.forEach { subscription ->
      Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text("${subscription.sellerName} • ${subscription.sellerPhone}", fontWeight = FontWeight.Bold)
          Text(
            "الحالة: ${subscription.status.labelArabic} • المنتجات: ${subscription.submittedProductCount}/${subscription.productLimit}",
            fontSize = 12.sp
          )
          Text(
            "السعر المتفق عليه: ${subscription.agreedPrice} ر.س • المدة: ${subscription.durationDays} يوماً",
            fontSize = 12.sp
          )
          if (subscription.expiresAt > 0L) {
            Text("ينتهي في: ${formatUsedAdExpiry(subscription.expiresAt)}", fontSize = 12.sp)
          }
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            UsedAdSubscriptionStatus.values().forEach { status ->
              TextButton(
                onClick = {
                  onUpdateSubscriptionStatus(subscription, status) { updateError ->
                    Toast.makeText(context, updateError ?: "تم تحديث حالة الاشتراك", Toast.LENGTH_SHORT).show()
                  }
                },
                enabled = status != subscription.status
              ) {
                Text(status.labelArabic, fontSize = 11.sp)
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text("طلبات الإعلانات (${ads.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
    error?.let {
      Spacer(modifier = Modifier.height(6.dp))
      Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
    }
    if (isLoading) {
      Spacer(modifier = Modifier.height(12.dp))
      CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
    } else if (ads.isEmpty()) {
      Spacer(modifier = Modifier.height(8.dp))
      Text("لا توجد طلبات إعلانات حالياً.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    ads.forEach { ad ->
      val adSubscription = subscriptions.firstOrNull {
        it.id == ad.subscriptionId || it.sellerPhone == ad.sellerPhone
      }
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          if (ad.imageUrl.isNotBlank()) {
            AsyncImage(
              model = ad.imageUrl,
              contentDescription = "صورة الدبة المعروضة",
              modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
          }
          Text("${ad.sellerName} • ${ad.sellerPhone}", fontWeight = FontWeight.Bold)
          Text("${ad.city} • ${ad.vehicleType} ${ad.vehicleModel}", fontSize = 12.sp)
          Text("الحالة: ${ad.catalyticCondition.labelArabic} • السعر: ${ad.price} ر.س", fontSize = 12.sp)
          adSubscription?.let {
            Text(
              "اشتراك البائع: ${it.status.labelArabic} • المنتجات: ${it.submittedProductCount}/${it.productLimit} • ${it.agreedPrice} ر.س",
              fontSize = 12.sp
            )
          }
          Text("حالة الإعلان: ${ad.status.labelArabic}", fontSize = 12.sp)
          if (ad.expiresAt > 0L) {
            Text("ينتهي في: ${formatUsedAdExpiry(ad.expiresAt)}", fontSize = 12.sp)
          }
          if (ad.status == UsedCatalyticAdStatus.PENDING_PAYMENT) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
              onClick = { selectedAdForApproval = ad },
              enabled = !isApprovingAd,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("مراجعة واعتماد المنتج")
            }
          }
        }
      }
    }
  }

  selectedAdForApproval?.let { ad ->
    val sellerSubscription = subscriptions.firstOrNull {
      it.id == ad.subscriptionId || it.sellerPhone == ad.sellerPhone
    }
    AlertDialog(
      onDismissRequest = {
        if (!isApprovingAd) selectedAdForApproval = null
      },
      title = { Text("تفعيل إعلان الدبة؟") },
      text = {
        Text(
          "بعد مراجعة بيانات المنتج${sellerSubscription?.let { " ضمن اشتراك ${it.sellerName} (${it.agreedPrice} ر.س)" } ?: ""} سيظهر الإعلان للمدة المتبقية من الاشتراك."
        )
      },
      confirmButton = {
        TextButton(
          enabled = !isApprovingAd,
          onClick = {
            isApprovingAd = true
            onApproveAd(ad) { approvalError ->
              isApprovingAd = false
              selectedAdForApproval = null
              Toast.makeText(
                context,
                approvalError ?: "تم اعتماد المنتج",
                Toast.LENGTH_LONG
              ).show()
            }
          }
        ) {
          Text("تأكيد التفعيل")
        }
      },
      dismissButton = {
        TextButton(
          enabled = !isApprovingAd,
          onClick = { selectedAdForApproval = null }
        ) {
          Text("إلغاء")
        }
      }
    )
  }
}

@Composable
private fun CommercialStoreAdsAdminTab(
  ads: List<CommercialStoreAd>,
  isLoading: Boolean,
  error: String?,
  onAddAd: (CommercialStoreAd, (String?) -> Unit) -> Unit,
  onApproveOrRenewAd: (CommercialStoreAd, (String?) -> Unit) -> Unit,
  onExpireAd: (CommercialStoreAd, (String?) -> Unit) -> Unit
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
  var durationInput by remember { mutableStateOf("30") }
  var durationUnit by remember { mutableStateOf(CommercialAdDurationUnit.DAYS) }
  var formError by remember { mutableStateOf<String?>(null) }
  var isAdding by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(rememberScrollState())
  ) {
    Text("إدارة إعلانات المحلات التجارية والورش", fontWeight = FontWeight.Bold, fontSize = 17.sp)
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      "يمكنك إضافة إعلان نشط مباشرة، أو مراجعة طلبات العملاء وتفعيلها بعد الاتفاق على الدفع.",
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(12.dp))

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text("إضافة محل أو ورشة مباشرة", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(storeName, { storeName = it }, label = { Text("اسم المحل") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(goodsType, { goodsType = it }, label = { Text("نوع السلع المعروضة") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(region, { region = it }, label = { Text("المنطقة") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(city, { city = it }, label = { Text("المدينة") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(locationUrl, { locationUrl = it }, label = { Text("رابط الموقع في خرائط جوجل") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(contactPhone, { contactPhone = it }, label = { Text("رقم التواصل/واتساب") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(image1, { image1 = it }, label = { Text("رابط الصورة الأولى") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(image2, { image2 = it }, label = { Text("رابط الصورة الثانية") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(image3, { image3 = it }, label = { Text("رابط الصورة الثالثة") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(image4, { image4 = it }, label = { Text("رابط الصورة الرابعة") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(videoUrl, { videoUrl = it }, label = { Text("رابط مقطع الفيديو") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
          value = durationInput,
          onValueChange = { durationInput = it },
          label = { Text("مدة الإعلان") },
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
        Spacer(modifier = Modifier.height(8.dp))
        Button(
          onClick = {
            val duration = durationInput.toIntOrNull()
            if (
              storeName.isBlank() || goodsType.isBlank() || region.isBlank() || city.isBlank() ||
              locationUrl.isBlank() || contactPhone.isBlank() || duration == null || duration <= 0
            ) {
              formError = "أكمل البيانات الأساسية وأدخل مدة إعلان صحيحة."
            } else {
              formError = null
              isAdding = true
              onAddAd(
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
                  durationValue = duration,
                  durationUnit = durationUnit
                )
              ) { saveError ->
                isAdding = false
                Toast.makeText(context, saveError ?: "تم إضافة المحل وتفعيل إعلانه", Toast.LENGTH_SHORT).show()
                if (saveError == null) {
                  storeName = ""
                  goodsType = ""
                  region = ""
                  city = ""
                  locationUrl = ""
                  contactPhone = ""
                  image1 = ""
                  image2 = ""
                  image3 = ""
                  image4 = ""
                  videoUrl = ""
                }
              }
            }
          },
          enabled = !isAdding,
          modifier = Modifier.fillMaxWidth()
        ) { Text(if (isAdding) "جارٍ الإضافة..." else "إضافة وتفعيل الإعلان") }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text("الطلبات والإعلانات (${ads.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
    error?.let {
      Spacer(modifier = Modifier.height(6.dp))
      Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
    }
    if (isLoading) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
    } else if (ads.isEmpty()) {
      Spacer(modifier = Modifier.height(8.dp))
      Text("لا توجد طلبات أو إعلانات محلات حالياً.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    ads.forEach { ad ->
      CommercialStoreAdAdminCard(ad, onApproveOrRenewAd, onExpireAd)
    }
  }
}

@Composable
private fun CommercialStoreAdAdminCard(
  ad: CommercialStoreAd,
  onApproveOrRenewAd: (CommercialStoreAd, (String?) -> Unit) -> Unit,
  onExpireAd: (CommercialStoreAd, (String?) -> Unit) -> Unit
) {
  val context = LocalContext.current
  var durationInput by remember(ad.id, ad.durationValue) { mutableStateOf(ad.durationValue.toString()) }
  var durationUnit by remember(ad.id, ad.durationUnit) { mutableStateOf(ad.durationUnit) }
  var isUpdating by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(ad.storeName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Text("${ad.goodsType} • ${ad.region} • ${ad.city}", fontSize = 12.sp)
      Text("التواصل: ${ad.contactPhone}", fontSize = 12.sp)
      if (ad.imageUrls.isNotEmpty()) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 8.dp)) {
          ad.imageUrls.take(4).forEach { imageUrl ->
            AsyncImage(
              model = imageUrl,
              contentDescription = "صورة ${ad.storeName}",
              modifier = Modifier.size(64.dp).clip(RoundedCornerShape(6.dp))
            )
          }
        }
      }
      if (ad.videoUrl.isNotBlank()) Text("يتضمن مقطع فيديو", fontSize = 12.sp)
      Text("الحالة: ${ad.status.labelArabic}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
      if (ad.expiryTimestamp > 0L) {
        Text("تاريخ الانتهاء: ${formatUsedAdExpiry(ad.expiryTimestamp)}", fontSize = 12.sp)
      }
      OutlinedTextField(
        value = durationInput,
        onValueChange = { durationInput = it },
        label = { Text("مدة التفعيل أو التجديد") },
        singleLine = true,
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
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Button(
          onClick = {
            val duration = durationInput.toIntOrNull()
            if (duration == null || duration <= 0) {
              Toast.makeText(context, "أدخل مدة صحيحة أكبر من صفر", Toast.LENGTH_SHORT).show()
            } else {
              isUpdating = true
              onApproveOrRenewAd(ad.copy(durationValue = duration, durationUnit = durationUnit)) { updateError ->
                isUpdating = false
                Toast.makeText(
                  context,
                  updateError ?: if (ad.status == CommercialStoreAdStatus.ACTIVE) "تم تجديد الإعلان" else "تم تفعيل الإعلان",
                  Toast.LENGTH_SHORT
                ).show()
              }
            }
          },
          enabled = !isUpdating,
          modifier = Modifier.weight(1f)
        ) {
          Text(
            when (ad.status) {
              CommercialStoreAdStatus.PENDING_PAYMENT -> "موافقة وتفعيل"
              CommercialStoreAdStatus.ACTIVE -> "حفظ المدة وتجديد"
              CommercialStoreAdStatus.EXPIRED -> "إعادة التفعيل"
            }
          )
        }
        if (ad.status == CommercialStoreAdStatus.ACTIVE) {
          Button(
            onClick = {
              isUpdating = true
              onExpireAd(ad) { updateError ->
                isUpdating = false
                Toast.makeText(context, updateError ?: "تم إنهاء الإعلان", Toast.LENGTH_SHORT).show()
              }
            },
            enabled = !isUpdating,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
          ) { Text("إنهاء الإعلان") }
        }
      }
    }
  }
}

private fun formatUsedAdExpiry(timestamp: Long): String =
  SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(timestamp))

@Composable
private fun OwnerWalletTab(
  wallet: OwnerWallet,
  transactions: List<OwnerWalletTransaction>,
  isLoading: Boolean,
  error: String?,
  onSaveBankDetails: (String, String, (String?) -> Unit) -> Unit,
  onAddTransaction: (OwnerWalletTransactionType, String, String, Long, (String?) -> Unit) -> Unit
) {
  val context = LocalContext.current
  var bankName by remember(wallet.bankName) { mutableStateOf(wallet.bankName) }
  var iban by remember(wallet.iban) { mutableStateOf(wallet.iban) }
  var depositAmount by remember { mutableStateOf("") }
  var depositReference by remember { mutableStateOf("") }
  var purchaseAmount by remember { mutableStateOf("") }
  var purchaseOrderNumber by remember { mutableStateOf("") }
  var withdrawalAmount by remember { mutableStateOf("") }
  var withdrawalReference by remember { mutableStateOf("") }
  var selectedStore by remember { mutableStateOf("AliExpress") }
  var storeMenuExpanded by remember { mutableStateOf(false) }
  var isSavingBank by remember { mutableStateOf(false) }
  var isSavingTransaction by remember { mutableStateOf(false) }

  fun submitTransaction(
    type: OwnerWalletTransactionType,
    storeName: String,
    reference: String,
    amountText: String,
    clearInputs: () -> Unit
  ) {
    val amountHalalas = parseOwnerWalletAmountHalalas(amountText)
    if (amountHalalas == null || amountHalalas <= 0L) {
      Toast.makeText(context, "أدخل مبلغاً صحيحاً بالريال (حتى منزلتين عشريتين)", Toast.LENGTH_LONG).show()
      return
    }
    isSavingTransaction = true
    onAddTransaction(type, storeName, reference, amountHalalas) { transactionError ->
      isSavingTransaction = false
      if (transactionError == null) clearInputs()
      Toast.makeText(
        context,
        transactionError ?: "تم تسجيل العملية وتحديث الرصيد",
        Toast.LENGTH_LONG
      ).show()
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text("محفظتي وخزينة المتجر (SAR)", fontWeight = FontWeight.Bold, fontSize = 19.sp)
    Text(
      "سجل مالي خاص بالمالك؛ تُسجل العمليات والرصيد في Firestore بشكل فوري.",
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
    if (isLoading) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
    }

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("الرصيد المتاح", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(
          formatOwnerWalletSar(wallet.balanceHalalas),
          fontSize = 28.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        HorizontalDivider()
        Text("إجمالي الأرباح: ${formatOwnerWalletSar(wallet.totalEarningsHalalas)}", fontSize = 13.sp)
        Text("إجمالي المشتريات: ${formatOwnerWalletSar(wallet.totalPurchasesHalalas)}", fontSize = 13.sp)
        Text("إجمالي السحوبات: ${formatOwnerWalletSar(wallet.totalWithdrawalsHalalas)}", fontSize = 13.sp)
      }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text("الحساب البنكي الشخصي", fontWeight = FontWeight.Bold)
        OutlinedTextField(
          value = bankName,
          onValueChange = { bankName = it },
          label = { Text("اسم البنك") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = iban,
          onValueChange = { iban = it },
          label = { Text("رقم الآيبان (IBAN)") },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Button(
          enabled = !isSavingBank && !isSavingTransaction,
          onClick = {
            isSavingBank = true
            onSaveBankDetails(bankName, iban) { saveError ->
              isSavingBank = false
              Toast.makeText(context, saveError ?: "تم حفظ بيانات الحساب البنكي", Toast.LENGTH_LONG).show()
            }
          },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(if (isSavingBank) "جارٍ الحفظ..." else "حفظ بيانات البنك والآيبان")
        }
      }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("إضافة أرباح / إيداع", fontWeight = FontWeight.Bold)
        OutlinedTextField(
          value = depositAmount,
          onValueChange = { depositAmount = it },
          label = { Text("المبلغ بالريال السعودي") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = depositReference,
          onValueChange = { depositReference = it },
          label = { Text("رقم الطلب أو مرجع العملية (اختياري)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Button(
          enabled = !isSavingTransaction && !isSavingBank,
          onClick = {
            submitTransaction(
              OwnerWalletTransactionType.PROFIT_DEPOSIT,
              "",
              depositReference,
              depositAmount
            ) {
              depositAmount = ""
              depositReference = ""
            }
          },
          modifier = Modifier.fillMaxWidth()
        ) { Text("إضافة أرباح / إيداع") }
      }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("تسجيل شراء من متجر", fontWeight = FontWeight.Bold)
        Box {
          Button(onClick = { storeMenuExpanded = true }) {
            Text("المتجر: $selectedStore")
          }
          DropdownMenu(
            expanded = storeMenuExpanded,
            onDismissRequest = { storeMenuExpanded = false }
          ) {
            listOf("AliExpress", "Alibaba", "Amazon").forEach { store ->
              DropdownMenuItem(
                text = { Text(store) },
                onClick = {
                  selectedStore = store
                  storeMenuExpanded = false
                }
              )
            }
          }
        }
        OutlinedTextField(
          value = purchaseOrderNumber,
          onValueChange = { purchaseOrderNumber = it },
          label = { Text("رقم طلب الشراء") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = purchaseAmount,
          onValueChange = { purchaseAmount = it },
          label = { Text("قيمة الفاتورة بالريال السعودي") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Button(
          enabled = !isSavingTransaction && !isSavingBank,
          onClick = {
            submitTransaction(
              OwnerWalletTransactionType.INVENTORY_PURCHASE,
              selectedStore,
              purchaseOrderNumber,
              purchaseAmount
            ) {
              purchaseAmount = ""
              purchaseOrderNumber = ""
            }
          },
          modifier = Modifier.fillMaxWidth()
        ) { Text("تسجيل الشراء وخصم المبلغ") }
      }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("سحب أرباح لحسابي الشخصي", fontWeight = FontWeight.Bold)
        Text(
          if (wallet.bankName.isBlank() || wallet.iban.isBlank()) {
            "احفظ بيانات البنك والآيبان قبل تسجيل السحب."
          } else {
            "التحويل إلى ${wallet.bankName} • الآيبان محفوظ بشكل مخفي."
          },
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
          value = withdrawalAmount,
          onValueChange = { withdrawalAmount = it },
          label = { Text("المبلغ المحوّل بالريال السعودي") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = withdrawalReference,
          onValueChange = { withdrawalReference = it },
          label = { Text("رقم الحوالة أو مرجعها (اختياري)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Button(
          enabled = !isSavingTransaction && !isSavingBank &&
            wallet.bankName.isNotBlank() && wallet.iban.isNotBlank(),
          onClick = {
            submitTransaction(
              OwnerWalletTransactionType.PERSONAL_WITHDRAWAL,
              "",
              withdrawalReference,
              withdrawalAmount
            ) {
              withdrawalAmount = ""
              withdrawalReference = ""
            }
          },
          modifier = Modifier.fillMaxWidth()
        ) { Text("سحب أرباح لحسابي الشخصي") }
      }
    }

    Text("فتح المتاجر", fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
      listOf(
        "AliExpress" to "https://www.aliexpress.com",
        "Alibaba" to "https://www.alibaba.com",
        "Amazon" to "https://www.amazon.sa"
      ).forEach { (name, url) ->
        Button(
          onClick = { openOwnerWalletStore(context, url) },
          modifier = Modifier.weight(1f),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
        ) {
          Text(name, fontSize = 10.sp, maxLines = 1)
        }
      }
    }

    Text("سجل العمليات", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    if (!isLoading && transactions.isEmpty()) {
      Text("لا توجد عمليات مسجلة.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
    transactions.forEach { transaction ->
      val isCredit = transaction.type == OwnerWalletTransactionType.PROFIT_DEPOSIT
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
          Text(transaction.type.labelArabic, fontWeight = FontWeight.Bold)
          if (transaction.storeName.isNotBlank()) Text("المتجر: ${transaction.storeName}", fontSize = 12.sp)
          if (transaction.orderNumber.isNotBlank()) Text("رقم الطلب/المرجع: ${transaction.orderNumber}", fontSize = 12.sp)
          Text(
            "${if (isCredit) "+" else "−"}${formatOwnerWalletSar(transaction.amountHalalas)}",
            color = if (isCredit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
          )
          Text(
            SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(transaction.createdAt)),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

private fun parseOwnerWalletAmountHalalas(value: String): Long? =
  try {
    BigDecimal(value.trim().replace(',', '.'))
      .setScale(2, RoundingMode.UNNECESSARY)
      .movePointRight(2)
      .longValueExact()
  } catch (_: NumberFormatException) {
    null
  } catch (_: ArithmeticException) {
    null
  }

private fun formatOwnerWalletSar(amountHalalas: Long): String =
  String.format(Locale.US, "%,.2f ر.س", amountHalalas / 100.0)

private fun openOwnerWalletStore(context: Context, url: String) {
  try {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
  } catch (_: ActivityNotFoundException) {
    Toast.makeText(context, "تعذر فتح المتصفح", Toast.LENGTH_LONG).show()
  }
}

@Composable
private fun VisitorStatsCard(
  stats: VisitorStats,
  error: String?,
  onReset: ((String?) -> Unit) -> Unit
) {
  val context = LocalContext.current
  var showResetConfirmation by remember { mutableStateOf(false) }
  var isResetting by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(
        "إحصائيات زوار التطبيق",
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimaryContainer
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text("إجمالي الزيارات", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
          Text(stats.totalVisitors.toString(), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        }
        Column {
          Text("زيارات اليوم", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
          Text(stats.todayVisitors.toString(), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        }
        Button(
          onClick = { showResetConfirmation = true },
          enabled = !isResetting
        ) {
          Text(if (isResetting) "جارٍ التصفير..." else "تصفير")
        }
      }
      error?.let {
        Spacer(modifier = Modifier.height(6.dp))
        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
      }
    }
  }

  if (showResetConfirmation) {
    AlertDialog(
      onDismissRequest = { showResetConfirmation = false },
      title = { Text("تصفير إحصائيات الزوار؟") },
      text = { Text("سيتم تصفير الإجمالي وعدد زيارات اليوم لجميع مستخدمي التطبيق.") },
      confirmButton = {
        TextButton(
          enabled = !isResetting,
          onClick = {
            isResetting = true
            onReset { resetError ->
              isResetting = false
              showResetConfirmation = false
              Toast.makeText(
                context,
                resetError ?: "تم تصفير إحصائيات الزوار",
                Toast.LENGTH_LONG
              ).show()
            }
          }
        ) {
          Text("تصفير العداد")
        }
      },
      dismissButton = {
        TextButton(
          onClick = { showResetConfirmation = false },
          enabled = !isResetting
        ) {
          Text("إلغاء")
        }
      }
    )
  }
}

@Composable
private fun OrdersManagementTab(
  orders: List<Order>,
  isLoading: Boolean,
  syncError: String?,
  onUpdateOrder: (Order, (String?) -> Unit) -> Unit
) {
  val context = LocalContext.current

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(rememberScrollState())
  ) {
    Text("إدارة وتحديث الطلبات", fontWeight = FontWeight.Bold, fontSize = 17.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      "تتحدث القائمة تلقائياً من Firestore. اختر الحالة وأضف ملاحظة ثم احفظ لإيصال التحديث للعميل مباشرة.",
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    syncError?.let {
      Spacer(modifier = Modifier.height(8.dp))
      Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
    }
    if (isLoading) {
      Spacer(modifier = Modifier.height(16.dp))
      CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
    } else if (orders.isEmpty()) {
      Spacer(modifier = Modifier.height(20.dp))
      Text("لا توجد طلبات حتى الآن.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    orders.forEach { order ->
      var selectedStatus by remember(order.orderNumber, order.status) { mutableStateOf(order.status) }
      var note by remember(order.orderNumber, order.updateNote) { mutableStateOf(order.updateNote) }
      var statusMenuExpanded by remember(order.orderNumber) { mutableStateOf(false) }
      var isSaving by remember(order.orderNumber) { mutableStateOf(false) }

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("طلب رقم ${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            "العميل: ${order.customerName} • ${order.customerContact}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          order.products.forEach { product ->
            Spacer(modifier = Modifier.height(6.dp))
            Text("${product.productName} • ${product.unitPrice.toInt()} ر.س", fontSize = 13.sp)
            if (product.selectedOptions.isNotEmpty()) {
              Text(
                product.selectedOptions.joinToString(" • "),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            if (product.customerNote.isNotBlank()) {
              Text(
                "ملاحظة العميل: ${product.customerNote}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Box {
            Button(onClick = { statusMenuExpanded = true }) {
              Text("الحالة: ${selectedStatus.labelArabic}")
            }
            DropdownMenu(
              expanded = statusMenuExpanded,
              onDismissRequest = { statusMenuExpanded = false }
            ) {
              OrderStatus.values().forEach { status ->
                DropdownMenuItem(
                  text = { Text(status.labelArabic) },
                  onClick = {
                    selectedStatus = status
                    statusMenuExpanded = false
                  }
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("ملاحظة للعميل") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          Button(
            onClick = {
              isSaving = true
              onUpdateOrder(order.copy(status = selectedStatus, updateNote = note)) { error ->
                isSaving = false
                Toast.makeText(
                  context,
                  error ?: "تم تحديث الطلب وإشعار العميل بالحالة الجديدة",
                  Toast.LENGTH_LONG
                ).show()
              }
            },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
          ) {
            if (isSaving) {
              CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
              Text("حفظ تحديث الطلب")
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ComplaintsTab(complaints: List<Complaint>) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(rememberScrollState())
  ) {
    Text(text = "صندوق رسائل وشكاوى واقتراحات العملاء (${complaints.size}):", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    Spacer(modifier = Modifier.height(10.dp))

    if (complaints.isEmpty()) {
      Text(text = "لا توجد رسائل واردة حالياً.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
      complaints.forEach { c ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
          shape = RoundedCornerShape(10.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "${c.customerName} (${c.type})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
              Text(text = c.customerContact, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = c.content, fontSize = 13.sp, lineHeight = 19.sp)
          }
        }
      }
    }
  }
}
