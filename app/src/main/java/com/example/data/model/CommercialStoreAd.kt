package com.example.data.model

data class CommercialStoreAd(
  val id: String = "",
  val storeName: String = "",
  val goodsType: String = "",
  val imageUrls: List<String> = emptyList(),
  val videoUrl: String = "",
  val region: String = "",
  val city: String = "",
  val locationUrl: String = "",
  val contactPhone: String = "",
  val durationValue: Int = 30,
  val durationUnit: CommercialAdDurationUnit = CommercialAdDurationUnit.DAYS,
  val expiryTimestamp: Long = 0L,
  val status: CommercialStoreAdStatus = CommercialStoreAdStatus.PENDING_PAYMENT,
  val createdAt: Long = 0L
)

enum class CommercialAdDurationUnit(val labelArabic: String) {
  DAYS("أيام"),
  MONTHS("أشهر"),
  YEARS("سنة")
}

enum class CommercialStoreAdStatus(val labelArabic: String) {
  ACTIVE("نشط"),
  PENDING_PAYMENT("بانتظار الدفع"),
  EXPIRED("منتهي")
}
