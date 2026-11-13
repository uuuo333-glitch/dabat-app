package com.example.data.repository

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import com.example.data.model.Complaint
import com.example.data.model.CommercialAdDurationUnit
import com.example.data.model.CommercialStoreAd
import com.example.data.model.CommercialStoreAdStatus
import com.example.data.model.CustomerUser
import com.example.data.model.MarqueeSpeed
import com.example.data.model.Order
import com.example.data.model.OrderProduct
import com.example.data.model.OrderStatus
import com.example.data.model.OwnerWallet
import com.example.data.model.OwnerWalletTransaction
import com.example.data.model.OwnerWalletTransactionType
import com.example.data.model.OwnerAuthenticationResult
import com.example.data.model.Product
import com.example.data.model.StoreSettings
import com.example.data.model.CatalyticCondition
import com.example.data.model.UsedCatalyticAd
import com.example.data.model.UsedCatalyticAdStatus
import com.example.data.model.UsedAdCustomerSubscription
import com.example.data.model.UsedAdSubscriptionStatus
import com.example.data.model.VisitorStats
import com.example.service.AiDescriptionGenerator
import com.example.service.UrlProductScraper
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import java.util.UUID

class StoreRepository private constructor(private val context: Context) {
  companion object {
    private const val TAG = "StoreRepository"
    private const val ORDERS_COLLECTION = "orders"
    private const val USED_ADS_COLLECTION = "usedCatalyticAds"
    private const val USED_AD_SUBSCRIPTIONS_COLLECTION = "usedAdCustomerSubscriptions"
    private const val COMMERCIAL_STORE_ADS_COLLECTION = "commercialStoreAds"
    private const val ANALYTICS_COLLECTION = "analytics"
    private const val VISITOR_STATS_DOCUMENT = "visitors"
    private const val OWNER_WALLET_COLLECTION = "ownerWallet"
    private const val OWNER_WALLET_DOCUMENT = "private"
    private const val OWNER_EMAIL = "uuuo333@gmail.com"
    private const val FIREBASE_FUNCTIONS_REGION = "us-central1"
    private val whatsappAlertHttpClient = OkHttpClient.Builder()
      .callTimeout(20, TimeUnit.SECONDS)
      .build()

    @Volatile
    private var INSTANCE: StoreRepository? = null

    fun getInstance(context: Context): StoreRepository {
      return INSTANCE ?: synchronized(this) {
        val instance = StoreRepository(context.applicationContext)
        INSTANCE = instance
        instance
      }
    }
  }

  private val settingsPreferences = context.getSharedPreferences("store_settings", Context.MODE_PRIVATE)

  // Initial products specialized in Catalytic Converters & DPF for Diesel and Petrol
  private val _products = MutableStateFlow<List<Product>>(
    listOf(
      Product(
        id = "cat-001",
        title = "دبة تلوث بنزين سيراميك أصلية يورو 5 (Euro 5 Catalytic Converter)",
        description = """
دبة رصاص وبيئة مطابقة للمواصفات القياسية الأوروبية Euro 5 للسيارات العاملة بالبنزين.
تقضي تماماً على رائحة الكربون الكريهة وتطفي لمبة المكينة (Check Engine P0420 / P0430).
مصنوعة من غلاف ستانلس ستيل معالج مع خلية سيراميك داخلية عالية الكثافة مغطاة بطبقات من البلاتينيوم والبلاديوم لتنقية الغازات واجتياز الفحص الدوري بنجاح 100%.
        """.trimIndent(),
        price = 1450.0,
        originalPrice = 1850.0,
        category = "دبات بنزين",
        rating = 4.9,
        reviewCount = 64,
        imageDrawableName = "prod_cat_converter_1794448832780",
        option1Label = "نوع الوقود والمحرك",
        option1Values = listOf("بنزين Petrol 4 سلندر", "بنزين Petrol 6 و 8 سلندر"),
        option2Label = "كثافة ونوع الخلية الداخلية",
        option2Values = listOf("سيراميك أصلي وكالة (High Filtration)", "معدن رياضي 300 Cell"),
        option3Label = "مقاس القطر ومدخل الماسورة",
        option3Values = listOf("2.0 بوصة قياسي", "2.5 بوصة واسع", "3.0 بوصة رياضي"),
        option4Label = "سنة الصنع وتوافق الفحص الدوري",
        option4Values = listOf("تطفي لمبة المكينة وتجتاز الفحص 100%", "مع فتحة حساس أكسجين O2 جاهزة")
      ),
      Product(
        id = "dpf-002",
        title = "فلتر ودبة تلوث ديزل DPF معالجة كاملة للشاحنات والجيوب",
        description = """
فلتر التقاط جزيئات الديزل (Diesel Particulate Filter DPF) شديد التحمل للشاحنات والسيارات الديزل (تويوتا هايلوكس، فورتشنر، لاندكروزر، ميتسوبيشي، إيسوزو، فورد).
يمنع خروج الدخان الأسود ويحسن كفاءة حرق الوقود واستجابة التيربو ويحتوي على أنابيب حساسات فرق الضغط الأصلية.
        """.trimIndent(),
        price = 2200.0,
        originalPrice = 2750.0,
        category = "دبات ديزل",
        rating = 5.0,
        reviewCount = 42,
        imageDrawableName = "prod_dpf_diesel_1794448831848",
        option1Label = "نوع المحرك والديزل",
        option1Values = listOf("تيربو ديزل Common Rail (حديث)", "ديزل تقليدي Heavy Duty"),
        option2Label = "سعة الفلتر ومعالجة الرماد",
        option2Values = listOf("فلتر DPF سيليكون كاربايد (SiC)", "فلتر معالج كوديريت Cordierite"),
        option3Label = "مقاس الشفاه ومسار العادم",
        option3Values = listOf("فلنجة وكالة جاهزة (Direct Fit)", "توصيل لحام يونيفرسال"),
        option4Label = "توافق الموديل",
        option4Values = listOf("موديلات 2018 - 2025", "موديلات 2012 - 2017")
      ),
      Product(
        id = "sport-003",
        title = "دبة تلوث معدنية رياضية هاي فلو 200 خلية (200 CPSI Metal Cat)",
        description = """
دبة بيئة معدنية رياضية تدفق عالي High-Flow مصنوعة من صفائح الفولاذ المقاوم للصدأ المطلية بمادة الروديوم.
تتيح تنقية عوادم البنزين والديزل الرياضي مع تقليل ضغط الارتداد العكسي (Backpressure) لزيادة عزم الدوران وقوة الأحصنة مع المحافظة على البيئة واجتياز فحص الانبعاثات.
        """.trimIndent(),
        price = 1680.0,
        originalPrice = 1990.0,
        category = "دبات رياضية",
        rating = 4.8,
        reviewCount = 51,
        imageDrawableName = "prod_exhaust_quad_1794448543230",
        option1Label = "نوع الوقود المدعوم",
        option1Values = listOf("بنزين سوبر/رياضي", "ديزل رياضي مع تيربو معدل"),
        option2Label = "كثافة الخلية الرياضية",
        option2Values = listOf("200 CPSI (أقصى تدفق وقوة)", "300 CPSI (توازن بين القوة والتنقية)"),
        option3Label = "قطر الأنبوب والتركيب",
        option3Values = listOf("2.5 بوصة (In/Out)", "3.0 بوصة (In/Out)"),
        option4Label = "طريقة التثبيت",
        option4Values = listOf("لحام مباشر مع سناتر حساس O2", "كلبسات فك وتركيب سريعة V-Band")
      ),
      Product(
        id = "ecu-004",
        title = "جهاز فحص وبرمجة حساسات دبات التلوث ومسح أكواد P0420",
        description = """
جهاز إلكتروني ذكي للمحافظة على أداء دبات التلوث في محركات الديزل والبنزين، يقرأ كفاءة المحفز الحفاز، ضغط فلتر الـ DPF، حرارة غازات العادم EGT، ومسح الأخطاء.
        """.trimIndent(),
        price = 850.0,
        originalPrice = 1100.0,
        category = "إلكترونيات وفحص",
        rating = 4.7,
        reviewCount = 29,
        imageDrawableName = "prod_tuning_ecu_1794448742439",
        option1Label = "نوع الفحص المطلوب",
        option1Values = listOf("فحص دبات بنزين (O2 Sensors)", "فحص فلاتر ديزل وتجديد DPF Regen"),
        option2Label = "نوع الاتصال",
        option2Values = listOf("شاشة مستقلة + كابل OBD2", "ربط بلوتوث وتطبيق جوال"),
        option3Label = "اللغة والبرمجة",
        option3Values = listOf("لغة عربية كاملة + إنجليزي", "تحديثات مجانية مدى الحياة"),
        option4Label = "الضمان",
        option4Values = listOf("ضمان استبدال لمدة سنتين", "دعم فني عبر واتساب 0565911414")
      )
    )
  )
  val products: StateFlow<List<Product>> = _products.asStateFlow()

  private val defaultStoreSettings = StoreSettings(
      storeName = "دبات التلوث في الديزل والبنزين",
      marqueeText = "🌟 مرحباً بكم في متجر دبات التلوث وفلاتر الديزل والبنزين الأصلية | ضمان اجتياز الفحص الدوري وإطفاء لمبة المكينة | الطلب والشحن لجميع مناطق المملكة عبر الواتساب: 0565911414",
      marqueeSpeed = MarqueeSpeed.MEDIUM,
      headerVideoTitle = "شرح عمل دبة التلوث وفلتر الديزل DPF وكيف تحمي سيارتك من الأعطال",
      headerVideoDesc = "فيديو توضيحي لطريقة تنقية انبعاثات الكربون واجتياز الفحص الفني بكل سهولة",
      footerVideoTitle = "تجارب عملائنا بعد تركيب دبات التلوث الأصلية واختبار الفحص الدوري",
      footerVideoDesc = "شاهد اختفاء الدخان الأسود وانطفاء لمبة المحرك بعد التركيب المباشر",
      whatsappNumber = "0565911414",
      storeEmail = "dbattloth.221@gmail.com",
      storePhone = "0565911414"
    )
  private val _storeSettings = MutableStateFlow(loadStoreSettings())
  val storeSettings: StateFlow<StoreSettings> = _storeSettings.asStateFlow()

  private val _complaints = MutableStateFlow<List<Complaint>>(
    listOf(
      Complaint(
        id = "c-101",
        customerName = "أبو فهد العتيبي",
        customerContact = "0501234567",
        type = "اقتراح",
        content = "يا ريت توفرون مقاسات دبات تلوث خاصة للشاحنات الكبيرة تريلات مرسيدس وفولفو ديزل.",
        timestamp = System.currentTimeMillis() - 86400000L
      )
    )
  )
  val complaints: StateFlow<List<Complaint>> = _complaints.asStateFlow()

  private val _currentUser = MutableStateFlow(CustomerUser())
  val currentUser: StateFlow<CustomerUser> = _currentUser.asStateFlow()

  private val _orders = MutableStateFlow<List<Order>>(emptyList())
  val orders: StateFlow<List<Order>> = _orders.asStateFlow()

  private val _ordersLoading = MutableStateFlow(false)
  val ordersLoading: StateFlow<Boolean> = _ordersLoading.asStateFlow()

