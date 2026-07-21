package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return try {
            // BuildConfig.GEMINI_API_KEY is injected by Secrets Gradle Plugin
            val key = BuildConfig.GEMINI_API_KEY
            if (key == "MY_GEMINI_API_KEY" || key.isBlank()) "" else key
        } catch (e: Exception) {
            Log.e(TAG, "Error reading GEMINI_API_KEY", e)
            ""
        }
    }

    suspend fun generateResponse(prompt: String, systemInstruction: String = ""): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            Log.w(TAG, "GEMINI_API_KEY is empty. Falling back to offline AI simulation.")
            return@withContext getLocalFallbackResponse(prompt)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            
            val requestJson = JSONObject()
            
            // Add system instruction if present
            if (systemInstruction.isNotEmpty()) {
                requestJson.put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                })
            }

            // Add prompt in contents
            requestJson.put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "API error: ${response.code} ${response.message}")
                    return@withContext getLocalFallbackResponse(prompt)
                }

                val responseBody = response.body?.string() ?: return@withContext "Không nhận được phản hồi từ máy chủ."
                val responseJson = JSONObject(responseBody)
                
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "Không tìm thấy nội dung phản hồi.")
                    }
                }
                "Không giải mã được phản hồi từ AI."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception in Gemini API call", e)
            getLocalFallbackResponse(prompt)
        }
    }

    private fun getLocalFallbackResponse(prompt: String): String {
        val lowerPrompt = prompt.lowercase()
        return when {
            lowerPrompt.contains("áo") || lowerPrompt.contains("hoodie") || lowerPrompt.contains("thời trang") -> {
                "Dựa trên các sản phẩm hot của VibeCart, tôi gợi ý cho bạn chiếc **Aura Premium Oversized Hoodie** (490k). Phom áo đứng Hàn Quốc rất phù hợp để đi dạo phố hay hẹn hò. Bạn có muốn thêm vào giỏ hàng ngay không? 🧥✨"
            }
            lowerPrompt.contains("tai nghe") || lowerPrompt.contains("earbuds") || lowerPrompt.contains("âm thanh") || lowerPrompt.contains("tech") -> {
                "Tôi khuyên bạn nên thử **LunarTech Wireless ANC Earbuds** (890k). Mẫu tai nghe chống ồn chủ động này có dải âm bass sâu tuyệt vời, đang có chương trình flash sale giảm 40% đó ạ! 🎧⚡"
            }
            lowerPrompt.contains("son") || lowerPrompt.contains("son tint") || lowerPrompt.contains("beauty") -> {
                "Dòng son **GlowGlow Velvet Lip Tint** (150k) đang rất hot với hơn 650+ đánh giá tích cực. Chất son nhung mịn lì, không hề khô môi, đặc biệt bền màu cả ngày dài! 💄💖"
            }
            lowerPrompt.contains("bình hoa") || lowerPrompt.contains("gốm") || lowerPrompt.contains("zenstone") -> {
                "Chiếc bình hoa gốm nhám **ZenStone Minimalist Ceramic Vase** (340k) cực kỳ thích hợp để trang trí góc làm việc tối giản phong cách Bắc Âu. Bạn có muốn tậu ngay một chiếc làm mới không gian không? 🌿🏺"
            }
            lowerPrompt.contains("vòng tay") || lowerPrompt.contains("thông minh") || lowerPrompt.contains("fittrack") -> {
                "Sản phẩm **FitTrack Pro Smart Band** (590k) sở hữu màn hình AMOLED rực rỡ và thời lượng pin khủng 14 ngày, đo SpO2 rất chuẩn xác. Thích hợp cho người yêu thể thao! 🏃‍♂️🔋"
            }
            lowerPrompt.contains("mô tả") || lowerPrompt.contains("description") || lowerPrompt.contains("viết bài") -> {
                "✨ **[AI Product Description]** ✨\n\nSản phẩm đột phá mới mang phong cách trẻ trung, hiện đại và tiện dụng. Được sản xuất từ nguồn nguyên liệu chọn lọc cao cấp, bền bỉ và vô cùng thân thiện với người dùng. Thiết kế tỉ mỉ, tinh tế mang đậm hơi thở thời trang hiện đại sẽ giúp nâng tầm cuộc sống của bạn hằng ngày. Đặt mua ngay hôm nay để nhận ưu đãi lên tới 35% từ VibeCart!"
            }
            lowerPrompt.contains("dịch") || lowerPrompt.contains("translate") -> {
                "🤖 [AI Translation]: \"This is a premium quality product, highly recommended by global content creators! Let's purchase now!\""
            }
            else -> {
                "Cảm ơn bạn đã hỏi! Là Trợ lý AI VibeCart, tôi đề xuất bạn ghé qua mục **Shop** để săn các sản phẩm Flash Sale cực sốc từ các KOL uy tín như Chanh Beauty 🌸 hoặc Minh Techie 💻 nhé! Bạn cần tư vấn sâu hơn về sản phẩm nào không ạ? 🤖🛍️"
            }
        }
    }
}
