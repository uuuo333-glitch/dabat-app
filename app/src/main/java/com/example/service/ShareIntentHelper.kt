package com.example.service

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.model.Product

fun shareStore(context: Context, storeName: String, storeShareLink: String) {
  shareText(
    context = context,
    title = "مشاركة المتجر",
    message = "$storeName\n$storeShareLink"
  )
}

fun shareProduct(
  context: Context,
  product: Product,
  storeName: String,
  storeShareLink: String
) {
  shareText(
    context = context,
    title = "مشاركة المنتج",
    message = """
${product.title}
السعر: ${product.price.toInt()} ر.س
المتجر: $storeName
$storeShareLink
    """.trimIndent()
  )
}

private fun shareText(context: Context, title: String, message: String) {
  val sendIntent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, message)
  }
  try {
    context.startActivity(Intent.createChooser(sendIntent, title))
  } catch (_: ActivityNotFoundException) {
    Toast.makeText(context, "لا يوجد تطبيق متاح لمشاركة الرابط", Toast.LENGTH_SHORT).show()
  }
}