  private val _ordersSyncError = MutableStateFlow<String?>(null)
  val ordersSyncError: StateFlow<String?> = _ordersSyncError.asStateFlow()

  private val _visitorStats = MutableStateFlow(VisitorStats())
  val visitorStats: StateFlow<VisitorStats> = _visitorStats.asStateFlow()

  private val _visitorStatsError = MutableStateFlow<String?>(null)
  val visitorStatsError: StateFlow<String?> = _visitorStatsError.asStateFlow()

  private val _activeUsedCatalyticAds = MutableStateFlow<List<UsedCatalyticAd>>(emptyList())
  val activeUsedCatalyticAds: StateFlow<List<UsedCatalyticAd>> = _activeUsedCatalyticAds.asStateFlow()

  private val _activeUsedAdsLoading = MutableStateFlow(true)
  val activeUsedAdsLoading: StateFlow<Boolean> = _activeUsedAdsLoading.asStateFlow()

  private val _activeUsedAdsError = MutableStateFlow<String?>(null)
  val activeUsedAdsError: StateFlow<String?> = _activeUsedAdsError.asStateFlow()

  private val _usedCatalyticAds = MutableStateFlow<List<UsedCatalyticAd>>(emptyList())
  val usedCatalyticAds: StateFlow<List<UsedCatalyticAd>> = _usedCatalyticAds.asStateFlow()

  private val _usedAdsLoading = MutableStateFlow(false)
  val usedAdsLoading: StateFlow<Boolean> = _usedAdsLoading.asStateFlow()

  private val _usedAdsError = MutableStateFlow<String?>(null)
  val usedAdsError: StateFlow<String?> = _usedAdsError.asStateFlow()

  private val _usedAdCustomerSubscriptions = MutableStateFlow<List<UsedAdCustomerSubscription>>(emptyList())
  val usedAdCustomerSubscriptions: StateFlow<List<UsedAdCustomerSubscription>> =
    _usedAdCustomerSubscriptions.asStateFlow()

  private val _usedAdSubscriptionsLoading = MutableStateFlow(false)
  val usedAdSubscriptionsLoading: StateFlow<Boolean> = _usedAdSubscriptionsLoading.asStateFlow()

  private val _usedAdSubscriptionsError = MutableStateFlow<String?>(null)
  val usedAdSubscriptionsError: StateFlow<String?> = _usedAdSubscriptionsError.asStateFlow()

  private val _activeCommercialStoreAds = MutableStateFlow<List<CommercialStoreAd>>(emptyList())
  val activeCommercialStoreAds: StateFlow<List<CommercialStoreAd>> = _activeCommercialStoreAds.asStateFlow()

  private val _commercialStoreAdsLoading = MutableStateFlow(true)
  val commercialStoreAdsLoading: StateFlow<Boolean> = _commercialStoreAdsLoading.asStateFlow()

  private val _activeCommercialStoreAdsError = MutableStateFlow<String?>(null)
  val activeCommercialStoreAdsError: StateFlow<String?> = _activeCommercialStoreAdsError.asStateFlow()

  private val _commercialStoreAds = MutableStateFlow<List<CommercialStoreAd>>(emptyList())
  val commercialStoreAds: StateFlow<List<CommercialStoreAd>> = _commercialStoreAds.asStateFlow()

  private val _commercialStoreAdsAdminLoading = MutableStateFlow(false)
  val commercialStoreAdsAdminLoading: StateFlow<Boolean> = _commercialStoreAdsAdminLoading.asStateFlow()

  private val _commercialStoreAdsAdminError = MutableStateFlow<String?>(null)
  val commercialStoreAdsAdminError: StateFlow<String?> = _commercialStoreAdsAdminError.asStateFlow()

  private val _ownerWallet = MutableStateFlow(OwnerWallet())
  val ownerWallet: StateFlow<OwnerWallet> = _ownerWallet.asStateFlow()

  private val _ownerWalletTransactions = MutableStateFlow<List<OwnerWalletTransaction>>(emptyList())
  val ownerWalletTransactions: StateFlow<List<OwnerWalletTransaction>> =
    _ownerWalletTransactions.asStateFlow()

  private val _ownerWalletLoading = MutableStateFlow(false)
  val ownerWalletLoading: StateFlow<Boolean> = _ownerWalletLoading.asStateFlow()

  private val _ownerWalletError = MutableStateFlow<String?>(null)
  val ownerWalletError: StateFlow<String?> = _ownerWalletError.asStateFlow()

  private val firestore: FirebaseFirestore? = try {
    FirebaseFirestore.getInstance()
  } catch (error: IllegalStateException) {
    _ordersLoading.value = false
    _ordersSyncError.value = "لم يتم إعداد Firebase في التطبيق."
    _visitorStatsError.value = "لم يتم إعداد Firebase في التطبيق."
    _usedAdsError.value = "لم يتم إعداد Firebase في التطبيق."
    _activeUsedAdsLoading.value = false
    _activeUsedAdsError.value = "لم يتم إعداد Firebase في التطبيق."
    _usedAdSubscriptionsError.value = "لم يتم إعداد Firebase في التطبيق."
    _commercialStoreAdsLoading.value = false
    _activeCommercialStoreAdsError.value = "لم يتم إعداد Firebase في التطبيق."
    _commercialStoreAdsAdminError.value = "لم يتم إعداد Firebase في التطبيق."
    _ownerWalletError.value = "لم يتم إعداد Firebase في التطبيق."
    Log.e(TAG, "Firestore is not initialized", error)
    null
  }

  private val firebaseAuth: FirebaseAuth? = try {
    FirebaseAuth.getInstance()
  } catch (error: IllegalStateException) {
    Log.e(TAG, "Firebase Authentication is not initialized", error)
    null
  }

  private val firebaseFunctions: FirebaseFunctions? = try {
    FirebaseFunctions.getInstance(FIREBASE_FUNCTIONS_REGION)
  } catch (error: IllegalStateException) {
    Log.e(TAG, "Firebase Cloud Functions is not initialized", error)
    null
  }

  private var ordersListener: ListenerRegistration? = null
  private var visitorStatsListener: ListenerRegistration? = null
  private var usedAdsListener: ListenerRegistration? = null
  private var activeUsedAdsListener: ListenerRegistration? = null
  private var usedAdSubscriptionsListener: ListenerRegistration? = null
  private var activeCommercialStoreAdsListener: ListenerRegistration? = null
  private var commercialStoreAdsAdminListener: ListenerRegistration? = null
  private var ownerWalletListener: ListenerRegistration? = null
  private var ownerWalletTransactionsListener: ListenerRegistration? = null
  private var visitRecordedThisProcess = false

  init {
    startActiveUsedAdsListener()
    startActiveCommercialStoreAdsListener()
    firebaseAuth?.signOut()
  }

  // User Authentication
  @Synchronized
  fun loginCustomer(contact: String, name: String = "عميل مميز") {
    firebaseAuth?.signOut()
    _currentUser.value = CustomerUser(
      contactInfo = contact,
      displayName = name,
      isOwner = false,
      isLoggedIn = true
    )
    stopOwnerListeners()
  }

  fun authenticateOwner(
    email: String,
    password: String,
    createAccount: Boolean,
    onComplete: (OwnerAuthenticationResult) -> Unit
  ) {
    val auth = firebaseAuth
    if (auth == null) {
      onComplete(OwnerAuthenticationResult(false, "خدمة تسجيل الدخول غير متاحة. تحقق من إعداد Firebase."))
      return
    }
    val normalizedEmail = email.trim()
    if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
      onComplete(OwnerAuthenticationResult(false, "أدخل بريداً إلكترونياً صالحاً."))
      return
    }
    if (!normalizedEmail.equals(OWNER_EMAIL, ignoreCase = true)) {
      onComplete(OwnerAuthenticationResult(false, "البريد الإلكتروني غير معتمد لحساب المالك."))
      return
    }
    if (password.length < 6) {
      onComplete(OwnerAuthenticationResult(false, "كلمة المرور يجب ألا تقل عن 6 أحرف."))
      return
    }

    if (createAccount) {
      auth.createUserWithEmailAndPassword(normalizedEmail, password)
        .addOnSuccessListener { result ->
          val user = result.user
          if (user == null || !user.email.equals(OWNER_EMAIL, ignoreCase = true)) {
            auth.signOut()
            onComplete(OwnerAuthenticationResult(false, "تعذر إعداد حساب المالك."))
            return@addOnSuccessListener
          }
          user.sendEmailVerification()
            .addOnSuccessListener {
              auth.signOut()
              onComplete(
                OwnerAuthenticationResult(
                  false,
                  "تم إنشاء الحساب. افتح رسالة التحقق المرسلة إلى البريد المعتمد ثم سجّل الدخول."
                )
              )
            }
            .addOnFailureListener { error ->
              auth.signOut()
              Log.e(TAG, "Failed to send owner email verification", error)
              onComplete(
                OwnerAuthenticationResult(
                  false,
                  "تم إنشاء الحساب لكن تعذر إرسال رسالة التحقق: ${error.localizedMessage ?: "خطأ غير معروف"}"
                )
              )
            }
        }
        .addOnFailureListener { error ->
          Log.e(TAG, "Failed to create owner authentication account", error)
          onComplete(
            OwnerAuthenticationResult(
              false,
              "تعذر إنشاء حساب المالك: ${error.localizedMessage ?: "تحقق من إعداد Firebase"}"
            )
          )
        }
      return
    }

