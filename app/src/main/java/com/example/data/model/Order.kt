package com.example.data.model

data class Order(
  val orderNumber: String = "",
  val customerName: String = "",
  val customerContact: String = "",
  val products: List<OrderProduct> = emptyList(),
  val status: OrderStatus = OrderStatus.RECEIVED,
  val updateNote: String = "",
  val createdAt: Long = 0L,
  val updatedAt: Long = 0L
)

data class OrderProduct(
  val productId: String = "",
  val productName: String = "",
  val unitPrice: Double = 0.0,
  val selectedOptions: List<String> = emptyList(),
  val customerNote: String = ""
)

enum class OrderStatus(val labelArabic: String) {
  RECEIVED("تم الاستلام"),
  PREPARING("جاري التجهيز"),
  SHIPPED("تم الشحن"),
  DELIVERED("تم التوصيل")
}
