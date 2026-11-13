package com.example.service

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiDescriptionGen"

object AiDescriptionGenerator {

  data class AiGeneratedContent(
    val title: String,
    val description: String,
    val option1Label: String,
    val option1Values: List<String>,
    val option2Label: String,
    val option2Values: List<String>,
    val option3Label: String,
    val option3Values: List<String>,
    val option4Label: String,
    val option4Values: List<String>,
    val suggestedPrice: Double,
    val isPoweredByGemini: Boolean = true
  )

  // 1. Define schema for structured JSON output using Schema.obj
  private val productSchema = Schema.obj(
    properties = mapOf(
      "title" to Schema.string(description = "عنوان تسويقي جذاب للمنتج باللغة العربية"),
      "description" to Schema.string(description = "وصف تفصيلي احترافي ومواصفات فنية ومميزات للمنتج باللغة العربية"),
      "option1Label" to Schema.string(description = "اسم الخيار الأول (مثل: نوع الوقود والمحرك أو خامة التصنيع)"),
      "option1Values" to Schema.array(items = Schema.string(), description = "قائمة من 2 إلى 3 قيم ممكنة للخيار 1"),
      "option2Label" to Schema.string(description = "اسم الخيار الثاني (مثل: مقاس القطر أو كثافة الخلية)"),
      "option2Values" to Schema.array(items = Schema.string(), description = "قائمة من 2 إلى 3 قيم ممكنة للخيار 2"),
      "option3Label" to Schema.string(description = "اسم الخيار الثالث (مثل: لون الأطراف أو طريقة التثبيت)"),
      "option3Values" to Schema.array(items = Schema.string(), description = "قائمة من 2 إلى 3 قيم ممكنة للخيار 3"),
      "option4Label" to Schema.string(description = "اسم الخيار الرابع (مثل: سنة الصنع وتوافق الفحص الدوري)"),
      "option4Values" to Schema.array(items = Schema.string(), description = "قائمة من 2 إلى 3 قيم ممكنة للخيار 4"),
      "suggestedPrice" to Schema.double(description = "السعر المقترح بالريال السعودي")
    )
  )

  // 2. Configure responseMimeType and responseSchema in generationConfig
  private val config = generationConfig {
    responseMimeType = "application/json"
    responseSchema = productSchema
  }

  // 3. Initialize generative model lazily
  private val geminiModel by lazy {
    Firebase.ai.generativeModel(
      modelName = "gemini-3.8-flash",
      generationConfig = config
    )
  }

  /**
   * Generates high converting Arabic e-commerce product copy, technical specs,
   * and auto-configures the 4 blank options using Gemini 3.8 Flash via Firebase AI Logic.
   */
  suspend fun generateProductDetails(
    inputPrompt: String,
    categoryHint: String = "دبات التلوث وفلاتر الديزل والبنزين"
  ): AiGeneratedContent {
    try {
      val prompt = """
      أنت خبير كتابة إعلانات تقنية وتسويقية لمتجر إلكتروني سعودي متخصص في "دبات التلوث وفلاتر الديزل والبنزين والأنظمة الرياضية".
      المطلوب: توليد تفاصيل تسويقية كاملة باللغة العربية لمنتج بناءً على الوصف أو الاسم التالي:
      "$inputPrompt"
      التصنيف المقترح: $categoryHint
      
      يجب أن يتضمن الوصف:
      - أهمية دبة التلوث أو الفلتر في إطفاء لمبة المكينة Check Engine (P0420 / P0430) واجتياز الفحص الدوري.
      - تنقية العوادم ومنع الدخان الأسود والرائحة الكريهة.
      - تعبئة الخيارات الأربعة المخصصة بحيث تناسب مقاسات المحرك وسنة الصنع ونوع الوقود.
      """.trimIndent()

      val response = withTimeout(60_000L) {
        geminiModel.generateContent(prompt)
      }

      val jsonString = response.text
      if (!jsonString.isNullOrBlank()) {
        val json = JSONObject(jsonString)
        val title = json.optString("title", inputPrompt)
        val description = json.optString("description", "")
        val opt1Label = json.optString("option1Label", "نوع الوقود والمحرك")
        val opt1Values = json.optJSONArray("option1Values")?.let { arr ->
          (0 until arr.length()).map { arr.getString(it) }
        } ?: listOf("بنزين Petrol", "ديزل Diesel")

        val opt2Label = json.optString("option2Label", "كثافة ونقاء الخلية")
        val opt2Values = json.optJSONArray("option2Values")?.let { arr ->
          (0 until arr.length()).map { arr.getString(it) }
        } ?: listOf("سيراميك وكالة فائق الفلترة", "معدن رياضي High-Flow")

        val opt3Label = json.optString("option3Label", "مقاس القطر ومدخل الماسورة")
        val opt3Values = json.optJSONArray("option3Values")?.let { arr ->
          (0 until arr.length()).map { arr.getString(it) }
        } ?: listOf("2.0 بوصة", "2.5 بوصة", "3.0 بوصة")

        val opt4Label = json.optString("option4Label", "توافق الموديل والفحص الدوري")
        val opt4Values = json.optJSONArray("option4Values")?.let { arr ->
          (0 until arr.length()).map { arr.getString(it) }
        } ?: listOf("تطفي لمبة المكينة وتجتاز الفحص 100%", "مع فتحة حساس أكسجين O2")

        val price = json.optDouble("suggestedPrice", 1450.0)

        return AiGeneratedContent(
          title = title,
          description = description,
          option1Label = opt1Label,
          option1Values = opt1Values,
          option2Label = opt2Label,
          option2Values = opt2Values,
          option3Label = opt3Label,
          option3Values = opt3Values,
          option4Label = opt4Label,
          option4Values = opt4Values,
          suggestedPrice = price,
          isPoweredByGemini = true
        )
      }
    } catch (e: TimeoutCancellationException) {
      Log.e(TAG, "Gemini generation timed out", e)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Gemini generation failed, falling back to offline engine", e)
    }

    // High quality deterministic fallback
    return fallbackGenerate(inputPrompt)
  }