    auth.signInWithEmailAndPassword(normalizedEmail, password)
      .addOnSuccessListener { result ->
        val user = result.user
        when {
          user == null || !user.email.equals(OWNER_EMAIL, ignoreCase = true) -> {
            auth.signOut()
            onComplete(OwnerAuthenticationResult(false, "بيانات حساب المالك غير صحيحة."))
          }
          !user.isEmailVerified -> {
            user.sendEmailVerification()
              .addOnSuccessListener {
                auth.signOut()
                onComplete(
                  OwnerAuthenticationResult(
                    false,
                    "تحقق من بريدك الإلكتروني أولاً. أرسلنا رسالة تحقق جديدة."
                  )
                )
              }
              .addOnFailureListener { error ->
                auth.signOut()
                Log.e(TAG, "Failed to resend owner email verification", error)
                onComplete(
                  OwnerAuthenticationResult(false, "الحساب غير مؤكد ولم نتمكن من إعادة إرسال رسالة التحقق.")
                )
              }
          }
          else -> {
            val functions = firebaseFunctions
            if (functions == null) {
              auth.signOut()
              onComplete(OwnerAuthenticationResult(false, "خدمة إرسال رمز الدخول غير متاحة حالياً."))
              return@addOnSuccessListener
            }
            functions.getHttpsCallable("beginOwnerOtp")
              .call(emptyMap<String, Any>())
              .addOnSuccessListener { result ->
                auth.signOut()
                val data = result.data as? Map<*, *>
                val challengeId = data?.get("challengeId") as? String
                if (challengeId.isNullOrBlank()) {
                  onComplete(OwnerAuthenticationResult(false, "لم يصل رد صالح من خدمة رمز الدخول."))
                } else {
                  onComplete(
                    OwnerAuthenticationResult(
                      false,
                      "تم إرسال رمز التحقق إلى البريد المعتمد. أدخله لإكمال الدخول.",
                      challengeId
                    )
                  )
                }
              }
              .addOnFailureListener { error ->
                auth.signOut()
                Log.e(TAG, "Failed to request owner email OTP", error)
                onComplete(
                  OwnerAuthenticationResult(
                    false,
                    "تعذر إرسال رمز التحقق: ${error.localizedMessage ?: "تحقق من إعداد خدمة البريد"}"
                  )
                )
              }
          }
        }
      }
      .addOnFailureListener { error ->
        Log.e(TAG, "Owner sign-in failed", error)
        onComplete(
          OwnerAuthenticationResult(
            false,
            "تعذر تسجيل الدخول: تحقق من كلمة المرور وإعداد Email/Password في Firebase."
          )
        )
      }
  }

  fun verifyOwnerLoginCode(
    challengeId: String,
    code: String,
    onComplete: (Boolean, String) -> Unit
  ) {
    if (challengeId.isBlank() || !code.matches(Regex("^\\d{6}$"))) {
      onComplete(false, "أدخل رمز التحقق المكوّن من 6 أرقام.")
      return
    }
    val auth = firebaseAuth
    val functions = firebaseFunctions
    if (auth == null || functions == null) {
      onComplete(false, "خدمة تسجيل الدخول غير متاحة حالياً.")
      return
    }

    functions.getHttpsCallable("verifyOwnerOtp")
      .call(mapOf("challengeId" to challengeId, "code" to code))
      .addOnSuccessListener { result ->
        val customToken = (result.data as? Map<*, *>)?.get("customToken") as? String
        if (customToken.isNullOrBlank()) {
          onComplete(false, "تعذر التحقق من الرمز.")
          return@addOnSuccessListener
        }
        auth.signInWithCustomToken(customToken)
          .addOnSuccessListener { signInResult ->
            val user = signInResult.user
            if (user != null && isVerifiedOwner(user)) {
              establishOwnerSession(user.email ?: OWNER_EMAIL)
              onComplete(true, "تم تأكيد الرمز وتسجيل الدخول بنجاح.")
            } else {
              auth.signOut()
              onComplete(false, "لم يتم التحقق من هوية المالك.")
            }
          }
          .addOnFailureListener { error ->
            auth.signOut()
            Log.e(TAG, "Failed to complete owner OTP sign-in", error)
            onComplete(false, "تعذر إكمال تسجيل الدخول بعد التحقق.")
          }
      }
      .addOnFailureListener { error ->
        Log.e(TAG, "Owner OTP verification failed", error)
        onComplete(false, "رمز التحقق غير صحيح أو منتهي الصلاحية. اطلب رمزاً جديداً.")
      }
  }

  fun sendOwnerPasswordReset(email: String, onComplete: (String) -> Unit) {
    val auth = firebaseAuth
    if (auth == null) {
      onComplete("خدمة تسجيل الدخول غير متاحة. تحقق من إعداد Firebase.")
      return
    }
    val normalizedEmail = email.trim()
    if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
      onComplete("أدخل بريداً إلكترونياً صالحاً لإرسال رابط الاستعادة.")
      return
    }
    auth.sendPasswordResetEmail(normalizedEmail)
      .addOnSuccessListener {
        onComplete("تم إرسال رابط استعادة كلمة المرور إلى البريد المدخل. تحقق من البريد الوارد ومجلد الرسائل غير المرغوب فيها.")
      }
      .addOnFailureListener { error ->
        Log.e(TAG, "Failed to send owner password reset", error)
        onComplete("تعذر إرسال رسالة تعيين كلمة المرور. تحقق من إعدادات Firebase واتصال الإنترنت ثم حاول مجدداً.")
      }
  }

  @Synchronized
  fun logout() {
    firebaseAuth?.signOut()
    stopOwnerListeners()
    _currentUser.value = CustomerUser()
  }

  @Synchronized
  private fun establishOwnerSession(email: String) {
    _currentUser.value = CustomerUser(
      contactInfo = email,
      displayName = "مالك المتجر (المدير)",
      isOwner = true,
      isLoggedIn = true
    )
    startOrdersListener()
    startVisitorStatsListener()
    startUsedAdsListener()
    startUsedAdSubscriptionsListener()
    startCommercialStoreAdsAdminListener()
    startOwnerWalletListeners()
  }

  private fun isVerifiedOwner(user: FirebaseUser): Boolean =
    user.email.equals(OWNER_EMAIL, ignoreCase = true) && user.isEmailVerified

  @Synchronized
  fun recordAppVisit() {
    if (visitRecordedThisProcess) return
    val database = firestore ?: return
    visitRecordedThisProcess = true

    val dayKey = currentVisitorDayKey()
    val statsDocument = database.collection(ANALYTICS_COLLECTION).document(VISITOR_STATS_DOCUMENT)
    database.runTransaction { transaction ->
      val snapshot = transaction.get(statsDocument)
      val totalVisitors = (snapshot.getLong("totalVisitors") ?: 0L) + 1L
      val todayVisitors = if (snapshot.getString("dayKey") == dayKey) {
        (snapshot.getLong("todayVisitors") ?: 0L) + 1L
      } else {
        1L
      }
      val stats = VisitorStats(totalVisitors, todayVisitors, dayKey)
      transaction.set(statsDocument, stats.toFirestoreMap())
      stats
    }.addOnSuccessListener { stats ->
      _visitorStats.value = stats
      _visitorStatsError.value = null
    }.addOnFailureListener { error ->
      _visitorStatsError.value = "تعذر تسجيل الزيارة: ${error.localizedMessage ?: "خطأ غير معروف"}"
      Log.e(TAG, "Failed to record app visit", error)
    }
  }

  fun resetVisitorCounts(onComplete: (String?) -> Unit) {
    if (!_currentUser.value.isOwner) {
      onComplete("يقتصر تصفير الإحصاءات على مالك المتجر.")
      return
    }

    val database = firestore
    if (database == null) {
      onComplete(_visitorStatsError.value ?: "خدمة الإحصاءات غير متاحة حالياً.")
      return
    }

    val dayKey = currentVisitorDayKey()
    val statsDocument = database.collection(ANALYTICS_COLLECTION).document(VISITOR_STATS_DOCUMENT)
    database.runTransaction { transaction ->
      transaction.get(statsDocument)
      val stats = VisitorStats(dayKey = dayKey)
      transaction.set(statsDocument, stats.toFirestoreMap())
      stats
    }.addOnSuccessListener { stats ->
      _visitorStats.value = stats
      _visitorStatsError.value = null
      onComplete(null)
    }.addOnFailureListener { error ->
      _visitorStatsError.value = "تعذر تصفير الإحصاءات: ${error.localizedMessage ?: "خطأ غير معروف"}"
      Log.e(TAG, "Failed to reset visitor counts", error)
      onComplete(_visitorStatsError.value)
    }
  }

  private fun currentVisitorDayKey(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US)
      .apply { timeZone = TimeZone.getTimeZone("Asia/Riyadh") }
      .format(Date())

  private fun VisitorStats.toFirestoreMap(): Map<String, Any> = mapOf(
    "totalVisitors" to totalVisitors,
    "todayVisitors" to todayVisitors,
    "dayKey" to dayKey
  )

  private fun startVisitorStatsListener() {
    val database = firestore ?: return
    if (visitorStatsListener != null) return

    visitorStatsListener = database.collection(ANALYTICS_COLLECTION)
      .document(VISITOR_STATS_DOCUMENT)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          _visitorStatsError.value = "تعذرت مزامنة إحصاءات الزوار: ${error.localizedMessage ?: "خطأ غير معروف"}"
          Log.e(TAG, "Failed to listen for visitor stats", error)
        } else if (snapshot?.exists() == true) {
          _visitorStats.value = VisitorStats(
            totalVisitors = snapshot.getLong("totalVisitors") ?: 0L,
            todayVisitors = snapshot.getLong("todayVisitors") ?: 0L,
            dayKey = snapshot.getString("dayKey") ?: ""
          )
          _visitorStatsError.value = null
        } else if (snapshot != null) {
          _visitorStats.value = VisitorStats()
          _visitorStatsError.value = null
        }
      }
  }

  private fun stopOwnerListeners() {
    stopOrdersListener()
    usedAdsListener?.remove()
    usedAdsListener = null
    _usedCatalyticAds.value = emptyList()
    _usedAdsLoading.value = false
    usedAdSubscriptionsListener?.remove()
    usedAdSubscriptionsListener = null
    _usedAdCustomerSubscriptions.value = emptyList()
    _usedAdSubscriptionsLoading.value = false
    _usedAdSubscriptionsError.value = null
    commercialStoreAdsAdminListener?.remove()
    commercialStoreAdsAdminListener = null
    _commercialStoreAds.value = emptyList()
    _commercialStoreAdsAdminLoading.value = false
    _commercialStoreAdsAdminError.value = null
    ownerWalletListener?.remove()
    ownerWalletListener = null
    ownerWalletTransactionsListener?.remove()
    ownerWalletTransactionsListener = null
    _ownerWallet.value = OwnerWallet()
    _ownerWalletTransactions.value = emptyList()
    _ownerWalletLoading.value = false
    _ownerWalletError.value = null
    visitorStatsListener?.remove()
    visitorStatsListener = null
  }

  private fun startOwnerWalletListeners() {
    val database = firestore ?: return
    if (ownerWalletListener != null || !_currentUser.value.isOwner) return

    val walletDocument = database.collection(OWNER_WALLET_COLLECTION).document(OWNER_WALLET_DOCUMENT)
    _ownerWalletLoading.value = true
    ownerWalletListener = walletDocument.addSnapshotListener { snapshot, error ->
      if (error != null) {
        _ownerWalletLoading.value = false
        _ownerWalletError.value = "تعذرت مزامنة المحفظة: ${error.localizedMessage ?: "خطأ غير معروف"}"
        Log.e(TAG, "Failed to listen for owner wallet", error)
      } else if (snapshot != null) {
        _ownerWallet.value = if (snapshot.exists()) parseOwnerWallet(snapshot) else OwnerWallet()
        _ownerWalletLoading.value = false
        _ownerWalletError.value = null
      }
    }
    ownerWalletTransactionsListener = walletDocument.collection("transactions")
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          _ownerWalletError.value = "تعذر تحميل سجل المحفظة: ${error.localizedMessage ?: "خطأ غير معروف"}"
          Log.e(TAG, "Failed to listen for owner wallet transactions", error)
        } else if (snapshot != null) {
          _ownerWalletTransactions.value = snapshot.documents.mapNotNull(::parseOwnerWalletTransaction)
          _ownerWalletError.value = null
        }
      }
  }

  fun saveOwnerBankDetails(bankName: String, iban: String, onComplete: (String?) -> Unit) {
    if (!_currentUser.value.isOwner) {
      onComplete("يقتصر حفظ بيانات الحساب البنكي على مالك المتجر.")
      return
    }
    val normalizedBankName = bankName.trim()
    val normalizedIban = iban.filterNot(Char::isWhitespace).uppercase(Locale.US)
    if (normalizedBankName.isBlank()) {
      onComplete("أدخل اسم البنك.")
      return
    }
    if (!normalizedIban.matches(Regex("^[A-Z]{2}[0-9A-Z]{13,32}$"))) {
      onComplete("رقم الآيبان غير صالح.")
      return
    }

    val database = firestore
    if (database == null) {
      onComplete("خدمة المحفظة غير متاحة حالياً.")
      return
    }
    database.collection(OWNER_WALLET_COLLECTION)
      .document(OWNER_WALLET_DOCUMENT)
      .set(mapOf("bankName" to normalizedBankName, "iban" to normalizedIban), SetOptions.merge())
      .addOnSuccessListener {
        _ownerWallet.value = _ownerWallet.value.copy(
          bankName = normalizedBankName,
          iban = normalizedIban
        )
        _ownerWalletError.value = null
        onComplete(null)
      }
      .addOnFailureListener { error ->
        _ownerWalletError.value = "تعذر حفظ بيانات البنك: ${error.localizedMessage ?: "خطأ غير معروف"}"
        Log.e(TAG, "Failed to save owner bank details", error)
        onComplete(_ownerWalletError.value)
      }
  }

  fun addOwnerWalletTransaction(
    type: OwnerWalletTransactionType,
    storeName: String,
    orderNumber: String,
    amountHalalas: Long,
    onComplete: (String?) -> Unit
  ) {
    if (!_currentUser.value.isOwner) {
      onComplete("يقتصر تسجيل عمليات المحفظة على مالك المتجر.")
      return
    }
    if (amountHalalas <= 0L) {
      onComplete("أدخل مبلغاً أكبر من صفر.")
      return
    }
    val normalizedStoreName = storeName.trim()
    if (type == OwnerWalletTransactionType.INVENTORY_PURCHASE &&
      normalizedStoreName !in setOf("AliExpress", "Alibaba", "Amazon")
    ) {
      onComplete("اختر متجراً صحيحاً للشراء.")
      return
    }
    if (type == OwnerWalletTransactionType.INVENTORY_PURCHASE && orderNumber.isBlank()) {
      onComplete("أدخل رقم طلب الشراء.")
      return
    }

    val database = firestore
    if (database == null) {
      onComplete("خدمة المحفظة غير متاحة حالياً.")
      return
    }

    val walletDocument = database.collection(OWNER_WALLET_COLLECTION).document(OWNER_WALLET_DOCUMENT)
    val walletTransaction = OwnerWalletTransaction(
      id = UUID.randomUUID().toString(),
      type = type,
      storeName = if (type == OwnerWalletTransactionType.INVENTORY_PURCHASE) normalizedStoreName else "",
      orderNumber = orderNumber.trim().take(100),
      amountHalalas = amountHalalas
    )
    database.runTransaction { transaction ->
      val snapshot = transaction.get(walletDocument)
      val currentWallet = if (snapshot.exists()) parseOwnerWallet(snapshot) else OwnerWallet()
      val isCredit = type == OwnerWalletTransactionType.PROFIT_DEPOSIT
      if (type == OwnerWalletTransactionType.PERSONAL_WITHDRAWAL &&
        (currentWallet.bankName.isBlank() || currentWallet.iban.isBlank())
      ) {
        return@runTransaction OwnerWalletMutationResult(
          currentWallet,
          errorMessage = "احفظ بيانات البنك والآيبان قبل تسجيل السحب."
        )
      }
      if (!isCredit && amountHalalas > currentWallet.balanceHalalas) {
        return@runTransaction OwnerWalletMutationResult(
          currentWallet,
          errorMessage = "الرصيد المتاح لا يكفي لإتمام العملية."
        )
      }

      val updatedWallet = when (type) {
        OwnerWalletTransactionType.PROFIT_DEPOSIT -> currentWallet.copy(
          balanceHalalas = Math.addExact(currentWallet.balanceHalalas, amountHalalas),
          totalEarningsHalalas = Math.addExact(currentWallet.totalEarningsHalalas, amountHalalas)
        )
        OwnerWalletTransactionType.INVENTORY_PURCHASE -> currentWallet.copy(
          balanceHalalas = currentWallet.balanceHalalas - amountHalalas,
          totalPurchasesHalalas = Math.addExact(currentWallet.totalPurchasesHalalas, amountHalalas)
        )
        OwnerWalletTransactionType.PERSONAL_WITHDRAWAL -> currentWallet.copy(
          balanceHalalas = currentWallet.balanceHalalas - amountHalalas,
          totalWithdrawalsHalalas = Math.addExact(currentWallet.totalWithdrawalsHalalas, amountHalalas)
        )
      }
      transaction.set(walletDocument, updatedWallet.toFirestoreMap())
      transaction.set(
        walletDocument.collection("transactions").document(walletTransaction.id),
        walletTransaction.toFirestoreMap()
      )
      OwnerWalletMutationResult(updatedWallet, errorMessage = null)
    }.addOnSuccessListener { result ->
      if (result.errorMessage != null) {
        onComplete(result.errorMessage)
      } else {
        _ownerWallet.value = result.wallet
        _ownerWalletError.value = null
        onComplete(null)
      }
    }.addOnFailureListener { error ->
      _ownerWalletError.value = "تعذر حفظ العملية: ${error.localizedMessage ?: "خطأ غير معروف"}"
      Log.e(TAG, "Failed to save owner wallet transaction", error)
      onComplete(_ownerWalletError.value)
    }
  }

  private data class OwnerWalletMutationResult(
    val wallet: OwnerWallet,
    val errorMessage: String?
  )

  private fun OwnerWallet.toFirestoreMap(): Map<String, Any> = mapOf(
    "bankName" to bankName,
    "iban" to iban,
    "balanceHalalas" to balanceHalalas,
    "totalEarningsHalalas" to totalEarningsHalalas,
    "totalPurchasesHalalas" to totalPurchasesHalalas,
    "totalWithdrawalsHalalas" to totalWithdrawalsHalalas
  )

  private fun parseOwnerWallet(document: DocumentSnapshot): OwnerWallet = OwnerWallet(
    bankName = document.getString("bankName") ?: "",
    iban = document.getString("iban") ?: "",
    balanceHalalas = document.getLong("balanceHalalas") ?: 0L,
    totalEarningsHalalas = document.getLong("totalEarningsHalalas") ?: 0L,
    totalPurchasesHalalas = document.getLong("totalPurchasesHalalas") ?: 0L,
    totalWithdrawalsHalalas = document.getLong("totalWithdrawalsHalalas") ?: 0L
  )

  private fun OwnerWalletTransaction.toFirestoreMap(): Map<String, Any> = mapOf(
    "type" to type.name,
    "storeName" to storeName,
    "orderNumber" to orderNumber,
    "amountSar" to amountSar,
    "amountHalalas" to amountHalalas,
    "createdAt" to createdAt
  )

  private fun parseOwnerWalletTransaction(document: DocumentSnapshot): OwnerWalletTransaction? {
    val type = OwnerWalletTransactionType.values()
      .firstOrNull { it.name == document.getString("type") } ?: return null
    val amountHalalas = document.getLong("amountHalalas")
      ?: ((document.get("amountSar") as? Number)?.toDouble()?.times(100)?.toLong())
      ?: return null
    return OwnerWalletTransaction(
      id = document.id,
      type = type,
      storeName = document.getString("storeName") ?: "",
      orderNumber = document.getString("orderNumber") ?: "",
      amountHalalas = amountHalalas,
      createdAt = document.getLong("createdAt") ?: 0L
    )
  }

  private fun startActiveUsedAdsListener() {
    val database = firestore ?: return
    activeUsedAdsListener = database.collection(USED_ADS_COLLECTION)
      .whereEqualTo("status", UsedCatalyticAdStatus.ACTIVE.name)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          _activeUsedAdsLoading.value = false
          _activeUsedAdsError.value = "تعذر تحميل إعلانات الدبات المستعملة: ${error.localizedMessage ?: "خطأ غير معروف"}"
          Log.e(TAG, "Failed to listen for active used-ad listings", error)
        } else if (snapshot != null) {
          val now = System.currentTimeMillis()
          _activeUsedCatalyticAds.value = snapshot.documents
            .mapNotNull(::parseUsedCatalyticAd)
            .filter { it.expiresAt > now }
          _activeUsedAdsLoading.value = false
          _activeUsedAdsError.value = null
        }
      }
  }

  private fun startUsedAdsListener() {
    val database = firestore ?: return
    if (usedAdsListener != null) return

    _usedAdsLoading.value = true
    _usedAdsError.value = null
    usedAdsListener = database.collection(USED_ADS_COLLECTION)
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          _usedAdsLoading.value = false
          _usedAdsError.value = "تعذر مزامنة إعلانات الدبات: ${error.localizedMessage ?: "خطأ غير معروف"}"
          Log.e(TAG, "Failed to listen for used-ad listings", error)
        } else if (snapshot != null) {
          val ads = snapshot.documents.mapNotNull(::parseUsedCatalyticAd)
          _usedCatalyticAds.value = ads
          _usedAdsLoading.value = false
          _usedAdsError.value = null
          val now = System.currentTimeMillis()
          ads.filter {
            it.status == UsedCatalyticAdStatus.ACTIVE && it.expiresAt > 0L && it.expiresAt <= now
          }.forEach { ad ->
            database.collection(USED_ADS_COLLECTION)
              .document(ad.id)
              .update("status", UsedCatalyticAdStatus.EXPIRED.name)
              .addOnFailureListener { updateError ->
                Log.e(TAG, "Failed to expire used-ad listing ${ad.id}", updateError)
              }
          }
        }
      }
  }

  private fun startUsedAdSubscriptionsListener() {
    val database = firestore ?: return
    if (usedAdSubscriptionsListener != null) return

    _usedAdSubscriptionsLoading.value = true
    _usedAdSubscriptionsError.value = null
    usedAdSubscriptionsListener = database.collection(USED_AD_SUBSCRIPTIONS_COLLECTION)
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          _usedAdSubscriptionsLoading.value = false
          _usedAdSubscriptionsError.value =
            "تعذرت مزامنة اشتراكات سوق المستعمل: ${error.localizedMessage ?: "خطأ غير معروف"}"
          Log.e(TAG, "Failed to listen for used-ad subscriptions", error)
        } else if (snapshot != null) {
          val now = System.currentTimeMillis()
          val subscriptions = snapshot.documents.mapNotNull(::parseUsedAdCustomerSubscription)
          _usedAdCustomerSubscriptions.value = subscriptions.map { subscription ->
            if (subscription.status == UsedAdSubscriptionStatus.ACTIVE && subscription.expiresAt <= now) {
              database.collection(USED_AD_SUBSCRIPTIONS_COLLECTION)
                .document(subscription.id)
                .update("status", UsedAdSubscriptionStatus.EXPIRED.name)
                .addOnFailureListener { updateError ->
                  Log.e(TAG, "Failed to expire used-ad subscription ${subscription.id}", updateError)
                }
              subscription.copy(status = UsedAdSubscriptionStatus.EXPIRED)
            } else {
              subscription
            }
          }
          _usedAdSubscriptionsLoading.value = false
          _usedAdSubscriptionsError.value = null
        }
      }
  }

  private fun startActiveCommercialStoreAdsListener() {
    val database = firestore ?: return
    activeCommercialStoreAdsListener = database.collection(COMMERCIAL_STORE_ADS_COLLECTION)
      .whereEqualTo("status", CommercialStoreAdStatus.ACTIVE.name)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          _commercialStoreAdsLoading.value = false
          _activeCommercialStoreAdsError.value =
            "تعذر تحميل إعلانات المحلات: ${error.localizedMessage ?: "خطأ غير معروف"}"
          Log.e(TAG, "Failed to listen for active commercial store ads", error)
        } else if (snapshot != null) {
          val now = System.currentTimeMillis()
          _activeCommercialStoreAds.value = snapshot.documents
            .mapNotNull(::parseCommercialStoreAd)
            .filter { it.expiryTimestamp > now }
          _commercialStoreAdsLoading.value = false
          _activeCommercialStoreAdsError.value = null
        }
      }
  }

  private fun startCommercialStoreAdsAdminListener() {
    val database = firestore ?: return
    if (commercialStoreAdsAdminListener != null) return

    _commercialStoreAdsAdminLoading.value = true
    _commercialStoreAdsAdminError.value = null
    commercialStoreAdsAdminListener = database.collection(COMMERCIAL_STORE_ADS_COLLECTION)
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          _commercialStoreAdsAdminLoading.value = false
          _commercialStoreAdsAdminError.value =
            "تعذرت مزامنة إعلانات المحلات: ${error.localizedMessage ?: "خطأ غير معروف"}"
          Log.e(TAG, "Failed to listen for commercial store ads", error)
        } else if (snapshot != null) {
          val now = System.currentTimeMillis()
          val ads = snapshot.documents.mapNotNull(::parseCommercialStoreAd)
          _commercialStoreAds.value = ads.map { ad ->
            if (ad.status == CommercialStoreAdStatus.ACTIVE && ad.expiryTimestamp <= now) {
              database.collection(COMMERCIAL_STORE_ADS_COLLECTION)
                .document(ad.id)
                .update("status", CommercialStoreAdStatus.EXPIRED.name)
                .addOnFailureListener { updateError ->
                  _commercialStoreAdsAdminError.value =
                    "تعذر تحديث حالة إعلان ${ad.storeName} إلى منتهي."
                  Log.e(TAG, "Failed to expire commercial store ad ${ad.id}", updateError)
                }
              ad.copy(status = CommercialStoreAdStatus.EXPIRED)
            } else {
              ad
            }
          }
          _commercialStoreAdsAdminLoading.value = false
          _commercialStoreAdsAdminError.value = null
        }
      }
  }

  private fun CommercialStoreAd.toFirestoreMap(): Map<String, Any> = mapOf(
    "storeName" to storeName,
    "goodsType" to goodsType,
    "imageUrls" to imageUrls.take(4),
    "videoUrl" to videoUrl,
    "region" to region,
    "city" to city,
    "locationUrl" to locationUrl,
    "contactPhone" to contactPhone,
    "durationValue" to durationValue,
    "durationUnit" to durationUnit.name,
    "expiryTimestamp" to expiryTimestamp,
    "status" to status.name,
    "createdAt" to createdAt
  )

  private fun parseCommercialStoreAd(document: DocumentSnapshot): CommercialStoreAd? {
    val imageUrls = (document.get("imageUrls") as? List<*>)
      ?.mapNotNull { it as? String }
      ?.filter(String::isNotBlank)
      ?.take(4)
      .orEmpty()
    return CommercialStoreAd(
      id = document.id,
      storeName = document.getString("storeName") ?: return null,
      goodsType = document.getString("goodsType") ?: "",
      imageUrls = imageUrls,
      videoUrl = document.getString("videoUrl") ?: "",
      region = document.getString("region") ?: "",
      city = document.getString("city") ?: "",
      locationUrl = document.getString("locationUrl") ?: "",
      contactPhone = document.getString("contactPhone") ?: "",
      durationValue = document.getLong("durationValue")?.toInt() ?: 30,
      durationUnit = CommercialAdDurationUnit.values()
        .firstOrNull { it.name == document.getString("durationUnit") } ?: CommercialAdDurationUnit.DAYS,
      expiryTimestamp = document.getLong("expiryTimestamp") ?: 0L,
      status = CommercialStoreAdStatus.values()
        .firstOrNull { it.name == document.getString("status") } ?: CommercialStoreAdStatus.PENDING_PAYMENT,
      createdAt = document.getLong("createdAt") ?: 0L
    )
  }

  private fun calculateCommercialAdExpiry(
    fromTimestamp: Long,
    durationValue: Int,
    durationUnit: CommercialAdDurationUnit
  ): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = fromTimestamp }
    when (durationUnit) {
      CommercialAdDurationUnit.DAYS -> calendar.add(Calendar.DAY_OF_YEAR, durationValue)
      CommercialAdDurationUnit.MONTHS -> calendar.add(Calendar.MONTH, durationValue)
      CommercialAdDurationUnit.YEARS -> calendar.add(Calendar.YEAR, durationValue)
    }
    return calendar.timeInMillis
  }

  private fun validateCommercialStoreAd(ad: CommercialStoreAd): String? {
    if (
      ad.storeName.isBlank() || ad.goodsType.isBlank() || ad.region.isBlank() ||
      ad.city.isBlank() || ad.contactPhone.isBlank()
    ) {
      return "أكمل اسم المحل ونوع السلع والمنطقة والمدينة ورقم التواصل."
    }
    if (ad.durationValue <= 0) return "أدخل مدة إعلان أكبر من صفر."
    if (ad.imageUrls.size > 4) return "يسمح بإضافة أربع صور كحد أقصى."
    return null
  }

  fun submitCommercialStoreAd(ad: CommercialStoreAd, onComplete: (String?) -> Unit) {
    val validationError = validateCommercialStoreAd(ad)
    if (validationError != null) {
      onComplete(validationError)
      return
    }
    val database = firestore
    if (database == null) {
      onComplete(_activeCommercialStoreAdsError.value ?: "خدمة إعلانات المحلات غير متاحة حالياً.")
      return
    }

    val now = System.currentTimeMillis()
    val adId = UUID.randomUUID().toString()
    val request = ad.copy(
      id = adId,
      imageUrls = ad.imageUrls.map(String::trim).filter(String::isNotBlank).take(4),
      videoUrl = ad.videoUrl.trim(),
      status = CommercialStoreAdStatus.PENDING_PAYMENT,
      expiryTimestamp = 0L,
      createdAt = now
    )
    database.collection(COMMERCIAL_STORE_ADS_COLLECTION)
      .document(adId)
      .set(request.toFirestoreMap())
      .addOnSuccessListener { onComplete(null) }
      .addOnFailureListener { error ->
        Log.e(TAG, "Failed to submit commercial store ad", error)
        onComplete("تعذر إرسال طلب الإعلان: ${error.localizedMessage ?: "خطأ غير معروف"}")
      }
  }

  fun addCommercialStoreAdDirectly(ad: CommercialStoreAd, onComplete: (String?) -> Unit) {
    if (!_currentUser.value.isOwner) {
      onComplete("يقتصر إضافة إعلانات المحلات مباشرة على مالك المتجر.")
      return
    }
    val validationError = validateCommercialStoreAd(ad)
    if (validationError != null) {
      onComplete(validationError)
      return
    }
    val database = firestore
    if (database == null) {
      onComplete(_commercialStoreAdsAdminError.value ?: "خدمة إعلانات المحلات غير متاحة حالياً.")
      return
    }

    val now = System.currentTimeMillis()
    val adId = UUID.randomUUID().toString()
    val activeAd = ad.copy(
      id = adId,
      imageUrls = ad.imageUrls.map(String::trim).filter(String::isNotBlank).take(4),
      videoUrl = ad.videoUrl.trim(),
      status = CommercialStoreAdStatus.ACTIVE,
      expiryTimestamp = calculateCommercialAdExpiry(now, ad.durationValue, ad.durationUnit),
      createdAt = now
    )
    database.collection(COMMERCIAL_STORE_ADS_COLLECTION)
      .document(adId)
      .set(activeAd.toFirestoreMap())
      .addOnSuccessListener { onComplete(null) }
      .addOnFailureListener { error ->
        _commercialStoreAdsAdminError.value =
          "تعذر إضافة إعلان المحل: ${error.localizedMessage ?: "خطأ غير معروف"}"
        Log.e(TAG, "Failed to add commercial store ad directly", error)
        onComplete(_commercialStoreAdsAdminError.value)
      }
  }

  fun approveOrRenewCommercialStoreAd(ad: CommercialStoreAd, onComplete: (String?) -> Unit) {
    if (!_currentUser.value.isOwner) {
      onComplete("يقتصر اعتماد وتجديد إعلانات المحلات على مالك المتجر.")
      return
    }
    if (ad.id.isBlank()) {
      onComplete("تعذر تحديث الإعلان: رقم الإعلان غير صالح.")
      return
    }
    val validationError = validateCommercialStoreAd(ad)
    if (validationError != null) {
      onComplete(validationError)
      return
    }
    val database = firestore
    if (database == null) {
      onComplete(_commercialStoreAdsAdminError.value ?: "خدمة إعلانات المحلات غير متاحة حالياً.")
      return
    }

    val now = System.currentTimeMillis()
    val baseTimestamp = if (
      ad.status == CommercialStoreAdStatus.ACTIVE && ad.expiryTimestamp > now
    ) ad.expiryTimestamp else now
    val activeAd = ad.copy(
      imageUrls = ad.imageUrls.map(String::trim).filter(String::isNotBlank).take(4),
      videoUrl = ad.videoUrl.trim(),
      status = CommercialStoreAdStatus.ACTIVE,
      expiryTimestamp = calculateCommercialAdExpiry(baseTimestamp, ad.durationValue, ad.durationUnit)
    )
    database.collection(COMMERCIAL_STORE_ADS_COLLECTION)
      .document(ad.id)
      .set(activeAd.toFirestoreMap())
      .addOnSuccessListener { onComplete(null) }
      .addOnFailureListener { error ->
        _commercialStoreAdsAdminError.value =
          "تعذر اعتماد أو تجديد الإعلان: ${error.localizedMessage ?: "خطأ غير معروف"}"
        Log.e(TAG, "Failed to approve or renew commercial store ad ${ad.id}", error)
        onComplete(_commercialStoreAdsAdminError.value)
      }
  }

  fun expireCommercialStoreAd(ad: CommercialStoreAd, onComplete: (String?) -> Unit) {
    if (!_currentUser.value.isOwner) {
      onComplete("يقتصر إنهاء إعلانات المحلات على مالك المتجر.")
      return
    }
    if (ad.id.isBlank()) {
      onComplete("تعذر إنهاء الإعلان: رقم الإعلان غير صالح.")
      return
    }
    val database = firestore
    if (database == null) {
      onComplete(_commercialStoreAdsAdminError.value ?: "خدمة إعلانات المحلات غير متاحة حالياً.")
      return
    }
    database.collection(COMMERCIAL_STORE_ADS_COLLECTION)
      .document(ad.id)
      .update(
        mapOf(
          "status" to CommercialStoreAdStatus.EXPIRED.name,
          "expiryTimestamp" to System.currentTimeMillis()
        )
      )
      .addOnSuccessListener { onComplete(null) }
      .addOnFailureListener { error ->
        _commercialStoreAdsAdminError.value =
          "تعذر إنهاء الإعلان: ${error.localizedMessage ?: "خطأ غير معروف"}"
        Log.e(TAG, "Failed to expire commercial store ad ${ad.id}", error)
        onComplete(_commercialStoreAdsAdminError.value)
      }
  }

  private fun UsedCatalyticAd.toFirestoreMap(): Map<String, Any> = mapOf(
    "sellerName" to sellerName,
    "sellerPhone" to sellerPhone,
    "city" to city,
    "vehicleType" to vehicleType,
    "vehicleModel" to vehicleModel,
    "catalyticCondition" to catalyticCondition.name,
    "price" to price,
    "imageUrl" to imageUrl,
    "subscriptionDays" to subscriptionDays,
    "subscriptionId" to subscriptionId,
    "subscriptionPaidAmount" to subscriptionPaidAmount,
    "status" to status.name,
    "createdAt" to createdAt,
    "expiresAt" to expiresAt
  )

  private fun parseUsedCatalyticAd(document: DocumentSnapshot): UsedCatalyticAd? {
    val conditionName = document.getString("catalyticCondition")
    val statusName = document.getString("status")
    return UsedCatalyticAd(
      id = document.id,
      sellerName = document.getString("sellerName") ?: return null,
      sellerPhone = document.getString("sellerPhone") ?: "",
      city = document.getString("city") ?: "",
      vehicleType = document.getString("vehicleType") ?: "",
      vehicleModel = document.getString("vehicleModel") ?: "",
      catalyticCondition = CatalyticCondition.values()
        .firstOrNull { it.name == conditionName } ?: CatalyticCondition.USED,
      price = document.getDouble("price") ?: 0.0,
      imageUrl = document.getString("imageUrl") ?: "",
      subscriptionDays = document.getLong("subscriptionDays")?.toInt() ?: 60,
      subscriptionId = document.getString("subscriptionId") ?: "",
      subscriptionPaidAmount = document.getDouble("subscriptionPaidAmount") ?: 0.0,
      status = UsedCatalyticAdStatus.values()
        .firstOrNull { it.name == statusName } ?: UsedCatalyticAdStatus.PENDING_PAYMENT,
      createdAt = document.getLong("createdAt") ?: 0L,
      expiresAt = document.getLong("expiresAt") ?: 0L
    )
  }

  private fun UsedAdCustomerSubscription.toFirestoreMap(): Map<String, Any> = mapOf(
    "sellerName" to sellerName,
    "sellerPhone" to sellerPhone,
    "productLimit" to productLimit,
    "submittedProductCount" to submittedProductCount,
    "agreedPrice" to agreedPrice,
    "durationDays" to durationDays,
    "status" to status.name,
    "createdAt" to createdAt,
    "expiresAt" to expiresAt
  )

  private fun parseUsedAdCustomerSubscription(document: DocumentSnapshot): UsedAdCustomerSubscription? {
    val statusName = document.getString("status")
    return UsedAdCustomerSubscription(
      id = document.id,
      sellerName = document.getString("sellerName") ?: return null,
      sellerPhone = document.getString("sellerPhone") ?: "",
      productLimit = document.getLong("productLimit")?.toInt() ?: 0,
      submittedProductCount = document.getLong("submittedProductCount")?.toInt() ?: 0,
      agreedPrice = document.getDouble("agreedPrice") ?: 0.0,
      durationDays = document.getLong("durationDays")?.toInt() ?: 60,
      status = UsedAdSubscriptionStatus.values()
        .firstOrNull { it.name == statusName } ?: UsedAdSubscriptionStatus.UNSUBSCRIBED,
      createdAt = document.getLong("createdAt") ?: 0L,
      expiresAt = document.getLong("expiresAt") ?: 0L
    )
  }

  private fun normalizedSellerPhone(phone: String): String {
    val digits = phone.filter(Char::isDigit)
    return when {
      digits.startsWith("00966") -> digits.drop(2)
      digits.startsWith("0") -> "966${digits.drop(1)}"
      else -> digits
    }
  }

  fun checkUsedAdCustomerSubscription(
    phone: String,
    onComplete: (UsedAdCustomerSubscription?, String?) -> Unit
  ) {
    val database = firestore
    val subscriptionId = normalizedSellerPhone(phone)
    if (database == null) {
      onComplete(null, _usedAdsError.value ?: "خدمة الاشتراكات غير متاحة حالياً.")
      return
    }
    if (subscriptionId.length < 10) {
      onComplete(null, "أدخل رقم جوال صحيحاً.")
      return
    }

    val subscriptionDocument = database.collection(USED_AD_SUBSCRIPTIONS_COLLECTION).document(subscriptionId)
    subscriptionDocument.get()
      .addOnSuccessListener { document ->
        val subscription = parseUsedAdCustomerSubscription(document)
          ?: UsedAdCustomerSubscription(id = subscriptionId, sellerPhone = phone.trim())
        val effectiveSubscription = if (
          subscription.status == UsedAdSubscriptionStatus.ACTIVE &&
          subscription.expiresAt <= System.currentTimeMillis()
        ) {
          subscriptionDocument.update("status", UsedAdSubscriptionStatus.EXPIRED.name)
            .addOnFailureListener { updateError ->
              Log.e(TAG, "Failed to expire used-ad subscription $subscriptionId during status check", updateError)
            }
          subscription.copy(status = UsedAdSubscriptionStatus.EXPIRED)
        } else {
          subscription
        }
        onComplete(effectiveSubscription, null)
      }
      .addOnFailureListener { error ->
        Log.e(TAG, "Failed to check used-ad subscription for $subscriptionId", error)
        onComplete(null, "تعذر التحقق من الاشتراك: ${error.localizedMessage ?: "خطأ غير معروف"}")
      }
  }

  fun saveUsedAdCustomerSubscription(
    subscription: UsedAdCustomerSubscription,
    onComplete: (String?) -> Unit
  ) {
    if (!_currentUser.value.isOwner) {
      onComplete("يقتصر تعديل اشتراكات سوق المستعمل على مالك المتجر.")
      return
    }
    val subscriptionId = normalizedSellerPhone(subscription.sellerPhone)
    if (
      subscription.sellerName.isBlank() || subscription.sellerPhone.isBlank() ||
      subscriptionId.length < 10 || subscription.productLimit <= 0 ||
      !subscription.agreedPrice.isFinite() || subscription.agreedPrice <= 0.0 ||
      (subscription.durationDays != 60 && subscription.durationDays != 90)
    ) {
      onComplete("أدخل اسم العميل ورقم جواله وعدد منتجات وسعراً صحيحاً، واختر مدة 60 أو 90 يوماً.")
      return
    }
    val database = firestore
    if (database == null) {
      onComplete(_usedAdSubscriptionsError.value ?: "خدمة الاشتراكات غير متاحة حالياً.")
      return
    }

    val subscriptionDocument = database.collection(USED_AD_SUBSCRIPTIONS_COLLECTION).document(subscriptionId)
    val now = System.currentTimeMillis()
    database.runTransaction { transaction ->
      val existing = transaction.get(subscriptionDocument)
      val existingSubscription = parseUsedAdCustomerSubscription(existing)
      val activeSubscription = subscription.copy(
        id = subscriptionId,
        sellerName = subscription.sellerName.trim(),
        sellerPhone = subscription.sellerPhone.trim(),
        submittedProductCount = existingSubscription
          ?.takeIf {
            it.status == UsedAdSubscriptionStatus.ACTIVE && it.expiresAt > now
          }
          ?.submittedProductCount ?: 0,
        status = UsedAdSubscriptionStatus.ACTIVE,
        createdAt = now,
        expiresAt = now + subscription.durationDays * 24L * 60L * 60L * 1000L
      )
      transaction.set(subscriptionDocument, activeSubscription.toFirestoreMap())
      activeSubscription
    }.addOnSuccessListener {
      _usedAdSubscriptionsError.value = null
      onComplete(null)
    }.addOnFailureListener { error ->
      _usedAdSubscriptionsError.value =
        "تعذر حفظ اشتراك العميل: ${error.localizedMessage ?: "خطأ غير معروف"}"
      Log.e(TAG, "Failed to save used-ad customer subscription", error)
      onComplete(_usedAdSubscriptionsError.value)
    }
  }

  fun updateUsedAdCustomerSubscriptionStatus(
    subscription: UsedAdCustomerSubscription,
    status: UsedAdSubscriptionStatus,
    onComplete: (String?) -> Unit
  ) {
    if (!_currentUser.value.isOwner) {
      onComplete("يقتصر تعديل اشتراكات سوق المستعمل على مالك المتجر.")
      return
    }
    val database = firestore
    if (database == null) {
      onComplete(_usedAdSubscriptionsError.value ?: "خدمة الاشتراكات غير متاحة حالياً.")
      return
    }
    val now = System.currentTimeMillis()
    val subscriptionDocument = database.collection(USED_AD_SUBSCRIPTIONS_COLLECTION).document(subscription.id)
    val updates = mutableMapOf<String, Any>("status" to status.name)
    if (status == UsedAdSubscriptionStatus.ACTIVE) {
      updates["createdAt"] = now
      updates["expiresAt"] = now + subscription.durationDays * 24L * 60L * 60L * 1000L
    } else if (status == UsedAdSubscriptionStatus.EXPIRED) {
      updates["expiresAt"] = now
    }
    subscriptionDocument.update(updates)
      .addOnSuccessListener {
        if (status == UsedAdSubscriptionStatus.ACTIVE) {
          onComplete(null)
        } else {
          val activeAdsQuery = database.collection(USED_ADS_COLLECTION)
            .whereEqualTo("subscriptionId", subscription.id)
            .whereEqualTo("status", UsedCatalyticAdStatus.ACTIVE.name)
          activeAdsQuery.get()
            .addOnSuccessListener { snapshot ->
              val batch = database.batch()
              snapshot.documents.forEach { document ->
                batch.update(
                  document.reference,
                  mapOf(
                    "status" to UsedCatalyticAdStatus.EXPIRED.name,
                    "expiresAt" to now
                  )
                )
              }
              batch.commit()
                .addOnSuccessListener { onComplete(null) }
                .addOnFailureListener { error ->
                  Log.e(TAG, "Failed to deactivate listings for subscription ${subscription.id}", error)
                  onComplete("تم تحديث الاشتراك لكن تعذر إخفاء بعض إعلاناته: ${error.localizedMessage ?: "خطأ غير معروف"}")
                }
            }
            .addOnFailureListener { error ->
              Log.e(TAG, "Failed to find listings for subscription ${subscription.id}", error)
              onComplete("تم تحديث الاشتراك لكن تعذر إخفاء إعلاناته: ${error.localizedMessage ?: "خطأ غير معروف"}")
            }
        }
      }
      .addOnFailureListener { error ->
        _usedAdSubscriptionsError.value =
          "تعذر تحديث حالة الاشتراك: ${error.localizedMessage ?: "خطأ غير معروف"}"
        Log.e(TAG, "Failed to update used-ad subscription status ${subscription.id}", error)
        onComplete(_usedAdSubscriptionsError.value)
      }
  }

  fun submitUsedCatalyticAd(ad: UsedCatalyticAd, onComplete: (String?) -> Unit) {
    val database = firestore
    if (database == null) {
      onComplete(_usedAdsError.value ?: "خدمة الإعلانات غير متاحة حالياً.")
      return
    }
    if (
      ad.sellerName.isBlank() || ad.sellerPhone.isBlank() || ad.city.isBlank() ||
      ad.vehicleType.isBlank() || ad.vehicleModel.isBlank() || ad.imageUrl.isBlank() ||
      !ad.price.isFinite() || ad.price <= 0.0
    ) {
      onComplete("أكمل بيانات البائع والسيارة والسعر ورابط الصورة.")
      return
    }

    val subscriptionId = normalizedSellerPhone(ad.sellerPhone)
    val subscriptionDocument = database.collection(USED_AD_SUBSCRIPTIONS_COLLECTION).document(subscriptionId)
    val adDocument = database.collection(USED_ADS_COLLECTION).document(UUID.randomUUID().toString())
    val now = System.currentTimeMillis()
    database.runTransaction { transaction ->
      val subscriptionSnapshot = transaction.get(subscriptionDocument)
      val subscription = parseUsedAdCustomerSubscription(subscriptionSnapshot)
        ?: throw IllegalStateException("رقم الجوال غير مشترك في سوق المنتجات المستعملة.")
      if (subscription.status != UsedAdSubscriptionStatus.ACTIVE || subscription.expiresAt <= now) {
        throw IllegalStateException(
          if (subscription.expiresAt <= now) "انتهت مدة الاشتراك." else "العميل غير مشترك حالياً."
        )
      }
      if (subscription.submittedProductCount >= subscription.productLimit) {
        throw IllegalStateException("اكتمل عدد المنتجات المسموح به في الاشتراك.")
      }
      val pendingAd = ad.copy(
        id = adDocument.id,
        subscriptionDays = subscription.durationDays,
        subscriptionId = subscription.id,
        subscriptionPaidAmount = 0.0,
        status = UsedCatalyticAdStatus.PENDING_PAYMENT,
        createdAt = now,
        expiresAt = 0L
      )
      transaction.update(
        subscriptionDocument,
        "submittedProductCount",
        subscription.submittedProductCount + 1
      )
      transaction.set(adDocument, pendingAd.toFirestoreMap())
    }.addOnSuccessListener {
      onComplete(null)
    }.addOnFailureListener { error ->
      val message = error.localizedMessage ?: "خطأ غير معروف"
      _usedAdsError.value = "تعذر إرسال طلب الإعلان: $message"
      Log.e(TAG, "Failed to submit used-ad listing", error)
      onComplete(_usedAdsError.value)
    }
  }

  fun approveUsedCatalyticAd(ad: UsedCatalyticAd, onComplete: (String?) -> Unit) {
    if (!_currentUser.value.isOwner) {
      onComplete("يقتصر اعتماد الإعلانات على مالك المتجر.")
      return
    }
    if (ad.status != UsedCatalyticAdStatus.PENDING_PAYMENT) {
      onComplete("هذا الإعلان ليس بانتظار الدفع.")
      return
    }
    val database = firestore
    if (database == null) {
      onComplete(_usedAdsError.value ?: "خدمة الإعلانات غير متاحة حالياً.")
      return
    }

    val now = System.currentTimeMillis()
    val subscriptionId = ad.subscriptionId.ifBlank { normalizedSellerPhone(ad.sellerPhone) }
    val subscriptionDocument = database.collection(USED_AD_SUBSCRIPTIONS_COLLECTION).document(subscriptionId)
    val adDocument = database.collection(USED_ADS_COLLECTION).document(ad.id)
    database.runTransaction { transaction ->
      val subscriptionSnapshot = transaction.get(subscriptionDocument)
      val currentSubscription = parseUsedAdCustomerSubscription(subscriptionSnapshot)
        ?: throw IllegalStateException("اشتراك البائع غير موجود.")
      if (
        currentSubscription.status != UsedAdSubscriptionStatus.ACTIVE ||
        currentSubscription.expiresAt <= now
      ) {
        throw IllegalStateException("لا يمكن اعتماد الإعلان؛ اشتراك البائع غير نشط أو منتهي.")
      }
      val adExpiresAt = minOf(
        now + currentSubscription.durationDays * 24L * 60L * 60L * 1000L,
        currentSubscription.expiresAt
      )
      transaction.update(
        adDocument,
        mapOf(
          "status" to UsedCatalyticAdStatus.ACTIVE.name,
          "subscriptionDays" to currentSubscription.durationDays,
          "subscriptionId" to currentSubscription.id,
          "expiresAt" to adExpiresAt
        )
      )
    }.addOnSuccessListener {
      onComplete(null)
    }.addOnFailureListener { error ->
      _usedAdsError.value = "تعذر اعتماد الإعلان: ${error.localizedMessage ?: "خطأ غير معروف"}"
      Log.e(TAG, "Failed to approve used-ad listing ${ad.id}", error)
      onComplete(_usedAdsError.value)
    }
  }

  private fun startOrdersListener() {
    val database = firestore ?: return
    if (ordersListener != null) return

    _ordersLoading.value = true
    _ordersSyncError.value = null
    ordersListener = database.collection(ORDERS_COLLECTION)
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          _ordersLoading.value = false
          _ordersSyncError.value = "تعذرت مزامنة الطلبات: ${error.localizedMessage ?: "خطأ غير معروف"}"
          Log.e(TAG, "Failed to listen for orders", error)
        } else if (snapshot != null) {
          _orders.value = snapshot.documents.mapNotNull(::parseOrder)
          _ordersLoading.value = false
          _ordersSyncError.value = null
        }
      }
  }

  private fun stopOrdersListener() {
    ordersListener?.remove()
    ordersListener = null
    _orders.value = emptyList()
    _ordersLoading.value = false
  }

  // Store Settings Customization (Name, Colors, Videos, Marquee, Policies)
  fun updateStoreSettings(newSettings: StoreSettings) {
    _storeSettings.value = newSettings
    persistStoreSettings(newSettings)
  }

  fun sendOwnerWhatsAppLeadAlert(
    customerName: String,
    customerPhone: String,
    requestSummary: String,
    onComplete: (String?) -> Unit
  ) {
    val settings = _storeSettings.value
    if (!settings.whatsappAlertsEnabled) {
      onComplete("تنبيهات واتساب غير مفعلة من لوحة المالك.")
      return
    }
    if (customerName.isBlank() || customerPhone.isBlank() || requestSummary.isBlank()) {
      onComplete("أدخل اسم العميل ورقم جواله وملخص الطلب لإرسال التنبيه.")
      return
    }
    if (settings.ownerWhatsappPhone.isBlank() || settings.whatsappApiKey.isBlank()) {
      onComplete("أكمل رقم واتساب المالك ومفتاح API في إعدادات التنبيهات.")
      return
    }

    val ownerPhone = toInternationalWhatsappPhone(settings.ownerWhatsappPhone)
    if (ownerPhone.length < 10) {
      onComplete("رقم واتساب المالك غير صالح.")
      return
    }
    val message = """
تنبيه عميل مهتم
الاسم: ${customerName.trim().take(100)}
رقم الجوال: ${customerPhone.trim().take(40)}
ملخص الطلب: ${requestSummary.trim().take(1200)}
    """.trimIndent()

    val requestUrl = HttpUrl.Builder()
      .scheme("https")
      .host("api.callmebot.com")
      .addPathSegment("whatsapp.php")
      .addQueryParameter("phone", ownerPhone)
      .addQueryParameter("text", message)
      .addQueryParameter("apikey", settings.whatsappApiKey.trim())
      .build()
    val request = Request.Builder().url(requestUrl).get().build()
    val mainHandler = Handler(Looper.getMainLooper())
    try {
      whatsappAlertHttpClient.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
          Log.e(TAG, "WhatsApp owner alert request failed (${e.javaClass.simpleName})")
          mainHandler.post { onComplete("تعذر الاتصال ببوابة واتساب لإرسال التنبيه.") }
        }

        override fun onResponse(call: Call, response: Response) {
          response.use {
            val errorMessage = if (it.isSuccessful) {
              null
            } else {
              "رفضت بوابة واتساب الإرسال (HTTP ${it.code})."
            }
            mainHandler.post { onComplete(errorMessage) }
          }
        }
      })
    } catch (error: IllegalArgumentException) {
      Log.e(TAG, "Invalid WhatsApp owner alert request (${error.javaClass.simpleName})")
      onComplete("تعذر تجهيز طلب إرسال التنبيه.")
    }
  }

  private fun toInternationalWhatsappPhone(phone: String): String {
    val digits = phone.filter(Char::isDigit)
    return when {
      digits.startsWith("00966") -> "+${digits.drop(2)}"
      digits.startsWith("0") -> "+966${digits.drop(1)}"
      digits.startsWith("966") -> "+$digits"
      else -> "+966$digits"
    }
  }

  private fun loadStoreSettings(): StoreSettings {
    fun readString(key: String, default: String): String =
      settingsPreferences.getString(key, default) ?: default

    val default = defaultStoreSettings
    val speedName = readString("marqueeSpeed", default.marqueeSpeed.name)
    val speed = MarqueeSpeed.values().firstOrNull { it.name == speedName } ?: default.marqueeSpeed
    return default.copy(
      storeName = readString("storeName", default.storeName),
      storeShareLink = readString("storeShareLink", default.storeShareLink),
      marqueeText = readString("marqueeText", default.marqueeText),
      marqueeSpeed = speed,
      marqueeBgColorHex = readString("marqueeBgColorHex", default.marqueeBgColorHex),
      headerVideoTitle = readString("headerVideoTitle", default.headerVideoTitle),
      headerVideoDesc = readString("headerVideoDesc", default.headerVideoDesc),
      headerVideoPlaceholder = readString("headerVideoPlaceholder", default.headerVideoPlaceholder),
      headerVideoUrl = readString("headerVideoUrl", default.headerVideoUrl),
      headerVideoBackground = readString("headerVideoBackground", default.headerVideoBackground),
      footerVideoTitle = readString("footerVideoTitle", default.footerVideoTitle),
      footerVideoDesc = readString("footerVideoDesc", default.footerVideoDesc),
      footerVideoPlaceholder = readString("footerVideoPlaceholder", default.footerVideoPlaceholder),
      footerVideoUrl = readString("footerVideoUrl", default.footerVideoUrl),
      footerVideoBackground = readString("footerVideoBackground", default.footerVideoBackground),
      mainBackgroundImage = readString("mainBackgroundImage", default.mainBackgroundImage),
      activeTheme = readString("activeTheme", default.activeTheme),
      whatsappNumber = readString("whatsappNumber", default.whatsappNumber),
      ownerWhatsappPhone = readString("ownerWhatsappPhone", default.ownerWhatsappPhone),
      whatsappApiKey = readString("whatsappApiKey", default.whatsappApiKey),
      whatsappAlertsEnabled = settingsPreferences.getBoolean(
        "whatsappAlertsEnabled",
        default.whatsappAlertsEnabled
      ),
      whatsappUrl = readString("whatsappUrl", default.whatsappUrl),
      storeEmail = readString("storeEmail", default.storeEmail),
      storePhone = readString("storePhone", default.storePhone),
      footerDescription = readString("footerDescription", default.footerDescription),
      instagramUrl = readString("instagramUrl", default.instagramUrl),
      tiktokUrl = readString("tiktokUrl", default.tiktokUrl),
      youtubeUrl = readString("youtubeUrl", default.youtubeUrl),
      snapchatUrl = readString("snapchatUrl", default.snapchatUrl),
      xTwitterUrl = readString("xTwitterUrl", default.xTwitterUrl),
      aboutUsUrl = readString("aboutUsUrl", default.aboutUsUrl),
      complaintsUrl = readString("complaintsUrl", default.complaintsUrl),
      returnPolicyUrl = readString("returnPolicyUrl", default.returnPolicyUrl),
      aboutUsText = readString("aboutUsText", default.aboutUsText),
      returnPolicyText = readString("returnPolicyText", default.returnPolicyText)
    )
  }

  private fun persistStoreSettings(settings: StoreSettings) {
    settingsPreferences.edit()
      .putString("storeName", settings.storeName)
      .putString("storeShareLink", settings.storeShareLink)
      .putString("marqueeText", settings.marqueeText)
      .putString("marqueeSpeed", settings.marqueeSpeed.name)
      .putString("marqueeBgColorHex", settings.marqueeBgColorHex)
      .putString("headerVideoTitle", settings.headerVideoTitle)
      .putString("headerVideoDesc", settings.headerVideoDesc)
      .putString("headerVideoPlaceholder", settings.headerVideoPlaceholder)
      .putString("headerVideoUrl", settings.headerVideoUrl)
      .putString("headerVideoBackground", settings.headerVideoBackground)
      .putString("footerVideoTitle", settings.footerVideoTitle)
      .putString("footerVideoDesc", settings.footerVideoDesc)
      .putString("footerVideoPlaceholder", settings.footerVideoPlaceholder)
      .putString("footerVideoUrl", settings.footerVideoUrl)
      .putString("footerVideoBackground", settings.footerVideoBackground)
      .putString("mainBackgroundImage", settings.mainBackgroundImage)
      .putString("activeTheme", settings.activeTheme)
      .putString("whatsappNumber", settings.whatsappNumber)
      .putString("ownerWhatsappPhone", settings.ownerWhatsappPhone)
      .putString("whatsappApiKey", settings.whatsappApiKey)
      .putBoolean("whatsappAlertsEnabled", settings.whatsappAlertsEnabled)
      .putString("whatsappUrl", settings.whatsappUrl)
      .putString("storeEmail", settings.storeEmail)
      .putString("storePhone", settings.storePhone)
      .putString("footerDescription", settings.footerDescription)
      .putString("instagramUrl", settings.instagramUrl)
      .putString("tiktokUrl", settings.tiktokUrl)
      .putString("youtubeUrl", settings.youtubeUrl)
      .putString("snapchatUrl", settings.snapchatUrl)
      .putString("xTwitterUrl", settings.xTwitterUrl)
      .putString("aboutUsUrl", settings.aboutUsUrl)
      .putString("complaintsUrl", settings.complaintsUrl)
      .putString("returnPolicyUrl", settings.returnPolicyUrl)
      .putString("aboutUsText", settings.aboutUsText)
      .putString("returnPolicyText", settings.returnPolicyText)
      .apply()
  }

  fun updateStoreName(newName: String) {
    updateStoreSettings(_storeSettings.value.copy(storeName = newName))
  }

  fun updateTheme(paletteName: String) {
    updateStoreSettings(_storeSettings.value.copy(activeTheme = paletteName))
  }

  fun updateMarquee(text: String, speed: MarqueeSpeed, bgColor: String) {
    updateStoreSettings(
      _storeSettings.value.copy(
        marqueeText = text,
        marqueeSpeed = speed,
        marqueeBgColorHex = bgColor
      )
    )
  }

  fun updateVideos(headerTitle: String, headerDesc: String, footerTitle: String, footerDesc: String) {
    updateStoreSettings(
      _storeSettings.value.copy(
        headerVideoTitle = headerTitle,
        headerVideoDesc = headerDesc,
        footerVideoTitle = footerTitle,
        footerVideoDesc = footerDesc
      )
    )
  }

  fun createOrder(order: Order, onComplete: (String?) -> Unit) {
    val database = firestore
    if (database == null) {
      onComplete(_ordersSyncError.value ?: "خدمة الطلبات غير متاحة حالياً.")
      return
    }
    if (order.orderNumber.isBlank()) {
      onComplete("تعذر إنشاء الطلب: رقم الطلب غير صالح.")
      return
    }

    val now = System.currentTimeMillis()
    val savedOrder = order.copy(createdAt = now, updatedAt = now)
    database.collection(ORDERS_COLLECTION)
      .document(savedOrder.orderNumber)
      .set(savedOrder.toFirestoreMap())
      .addOnSuccessListener {
        _ordersSyncError.value = null
        onComplete(null)
      }
      .addOnFailureListener { error ->
        _ordersSyncError.value = "تعذر حفظ الطلب: ${error.localizedMessage ?: "خطأ غير معروف"}"
        Log.e(TAG, "Failed to create order ${savedOrder.orderNumber}", error)
        onComplete(_ordersSyncError.value)
      }
  }

  fun updateOrder(order: Order, onComplete: (String?) -> Unit) {
    val database = firestore
    if (database == null) {
      onComplete(_ordersSyncError.value ?: "خدمة الطلبات غير متاحة حالياً.")
      return
    }
    if (order.orderNumber.isBlank()) {
      onComplete("تعذر تحديث الطلب: رقم الطلب غير صالح.")
      return
    }

    database.collection(ORDERS_COLLECTION)
      .document(order.orderNumber)
      .update(
        mapOf(
          "status" to order.status.name,
          "updateNote" to order.updateNote,
          "updatedAt" to System.currentTimeMillis()
        )
      )
      .addOnSuccessListener {
        _ordersSyncError.value = null
        onComplete(null)
      }
      .addOnFailureListener { error ->
        _ordersSyncError.value = "تعذر تحديث الطلب: ${error.localizedMessage ?: "خطأ غير معروف"}"
        Log.e(TAG, "Failed to update order ${order.orderNumber}", error)
        onComplete(_ordersSyncError.value)
      }
  }

  fun listenToOrder(
    orderNumber: String,
    onUpdate: (Order?, String?) -> Unit
  ): ListenerRegistration? {
    val database = firestore
    if (database == null) {
      onUpdate(null, _ordersSyncError.value ?: "خدمة تتبع الطلبات غير متاحة حالياً.")
      return null
    }
    if (orderNumber.isBlank()) {
      onUpdate(null, "أدخل رقم الطلب للمتابعة.")
      return null
    }

    return database.collection(ORDERS_COLLECTION)
      .document(orderNumber.trim())
      .addSnapshotListener { snapshot, error ->
        when {
          error != null -> {
            Log.e(TAG, "Failed to listen for order $orderNumber", error)
            onUpdate(null, "تعذر تحميل حالة الطلب: ${error.localizedMessage ?: "خطأ غير معروف"}")
          }
          snapshot?.exists() == true -> {
            val order = parseOrder(snapshot)
            if (order == null) onUpdate(null, "بيانات الطلب المحفوظة غير مكتملة.")
            else onUpdate(order, null)
          }
          else -> onUpdate(null, "لم يتم العثور على طلب بهذا الرقم.")
        }
      }
  }

  private fun Order.toFirestoreMap(): Map<String, Any> = mapOf(
    "orderNumber" to orderNumber,
    "customerName" to customerName,
    "customerContact" to customerContact,
    "products" to products.map { product ->
      mapOf(
        "productId" to product.productId,
        "productName" to product.productName,
        "unitPrice" to product.unitPrice,
        "selectedOptions" to product.selectedOptions,
        "customerNote" to product.customerNote
      )
    },
    "status" to status.name,
    "updateNote" to updateNote,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
  )

  private fun parseOrder(document: DocumentSnapshot): Order? {
    val orderNumber = document.getString("orderNumber") ?: return null
    val products = (document.get("products") as? List<*>).orEmpty().mapNotNull { item ->
      val fields = item as? Map<*, *> ?: return@mapNotNull null
      OrderProduct(
        productId = fields["productId"] as? String ?: "",
        productName = fields["productName"] as? String ?: "",
        unitPrice = (fields["unitPrice"] as? Number)?.toDouble() ?: 0.0,
        selectedOptions = (fields["selectedOptions"] as? List<*>)?.filterIsInstance<String>().orEmpty(),
        customerNote = fields["customerNote"] as? String ?: ""
      )
    }
    val statusName = document.getString("status")
    val status = OrderStatus.values().firstOrNull { it.name == statusName } ?: OrderStatus.RECEIVED
    return Order(
      orderNumber = orderNumber,
      customerName = document.getString("customerName") ?: "",
      customerContact = document.getString("customerContact") ?: "",
      products = products,
      status = status,
      updateNote = document.getString("updateNote") ?: "",
      createdAt = document.getLong("createdAt") ?: 0L,
      updatedAt = document.getLong("updatedAt") ?: 0L
    )
  }

  // Product Management
  fun addProduct(product: Product) {
    _products.update { listOf(product) + it }
  }

  fun updateProduct(product: Product) {
    _products.update { current ->
      current.map { if (it.id == product.id) product else it }
    }
  }

  fun deleteProduct(productId: String) {
    _products.update { current ->
      current.filterNot { it.id == productId }
    }
  }

  // Scrape and import product via URL
  suspend fun importProductFromUrl(url: String): Product {
    val scraped = UrlProductScraper.scrapeUrl(url)
    // Run AI to optimize Arabic copywriting and the 4 options:
    val aiDetails = AiDescriptionGenerator.generateProductDetails(
      inputPrompt = scraped.title,
      categoryHint = "دبات التلوث"
    )

    val newProduct = Product(
      id = "imp-${System.currentTimeMillis() % 10000}",
      title = if (scraped.title.isNotBlank()) scraped.title else aiDetails.title,
      description = if (scraped.description.isNotBlank()) "${scraped.description}\n\n${aiDetails.description}" else aiDetails.description,
      price = scraped.price ?: aiDetails.suggestedPrice,
      originalPrice = (scraped.price ?: aiDetails.suggestedPrice) * 1.25,
      category = "مستورد بالرابط",
      rating = 4.9,
      reviewCount = 12,
      imageDrawableName = "prod_cat_converter_1794448832780",
      customImageUrl = scraped.imageUrl,
      option1Label = aiDetails.option1Label,
      option1Values = aiDetails.option1Values,
      option2Label = aiDetails.option2Label,
      option2Values = aiDetails.option2Values,
      option3Label = aiDetails.option3Label,
      option3Values = aiDetails.option3Values,
      option4Label = aiDetails.option4Label,
      option4Values = aiDetails.option4Values,
      importedFromUrl = url
    )

    addProduct(newProduct)
    return newProduct
  }

  // Complaints / Suggestions
  fun submitComplaint(name: String, contact: String, type: String, content: String) {
    val item = Complaint(
      id = "c-${System.currentTimeMillis() % 10000}",
      customerName = name,
      customerContact = contact,
      type = type,
      content = content,
      timestamp = System.currentTimeMillis()
    )
    _complaints.update { listOf(item) + it }
  }
}
