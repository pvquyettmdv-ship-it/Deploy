package com.example.data.api

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

sealed class GeminiResult {
    data class Success(val response: String, val thinking: String?) : GeminiResult()
    data class Error(val message: String) : GeminiResult()
}

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro-preview:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateWithThinking(prompt: String, systemInstruction: String? = null): GeminiResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API key is empty or placeholder!")
            return@withContext GeminiResult.Error("Chưa cấu hình API Key trong mục Secrets của AI Studio.")
        }

        try {
            val requestBodyJson = JSONObject().apply {
                // Contents
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })

                // Generation Config with Thinking config HIGH
                put("generationConfig", JSONObject().apply {
                    put("thinkingConfig", JSONObject().apply {
                        put("thinkingLevel", "HIGH")
                    })
                })

                // Optional system instruction
                if (systemInstruction != null) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", systemInstruction)
                            })
                        })
                    })
                }
            }

            val requestBodyStr = requestBodyJson.toString()
            Log.d(TAG, "Request payload: $requestBodyStr")

            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBodyStr.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string()
                Log.d(TAG, "Response Code: ${response.code}")
                Log.d(TAG, "Response Body: $bodyStr")

                if (!response.isSuccessful || bodyStr == null) {
                    return@withContext GeminiResult.Error("API call failed with code ${response.code}: ${response.message}")
                }

                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return@withContext GeminiResult.Error("Không nhận được câu trả lời từ Gemini API.")
                }

                val firstCandidate = candidates.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")

                if (parts == null || parts.length() == 0) {
                    return@withContext GeminiResult.Error("Không có nội dung trả về từ Gemini API.")
                }

                var thinkingText = ""
                var responseText = ""

                for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i) ?: continue
                    val text = part.optString("text", "")
                    val thought = part.optBoolean("thought", false)

                    if (thought) {
                        thinkingText += text
                    } else {
                        // Some versions return thought without explicit boolean flag, 
                        // e.g. having mimeType="text/x-google-thinking" or just split parts.
                        // Let's check for standard "thought" fields.
                        responseText += text
                    }
                }

                // If thinkingText is empty but there are multiple parts and the first one feels like thinking,
                // or if we have only responseText, let's treat it safely.
                if (thinkingText.isEmpty() && parts.length() > 1) {
                    // Try to see if the first part is actually a thought
                    val firstPart = parts.optJSONObject(0)
                    val secondPart = parts.optJSONObject(1)
                    if (firstPart != null && secondPart != null) {
                        val firstText = firstPart.optString("text", "")
                        val secondText = secondPart.optString("text", "")
                        thinkingText = firstText
                        responseText = secondText
                    }
                }

                if (responseText.isEmpty() && thinkingText.isNotEmpty()) {
                    // Fallback if all text was put in thinking
                    responseText = thinkingText
                    thinkingText = ""
                }

                if (responseText.isEmpty()) {
                    responseText = parts.optJSONObject(0)?.optString("text", "") ?: ""
                }

                GeminiResult.Success(responseText, thinkingText.ifEmpty { null })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateWithThinking", e)
            GeminiResult.Error("Lỗi kết nối: ${e.localizedMessage ?: e.message}")
        }
    }
}