  private fun fallbackGenerate(inputPrompt: String): AiGeneratedContent {
    val lower = inputPrompt.lowercase()

    return if (lower.contains("ديزل") || lower.contains("dpf") || lower.contains("شاحن") || lower.contains("تيربو")) {
      AiGeneratedContent(
        title = "فلتر ودبة تلوث ديزل DPF معالجة كاملة للشاحنات والجيوب",
        description = """
فلتر التقاط جزيئات الديزل (Diesel Particulate Filter DPF) شديد التحمل للمركبات العاملة بالديزل.
يمنع خروج الدخان الأسود ويحسن كفاءة حرق الوقود واستجابة التيربو ويحتوي على أنابيب حساسات فرق الضغط الأصلية لاجتياز الفحص الدوري بنجاح.
        """.trimIndent(),
        option1Label = "نوع المحرك والديزل",
        option1Values = listOf("تيربو ديزل Common Rail (حديث)", "ديزل تقليدي Heavy Duty"),
        option2Label = "سعة الفلتر ومعالجة الرماد",
        option2Values = listOf("فلتر DPF سيليكون كاربايد (SiC)", "فلتر معالج كوديريت Cordierite"),
        option3Label = "مقاس الشفاه ومسار العادم",
        option3Values = listOf("فلنجة وكالة جاهزة (Direct Fit)", "توصيل لحام يونيفرسال"),
        option4Label = "توافق الموديل",
        option4Values = listOf("موديلات 2018 - 2025", "موديلات 2012 - 2017"),
        suggestedPrice = 2200.0,
        isPoweredByGemini = false
      )
    } else {
      AiGeneratedContent(
        title = if (inputPrompt.isNotBlank()) inputPrompt else "دبة تلوث بيئية متطورة بمواصفات قياسية",
        description = """
دبة رصاص وبيئة مطابقة للمواصفات القياسية الأوروبية للسيارات. تقضي تماماً على رائحة الكربون الكريهة وتطفي لمبة المكينة (Check Engine P0420 / P0430).
مصنوعة من غلاف ستانلس ستيل معالج مع خلية سيراميك داخلية عالية الكثافة مغطاة بطبقات من البلاتينيوم والبلاديوم لتنقية الغازات واجتياز الفحص الدوري بنجاح 100%.
        """.trimIndent(),
        option1Label = "نوع الوقود والمحرك",
        option1Values = listOf("بنزين Petrol 4 سلندر", "بنزين Petrol 6 و 8 سلندر"),
        option2Label = "كثافة ونقاء الخلية الداخلية",
        option2Values = listOf("سيراميك أصلي وكالة (High Filtration)", "معدن رياضي 200 Cell"),
        option3Label = "مقاس القطر ومدخل الماسورة",
        option3Values = listOf("2.0 بوصة قياسي", "2.5 بوصة واسع", "3.0 بوصة رياضي"),
        option4Label = "سنة الصنع وتوافق الفحص الدوري",
        option4Values = listOf("تطفي لمبة المكينة وتجتاز الفحص 100%", "مع فتحة حساس أكسجين O2 جاهزة"),
        suggestedPrice = 1450.0,
        isPoweredByGemini = false
      )
    }
  }
}
