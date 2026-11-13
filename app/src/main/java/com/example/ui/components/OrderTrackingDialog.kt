package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Order
import com.example.data.repository.StoreRepository

@Composable
fun OrderTrackingDialog(
  repository: StoreRepository,
  onDismiss: () -> Unit
) {
  var orderNumberInput by remember { mutableStateOf("") }
  var requestedOrderNumber by remember { mutableStateOf("") }
  var trackedOrder by remember { mutableStateOf<Order?>(null) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  DisposableEffect(requestedOrderNumber) {
    if (requestedOrderNumber.isNotBlank()) {
      trackedOrder = null
      errorMessage = null
      isLoading = true
    }
    val registration = if (requestedOrderNumber.isBlank()) {
      null
    } else {
      repository.listenToOrder(requestedOrderNumber) { order, error ->
        trackedOrder = order
        errorMessage = error
        isLoading = false
      }
    }
    onDispose { registration?.remove() }
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "تتبع طلباتي",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "أدخل رقم الطلب الموجود في رسالة تأكيد واتساب لمتابعة حالته مباشرة.",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = orderNumberInput,
          onValueChange = { orderNumberInput = it },
          label = { Text("رقم الطلب") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
          onClick = {
            requestedOrderNumber = orderNumberInput.trim()
            if (requestedOrderNumber.isBlank()) {
              errorMessage = "أدخل رقم الطلب للمتابعة."
            }
          },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("تتبع الطلب")
        }

        if (isLoading) {
          Spacer(modifier = Modifier.height(12.dp))
          Text("جارٍ تحميل حالة الطلب…", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        errorMessage?.let { message ->
          Spacer(modifier = Modifier.height(12.dp))
          Text(message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
        trackedOrder?.let { order ->
          Spacer(modifier = Modifier.height(16.dp))
          OrderStatusDetails(order)
        }
      }
    }
  }
}

@Composable
private fun OrderStatusDetails(order: Order) {
  Column {
    Text("رقم الطلب: ${order.orderNumber}", fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "الحالة الحالية: ${order.status.labelArabic}",
      color = MaterialTheme.colorScheme.primary,
      fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))
    order.products.forEach { product ->
      Text(product.productName, fontWeight = FontWeight.SemiBold)
      if (product.selectedOptions.isNotEmpty()) {
        Text(
          product.selectedOptions.joinToString(" • "),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 12.sp
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
    }
    if (order.updateNote.isNotBlank()) {
      Spacer(modifier = Modifier.height(8.dp))
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
      ) {
        Text(
          text = "ملاحظة المالك: ${order.updateNote}",
          modifier = Modifier.padding(10.dp),
          fontSize = 13.sp
        )
      }
    }
  }
}
