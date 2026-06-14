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

data class GeminiQuiz(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateTriviaChallenge(): GeminiQuiz? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured. Falling back to default quiz.")
            return@withContext getFallbackQuiz()
        }

        val prompt = """
            Generate an engaging, educational and fun multiple-choice trivia challenge for a reward points application. 
            The trivia question can cover technology, science, history, or pop culture.
            You MUST respond with EXACTLY a JSON object with this structure:
            {
               "question": "A clear trivia question statement...",
               "options": ["Option A", "Option B", "Option C", "Option D"],
               "correctIndex": 0, 
               "explanation": "Brief context explanation why this is correct..."
            }
            Make sure 'correctIndex' is the 0-based index targeting the correct option in your options list (0 to 3).
            Return ONLY raw JSON, do not wrap in markdown ```json or block fences.
        """.trimIndent()

        // Construct JSON body
        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            // Force JSON output
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)

        val urlWithKey = "$API_URL?key=$apiKey"

        val request = Request.Builder()
            .url(urlWithKey)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "API error response: code=${response.code} body=$errBody")
                    return@withContext getFallbackQuiz()
                }

                val responseBody = response.body?.string() ?: return@withContext getFallbackQuiz()
                Log.d(TAG, "Response obtained successfully: $responseBody")

                val root = JSONObject(responseBody)
                val candidates = root.optJSONArray("candidates") ?: return@withContext getFallbackQuiz()
                val candidate = candidates.optJSONObject(0) ?: return@withContext getFallbackQuiz()
                val content = candidate.optJSONObject("content") ?: return@withContext getFallbackQuiz()
                val parts = content.optJSONArray("parts") ?: return@withContext getFallbackQuiz()
                val part = parts.optJSONObject(0) ?: return@withContext getFallbackQuiz()
                val text = part.optString("text") ?: return@withContext getFallbackQuiz()

                // Parse generated quiz JSON
                try {
                    val cleanText = text.trim()
                    val quizJson = JSONObject(cleanText)
                    val question = quizJson.getString("question")
                    val optsArray = quizJson.getJSONArray("options")
                    val options = mutableListOf<String>()
                    for (i in 0 until optsArray.length()) {
                        options.add(optsArray.getString(i))
                    }
                    val correctIndex = quizJson.optInt("correctIndex", 0).coerceIn(0, 3)
                    val explanation = quizJson.optString("explanation", "Excellent brain challenge!")

                    return@withContext GeminiQuiz(question, options, correctIndex, explanation)
                } catch (e: Exception) {
                    Log.e(TAG, "Parsing text as Quiz JSON failed: text=$text", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception invoking Gemini API", e)
        }

        return@withContext getFallbackQuiz()
    }

    private fun getFallbackQuiz(): GeminiQuiz {
        val options = listOf(
            GeminiQuiz("Which computing pioneer is credited with writing the first computer algorithm in the 1840s?", listOf("Grace Hopper", "Ada Lovelace", "Alan Turing", "Charles Babbage"), 1, "Ada Lovelace wrote the algorithm for Babbage's mechanical Analytical Engine, making her the world's first programmer."),
            GeminiQuiz("In modern Android development, what is Jetpack Compose?", listOf("A background thread database scheduler", "An XML layout compiler", "A modern declarative UI toolkit", "A Kotlin dependency injection system"), 2, "Jetpack Compose is Android's modern native toolkit for building declarative user interfaces directly in Kotlin."),
            GeminiQuiz("Which design system is standard for styling modern Android apps?", listOf("Metro Design", "Material Design 3 (M3)", "Human Interface Guidelines", "Tailwind UI Layout"), 1, "Material Design 3 (M3) is Google's flagship open-source design system used for modern Android experiences."),
            GeminiQuiz("What mechanism does Room Database use under the hood on Android?", listOf("MongoDB Server Connection", "Local SQLite Engine", "Redis In-Memory State", "JSON File Stream"), 1, "Room is a modern architectural wrapper that manages a local SQLite file engine reactive to Kotlin Coroutines.")
        )
        return options.random()
    }
}
