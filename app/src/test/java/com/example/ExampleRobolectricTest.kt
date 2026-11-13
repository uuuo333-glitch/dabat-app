package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.StoreRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("دبات التلوث", appName)
  }

  @Test
  fun `verify store repository initialization and products`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = StoreRepository.getInstance(context)
    val products = repo.products.value
    assertTrue(products.isNotEmpty())

    val first = products.first()
    assertNotNull(first.title)
    assertTrue(first.option1Values.isNotEmpty())
    assertTrue(first.option2Values.isNotEmpty())
    assertTrue(first.option3Values.isNotEmpty())
    assertTrue(first.option4Values.isNotEmpty())

    val settings = repo.storeSettings.value
    assertEquals("0565911414", settings.whatsappNumber)
  }
}
