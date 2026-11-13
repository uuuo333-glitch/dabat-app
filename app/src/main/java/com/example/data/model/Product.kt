package com.example.data.model

data class Product(
  val id: String,
  val title: String,
  val description: String,
  val price: Double,
  val originalPrice: Double? = null,
  val category: String = "قطع وتعديل سيارات",
  val rating: Double = 4.9,
  val reviewCount: Int = 38,
  val imageDrawableName: String = "prod_exhaust_quad",
  val customImageUrl: String? = null,
  val inStock: Boolean = true,
  // The 4 customizable options filled by the store owner:
  val option1Label: String = "خامة التصنيع",
  val option1Values: List<String> = listOf("تيتانيوم فاخر خفيف الوزن", "ستانلس ستيل 304 أصلي"),
  val option2Label: String = "مقاس القطر والمواسير",
  val option2Values: List<String> = listOf("2.5 بوصة (High Flow)", "3.0 بوصة (Race Spec)"),
  val option3Label: String = "لون وتشطيب الأطراف (Tips)",
  val option3Values: List<String> = listOf("أزرق محروق Burnt Blue", "كروم فضي لامع", "أسود كربوني مطفي"),
  val option4Label: String = "توافق الموديل وسنة الصنع",
  val option4Values: List<String> = listOf("موديلات 2020 - 2025", "موديلات 2015 - 2019"),
  val importedFromUrl: String? = null
)

data class StoreSettings(
  val storeName: String = "المتجر الذكي للسيارات والإلكترونيات",
  val storeShareLink: String = "https://dabat-altalawuth.sa",
  val marqueeText: String = "🔥 خصم خاص 20% لفترة محدودة على جميع أنظمة العادم الرياضية والقطع الإلكترونية | التوصيل متاح لجميع المدن | اطلب مباشرة عبر الواتساب",
  val marqueeSpeed: MarqueeSpeed = MarqueeSpeed.MEDIUM,
  val marqueeBgColorHex: String = "#D4AF37",
  val headerVideoTitle: String = "نظام العادم التيتانيوم الرياضي - صوت وأداء استثنائي",
  val headerVideoDesc: String = "شاهد عزم وصوت محرك السيارات المزودة بأنظمتنا الحصرية",
  val headerVideoPlaceholder: String = "hero_store_banner",
  val headerVideoUrl: String = "",
  val headerVideoBackground: String = "",
  val footerVideoTitle: String = "تجربة أحد عملائنا بعد التركيب والبرمجة",
  val footerVideoDesc: String = "فيديو توضيحي لطريقة التركيب واختبار الأداء الحقيقي على الداينو",
  val footerVideoPlaceholder: String = "hero_store_banner",
  val footerVideoUrl: String = "",
  val footerVideoBackground: String = "",
  val mainBackgroundImage: String = "",
  val activeTheme: String = "GOLD",
  val whatsappNumber: String = "0565911414",
  val ownerWhatsappPhone: String = "",
  val whatsappApiKey: String = "",
  val whatsappAlertsEnabled: Boolean = false,
  val whatsappUrl: String = "",
  val storeEmail: String = "dbattloth.221@gmail.com",
  val storePhone: String = "0565911414",
  val footerDescription: String = "المتجر الأول المتخصص في دبات التلوث وفلاتر البيئة للديزل والبنزين",
  val instagramUrl: String = "https://instagram.com",
  val tiktokUrl: String = "https://tiktok.com",
  val youtubeUrl: String = "https://youtube.com",
  val snapchatUrl: String = "https://snapchat.com",
  val xTwitterUrl: String = "https://x.com",
  val aboutUsUrl: String = "",
  val complaintsUrl: String = "",
  val returnPolicyUrl: String = "",
  val aboutUsText: String = """
نحن متجر رائد متخصص في توفير وتوريد أحدث قطع تعديل السيارات والأنظمة الرياضية والإلكترونيات الذكية عالية الجودة في المملكة. نلتزم بأعلى معايير الدقة والأداء وتوفير خدمة طلب مباشرة وسريعة عبر واتساب أعمال مع دعم فني مخصص وضمان شامل على كافة منتجاتنا.
  """.trimIndent(),
  val returnPolicyText: String = """
▪ يحق للعميل استبدال أو استرجاع المنتج خلال 7 أيام من تاريخ الاستلام في حالته الأصلية وبغلافه دون فتح أو استخدام.
▪ في حال وجود عيب مصنعي أو خطأ في المواصفات المطلوبة، يتحمل المتجر كافة تكاليف الشحن والاسترجاع فوراً.
▪ المنتجات المصممة أو المصنعة بطلب مخصص تخضع للضمان الفني ضد عيوب التصنيع.
▪ يتم استرداد المبالغ عبر نفس وسيلة الدفع أو التحويل البنكي خلال 3 إلى 5 أيام عمل.
  """.trimIndent()
)

enum class MarqueeSpeed(val labelArabic: String, val speedMillis: Int) {
  SLOW("بطيء", 80),
  MEDIUM("متوسط", 40),
  FAST("سريع", 18)
}

data class Complaint(
  val id: String,
  val customerName: String,
  val customerContact: String,
  val type: String, // اقتراح أو شكوى
  val content: String,
  val timestamp: Long = System.currentTimeMillis()
)

data class CustomerUser(
  val contactInfo: String = "",
  val displayName: String = "",
  val isOwner: Boolean = false,
  val isLoggedIn: Boolean = false
)

data class OwnerWallet(
  val bankName: String = "",
  val iban: String = "",
  val balanceHalalas: Long = 0L,
  val totalEarningsHalalas: Long = 0L,
  val totalPurchasesHalalas: Long = 0L,
  val totalWithdrawalsHalalas: Long = 0L
) {
  val balanceSar: Double get() = balanceHalalas / 100.0
  val totalEarningsSar: Double get() = totalEarningsHalalas / 100.0
  val totalPurchasesSar: Double get() = totalPurchasesHalalas / 100.0
  val totalWithdrawalsSar: Double get() = totalWithdrawalsHalalas / 100.0
}

enum class OwnerWalletTransactionType(val labelArabic: String) {
  PROFIT_DEPOSIT("إيداع أرباح"),
  INVENTORY_PURCHASE("شراء بضاعة"),
  PERSONAL_WITHDRAWAL("سحب للحساب الشخصي")
}

data class OwnerWalletTransaction(
  val id: String,
  val type: OwnerWalletTransactionType,
  val storeName: String = "",
  val orderNumber: String = "",
  val amountHalalas: Long,
  val createdAt: Long = System.currentTimeMillis()
) {
  val amountSar: Double get() = amountHalalas / 100.0
}

data class OwnerAuthenticationResult(
  val isAuthenticated: Boolean,
  val message: String,
  val challengeId: String? = null
)
