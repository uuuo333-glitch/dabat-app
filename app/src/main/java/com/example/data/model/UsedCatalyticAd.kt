package com.example.data.model

data class UsedCatalyticAd(
  val id: String = "",
  val sellerName: String = "",
  val sellerPhone: String = "",
  val city: String = "",
  val vehicleType: String = "",
  val vehicleModel: String = "",
  val catalyticCondition: CatalyticCondition = CatalyticCondition.USED,
  val price: Double = 0.0,
  val imageUrl: String = "",
  val subscriptionDays: Int = 60,
  val subscriptionId: String = "",
  val subscriptionPaidAmount: Double = 0.0,
  val status: UsedCatalyticAdStatus = UsedCatalyticAdStatus.PENDING_PAYMENT,
  val createdAt: Long = 0L,
  val expiresAt: Long = 0L
)

enum class CatalyticCondition(val labelArabic: String) {
  AGENCY_GOOD("وكالة سليمة"),
  USED("مستعملة")
}

enum class UsedCatalyticAdStatus(val labelArabic: String) {
  PENDING_PAYMENT("بانتظار مراجعة المالك"),
  ACTIVE("نشط"),
  EXPIRED("منتهي")
}

data class UsedAdCustomerSubscription(
  val id: String = "",
  val sellerName: String = "",
  val sellerPhone: String = "",
  val productLimit: Int = 0,
  val submittedProductCount: Int = 0,
  val agreedPrice: Double = 0.0,
  val durationDays: Int = 60,
  val status: UsedAdSubscriptionStatus = UsedAdSubscriptionStatus.UNSUBSCRIBED,
  val createdAt: Long = 0L,
  val expiresAt: Long = 0L
) {
  val remainingProducts: Int
    get() = (productLimit - submittedProductCount).coerceAtLeast(0)
}

enum class UsedAdSubscriptionStatus(val labelArabic: String) {
  ACTIVE("مشترك"),
  UNSUBSCRIBED("غير مشترك"),
  EXPIRED("انتهت مدة الاشتراك")
}
