package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Product
import com.example.service.shareProduct
import com.example.ui.theme.StarGold
import com.example.ui.theme.WhatsAppGreen

@Composable
fun ProductCard(
  product: Product,
  onClick: () -> Unit,
  storeName: String = "المتجر",
  storeShareLink: String = "https://dabat-altalawuth.sa",
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  val fallbackDrawableId = remember(product.imageDrawableName) {
    val id = context.resources.getIdentifier(product.imageDrawableName, "drawable", context.packageName)
    if (id != 0) id else R.drawable.prod_cat_converter_1794448832780
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("product_card_${product.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Column {
      // Product Image Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(185.dp)
          .background(Color(0xFF0F172A))
      ) {
        Image(
          painter = painterResource(id = fallbackDrawableId),
          contentDescription = product.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Category Badge
        Surface(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(10.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(
            text = product.category,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }

        IconButton(
          onClick = { shareProduct(context, product, storeName, storeShareLink) },
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(6.dp)
            .size(34.dp)
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(17.dp))
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "مشاركة المنتج",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
        }

        // In-stock badge
        if (product.inStock) {
          Row(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(8.dp)
              .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = WhatsAppGreen,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "متوفر بالمخزن",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }

      // Details
      Column(modifier = Modifier.padding(14.dp)) {
        // Rating
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(bottom = 4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = StarGold,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "${product.rating} (${product.reviewCount} تقييم)",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )
        }

        // Title
        Text(
          text = product.title,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Price Section
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "${product.price.toInt()} ر.س",
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 18.sp
            )
            if (product.originalPrice != null && product.originalPrice > product.price) {
              Text(
                text = "${product.originalPrice.toInt()} ر.س",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                textDecoration = TextDecoration.LineThrough
              )
            }
          }

          // Order / View details button
          Button(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
            modifier = Modifier.height(38.dp)
          ) {
            Text(
              text = "الطلب والتفاصيل",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }
        }
      }
    }
  }
}
