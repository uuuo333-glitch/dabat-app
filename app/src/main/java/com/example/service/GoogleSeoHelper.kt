package com.example.service

import android.content.Context
import android.content.Intent
import com.example.data.model.Product

object GoogleSeoHelper {

  /**
   * Generates official Schema.org JSON-LD structured data for Google Search Indexing & Google Shopping Rich Snippets.
   */
  fun generateProductSchemaJsonLd(product: Product, storeUrl: String = "https://dabat-altalawuth.sa"): String {
    val cleanDesc = product.description.replace("\n", " ").replace("\"", "\\\"").take(200)
    val imageUrl = if (product.customImageUrl != null) product.customImageUrl else "$storeUrl/images/${product.imageDrawableName}.jpg"
    val availability = if (product.inStock) "https://schema.org/InStock" else "https://schema.org/OutOfStock"

    return """
    {
      "@context": "https://schema.org/",
      "@type": "Product",
      "name": "${product.title}",
      "image": [
        "$imageUrl"
      ],
      "description": "$cleanDesc",
      "sku": "CAT-${product.id}",
      "brand": {
        "@type": "Brand",
        "name": "دبات التلوث الأصلية"
      },
      "category": "${product.category}",
      "aggregateRating": {
        "@type": "AggregateRating",
        "ratingValue": "${product.rating}",
        "reviewCount": "${product.reviewCount}"
      },
      "offers": {
        "@type": "Offer",
        "url": "$storeUrl/products/${product.id}",
        "priceCurrency": "SAR",
        "price": "${product.price}",
        "priceValidUntil": "2027-12-31",
        "availability": "$availability",
        "itemCondition": "https://schema.org/NewCondition"
      }
    }
    """.trimIndent()
  }

  /**
   * Shares product link with SEO-ready preview and WhatsApp buy link.
   */
  fun shareProductToWebAndGoogle(context: Context, product: Product) {
    val shareBody = """
    🚗 *${product.title}*
    💰 السعر: ${product.price} ر.س
    ⭐ التقييم: ${product.rating} / 5.0
    
    ${product.description.take(150)}...
    
    📲 للطلب المباشر والاستفسار عبر واتساب المعتمد:
    https://api.whatsapp.com/send?phone=966565911414&text=${android.net.Uri.encode("السلام عليكم، أود الاستفسار عن ${product.title}")}
    
    🔗 رابط أرشفة المتجر في جوجل:
    https://dabat-altalawuth.sa/products/${product.id}
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_SUBJECT, product.title)
      putExtra(Intent.EXTRA_TEXT, shareBody)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة المنتج لأرشفة جوجل وشبكات التواصل"))
  }
}
