package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.repository.StoreRepository
import com.example.ui.components.CustomerAuthDialog
import com.example.ui.components.CommercialStoreAdsSection
import com.example.ui.components.FooterSection
import com.example.ui.components.MarqueeAnnouncementBar
import com.example.ui.components.OwnerAdminPortal
import com.example.ui.components.OrderTrackingDialog
import com.example.ui.components.ProductCard
import com.example.ui.components.ProductDetailDialog
import com.example.ui.components.VideoBannerCard
import com.example.ui.components.UsedCatalyticAdsSection
import com.example.ui.theme.SmartStoreTheme
import com.example.ui.theme.StoreThemePalette
import com.example.service.shareStore
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    com.example.util.configureAppCheck(this, intent)
    enableEdgeToEdge()

    val repository = StoreRepository.getInstance(applicationContext)
    repository.recordAppVisit()

    setContent {
      val settings by repository.storeSettings.collectAsState()
      val products by repository.products.collectAsState()
      val complaints by repository.complaints.collectAsState()
      val currentUser by repository.currentUser.collectAsState()
      val orders by repository.orders.collectAsState()
      val ordersLoading by repository.ordersLoading.collectAsState()
      val ordersSyncError by repository.ordersSyncError.collectAsState()
      val visitorStats by repository.visitorStats.collectAsState()
      val visitorStatsError by repository.visitorStatsError.collectAsState()

      val activePalette = remember(settings.activeTheme) {
        try {
          StoreThemePalette.valueOf(settings.activeTheme)
        } catch (_: Exception) {
          StoreThemePalette.GOLD
        }
      }

      SmartStoreTheme(themePalette = activePalette) {
        // Enforce RTL direction for Arabic store
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          StoreMainScreen(
            repository = repository,
            settings = settings,
            products = products,
            complaints = complaints,
            currentUser = currentUser,
            orders = orders,
            ordersLoading = ordersLoading,
            ordersSyncError = ordersSyncError,
            visitorStats = visitorStats,
            visitorStatsError = visitorStatsError
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StoreMainScreen(
  repository: StoreRepository,
  settings: com.example.data.model.StoreSettings,
  products: List<Product>,
  complaints: List<com.example.data.model.Complaint>,
  currentUser: com.example.data.model.CustomerUser,
  orders: List<com.example.data.model.Order>,
  ordersLoading: Boolean,
  ordersSyncError: String?,
  visitorStats: com.example.data.model.VisitorStats,
  visitorStatsError: String?
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val activeUsedAds by repository.activeUsedCatalyticAds.collectAsState()
  val activeUsedAdsLoading by repository.activeUsedAdsLoading.collectAsState()
  val activeUsedAdsError by repository.activeUsedAdsError.collectAsState()
  val usedCatalyticAds by repository.usedCatalyticAds.collectAsState()
  val usedAdsLoading by repository.usedAdsLoading.collectAsState()
  val usedAdsError by repository.usedAdsError.collectAsState()
  val usedAdCustomerSubscriptions by repository.usedAdCustomerSubscriptions.collectAsState()
  val usedAdSubscriptionsLoading by repository.usedAdSubscriptionsLoading.collectAsState()
  val usedAdSubscriptionsError by repository.usedAdSubscriptionsError.collectAsState()
  val activeCommercialStoreAds by repository.activeCommercialStoreAds.collectAsState()
  val commercialStoreAdsLoading by repository.commercialStoreAdsLoading.collectAsState()
  val activeCommercialStoreAdsError by repository.activeCommercialStoreAdsError.collectAsState()
  val commercialStoreAds by repository.commercialStoreAds.collectAsState()
  val commercialStoreAdsAdminLoading by repository.commercialStoreAdsAdminLoading.collectAsState()
  val commercialStoreAdsAdminError by repository.commercialStoreAdsAdminError.collectAsState()
  val ownerWallet by repository.ownerWallet.collectAsState()
  val ownerWalletTransactions by repository.ownerWalletTransactions.collectAsState()
  val ownerWalletLoading by repository.ownerWalletLoading.collectAsState()
  val ownerWalletError by repository.ownerWalletError.collectAsState()

  var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
  var showOwnerPortal by remember { mutableStateOf(false) }
  var showCustomerAuth by remember { mutableStateOf(false) }
  var showOrderTracking by remember { mutableStateOf(false) }

  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("الكل") }

  // Filtered products list
  val filteredProducts = remember(products, searchQuery, selectedCategory) {
    products.filter { prod ->
      val matchesCategory = selectedCategory == "الكل" || prod.category.contains(selectedCategory) || (selectedCategory == "ديزل" && prod.title.contains("ديزل")) || (selectedCategory == "بنزين" && prod.title.contains("بنزين"))
      val matchesSearch = searchQuery.isBlank() || prod.title.contains(searchQuery, ignoreCase = true) || prod.description.contains(searchQuery, ignoreCase = true)
      matchesCategory && matchesSearch
    }
  }

  val categories = listOf("الكل", "دبات بنزين", "دبات ديزل", "دبات رياضية", "إلكترونيات وفحص")

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("store_main_screen"),
    topBar = {
      Column {
        // 1. Dynamic Moving Marquee Announcement Bar with speed control
        MarqueeAnnouncementBar(
          text = settings.marqueeText,
          speed = settings.marqueeSpeed,
          bgColorHex = settings.marqueeBgColorHex
        )

        // 2. Main Store Top Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = "شعار المتجر",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = settings.storeName,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
              )
              Text(
                text = "الطلب المباشر عبر واتساب: ${settings.whatsappNumber}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          // User / Owner Actions
          Row(verticalAlignment = Alignment.CenterVertically) {
            // Customer Login button
            IconButton(
              onClick = { showCustomerAuth = true },
              modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
              Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "دخول العميل",
                tint = if (currentUser.isLoggedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
              onClick = { shareStore(context, settings.storeName, settings.storeShareLink) },
              modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .testTag("share_store_top_bar_button")
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "مشاركة المتجر",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
              )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Owner Portal Access
            IconButton(
              onClick = { showOwnerPortal = true },
              modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                .testTag("admin_icon_button")
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "لوحة المالك والذكاء الاصطناعي",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      if (settings.mainBackgroundImage.isNotBlank()) {
        AsyncImage(
          model = settings.mainBackgroundImage,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          alpha = 0.35f,
          modifier = Modifier.fillMaxSize()
        )
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f))
        )
      }

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
      ) {
      item {
        Button(
          onClick = { showOrderTracking = true },
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("order_tracking_button")
        ) {
          Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("تتبع طلباتي")
          }
      }

      // Header Video Section
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
          VideoBannerCard(
            title = settings.headerVideoTitle,
            description = settings.headerVideoDesc,
            badgeText = "فيديو توضيحي لرأس الصفحة",
            placeholderDrawableName = "hero_store_banner_1794448546017",
            videoSource = settings.headerVideoUrl,
            backgroundImageSource = settings.headerVideoBackground,
            testTag = "header_video_card"
          )
        }
      }

      // Search & Filters Section
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
          // Search Box
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث عن دبة تلوث أو فلتر ديزل أو نوع سيارتك...") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(imageVector = Icons.Default.Close, contentDescription = "مسح")
                }
              }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Category Chips Flow
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            categories.forEach { cat ->
              val isSelected = selectedCategory == cat
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { selectedCategory = cat }
              ) {
                Text(
                  text = cat,
                  color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 12.sp,
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Section title for customer focus
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "المنتجات المتوفرة للطلب الفوري (${filteredProducts.size})",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 17.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "اختر المواصفات واطلب عبر الواتساب",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }

      item {
        UsedCatalyticAdsSection(
          ads = activeUsedAds,
          isLoading = activeUsedAdsLoading,
          error = activeUsedAdsError,
          whatsappNumber = settings.whatsappNumber,
          onCheckSubscription = { phone, onComplete ->
            repository.checkUsedAdCustomerSubscription(phone, onComplete)
          },
          onSubmitAd = { ad, onComplete -> repository.submitUsedCatalyticAd(ad, onComplete) }
        )
      }

      item {
        CommercialStoreAdsSection(
          ads = activeCommercialStoreAds,
          isLoading = commercialStoreAdsLoading,
          error = activeCommercialStoreAdsError,
          onSubmitAd = { ad, onComplete -> repository.submitCommercialStoreAd(ad, onComplete) }
        )
      }

      // Products List Items
      if (filteredProducts.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(40.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "لا توجد نتائج تطابق بحثك حالياً.",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 14.sp
            )
          }
        }
      } else {
        items(filteredProducts, key = { it.id }) { product ->
          Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            ProductCard(
              product = product,
              storeName = settings.storeName,
              storeShareLink = settings.storeShareLink,
              onClick = { selectedProductForDetail = product }
            )
          }
        }
      }

      // Footer Video Section (Customer Reviews / Dyno Sound test)
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
          Text(
            text = "فيديو أسفل الصفحة (تجارب الفحص والتركيب):",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
          )
          VideoBannerCard(
            title = settings.footerVideoTitle,
            description = settings.footerVideoDesc,
            badgeText = "تجارب حقيقية واختبار الفحص",
            placeholderDrawableName = "hero_store_banner_1794448546017",
            videoSource = settings.footerVideoUrl,
            backgroundImageSource = settings.footerVideoBackground,
            testTag = "footer_video_card"
          )
        }
      }

      // Complete Footer (Socials, About us, Complaints, Policies, Email)
      item {
        FooterSection(
          settings = settings,
          onOpenOwnerPortal = { showOwnerPortal = true },
          onSubmitComplaint = { name, contact, type, content ->
            repository.submitComplaint(name, contact, type, content)
          }
        )
      }
    }
  }
  }

  // Product Detail & 4 Options WhatsApp Order Dialog
  if (selectedProductForDetail != null) {
    ProductDetailDialog(
      product = selectedProductForDetail!!,
      whatsappNumber = settings.whatsappNumber,
      customerName = currentUser.displayName,
      customerContact = currentUser.contactInfo,
      storeName = settings.storeName,
      storeShareLink = settings.storeShareLink,
      whatsappAlertsEnabled = settings.whatsappAlertsEnabled,
      onCreateOrder = { order, onComplete -> repository.createOrder(order, onComplete) },
      onNotifyOwner = { name, phone, summary, onComplete ->
        repository.sendOwnerWhatsAppLeadAlert(name, phone, summary, onComplete)
      },
      onDismiss = { selectedProductForDetail = null }
    )
  }

  // Customer Login Dialog
  if (showCustomerAuth) {
    CustomerAuthDialog(
      onLogin = { contact, name ->
        repository.loginCustomer(contact, name)
      },
      onDismiss = { showCustomerAuth = false }
    )
  }

  if (showOrderTracking) {
    OrderTrackingDialog(
      repository = repository,
      onDismiss = { showOrderTracking = false }
    )
  }

  // Owner Admin & AI Suite Dialog
  if (showOwnerPortal) {
    OwnerAdminPortal(
      isOwnerLoggedIn = currentUser.isOwner,
      settings = settings,
      products = products,
      complaints = complaints,
      orders = orders,
      ordersLoading = ordersLoading,
      ordersSyncError = ordersSyncError,
      visitorStats = visitorStats,
      visitorStatsError = visitorStatsError,
      usedCatalyticAds = usedCatalyticAds,
      usedAdsLoading = usedAdsLoading,
      usedAdsError = usedAdsError,
      usedAdCustomerSubscriptions = usedAdCustomerSubscriptions,
      usedAdSubscriptionsLoading = usedAdSubscriptionsLoading,
      usedAdSubscriptionsError = usedAdSubscriptionsError,
      commercialStoreAds = commercialStoreAds,
      commercialStoreAdsLoading = commercialStoreAdsAdminLoading,
      commercialStoreAdsError = commercialStoreAdsAdminError,
      ownerWallet = ownerWallet,
      ownerWalletTransactions = ownerWalletTransactions,
      ownerWalletLoading = ownerWalletLoading,
      ownerWalletError = ownerWalletError,
      onAuthenticateOwner = { email, password, createAccount, onComplete ->
        repository.authenticateOwner(email, password, createAccount, onComplete)
      },
      onVerifyOwnerCode = { challengeId, code, onComplete ->
        repository.verifyOwnerLoginCode(challengeId, code, onComplete)
      },
      onResetOwnerPassword = { email, onComplete ->
        repository.sendOwnerPasswordReset(email, onComplete)
      },
      onLogout = { repository.logout() },
      onUpdateSettings = { repository.updateStoreSettings(it) },
      onAddProduct = { repository.addProduct(it) },
      onUpdateProduct = { repository.updateProduct(it) },
      onDeleteProduct = { repository.deleteProduct(it) },
      onUpdateOrder = { order, onComplete -> repository.updateOrder(order, onComplete) },
      onResetVisitorCounts = { onComplete -> repository.resetVisitorCounts(onComplete) },
      onSaveUsedAdCustomerSubscription = { subscription, onComplete ->
        repository.saveUsedAdCustomerSubscription(subscription, onComplete)
      },
      onUpdateUsedAdCustomerSubscriptionStatus = { subscription, status, onComplete ->
        repository.updateUsedAdCustomerSubscriptionStatus(subscription, status, onComplete)
      },
      onApproveUsedCatalyticAd = { ad, onComplete ->
        repository.approveUsedCatalyticAd(ad, onComplete)
      },
      onAddCommercialStoreAd = { ad, onComplete ->
        repository.addCommercialStoreAdDirectly(ad, onComplete)
      },
      onApproveOrRenewCommercialStoreAd = { ad, onComplete ->
        repository.approveOrRenewCommercialStoreAd(ad, onComplete)
      },
      onExpireCommercialStoreAd = { ad, onComplete ->
        repository.expireCommercialStoreAd(ad, onComplete)
      },
      onSaveOwnerBankDetails = { bankName, iban, onComplete ->
        repository.saveOwnerBankDetails(bankName, iban, onComplete)
      },
      onAddOwnerWalletTransaction = { type, storeName, orderNumber, amountHalalas, onComplete ->
        repository.addOwnerWalletTransaction(
          type,
          storeName,
          orderNumber,
          amountHalalas,
          onComplete
        )
      },
      onImportUrl = { url -> repository.importProductFromUrl(url) },
      onDismiss = { showOwnerPortal = false }
    )
  }
}
