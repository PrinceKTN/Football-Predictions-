package com.example.data.remote

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

data class ChatMessage(
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val searchSources: List<GroundingSource> = emptyList(),
    val modelUsed: String? = null,
    val isThinking: Boolean = false
)

data class GroundingSource(
    val title: String,
    val url: String
)

data class GeminiResult(
    val text: String,
    val searchSources: List<GroundingSource> = emptyList(),
    val searchQueries: List<String> = emptyList(),
    val isError: Boolean = false,
    val errorMessage: String? = null
)

class GeminiService {

    companion object {
        private const val TAG = "GeminiService"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

        const val MODEL_PRO = "gemini-3.1-pro-preview"
        const val MODEL_FLASH = "gemini-3.5-flash"
        const val MODEL_FLASH_LITE = "gemini-3.1-flash-lite-preview"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiKey: String
        get() = BuildConfig.GEMINI_API_KEY.ifBlank { "" }

    /**
     * High Thinking Mode using gemini-3.1-pro-preview with thinkingLevel = HIGH.
     * Do NOT set maxOutputTokens.
     */
    suspend fun analyzeWithHighThinking(
        prompt: String,
        systemInstruction: String = "You are a world-class betting prediction auditor and sports betting mathematician. Provide deep, rigorous statistical evaluation, mathematical expectation, and scam detection."
    ): GeminiResult = withContext(Dispatchers.IO) {
        val jsonBody = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            }
            put("contents", contentsArray)

            // High thinking configuration
            put("generationConfig", JSONObject().apply {
                put("thinkingConfig", JSONObject().apply {
                    put("thinkingLevel", "HIGH")
                })
            })

            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemInstruction))
                })
            })
        }

        executeGeminiRequest(MODEL_PRO, jsonBody)
    }

    /**
     * Live Google Search Grounding using gemini-3.5-flash with googleSearch tool.
     */
    suspend fun searchGroundedQuery(
        prompt: String,
        systemInstruction: String = "You are a real-time sports prediction investigator. Use Google Search to find current, fresh daily predictions, recent site records, and current match intelligence."
    ): GeminiResult = withContext(Dispatchers.IO) {
        val jsonBody = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            }
            put("contents", contentsArray)

            // Google Search tool
            put("tools", JSONArray().apply {
                put(JSONObject().apply {
                    put("googleSearch", JSONObject())
                })
            })

            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemInstruction))
                })
            })
        }

        executeGeminiRequest(MODEL_FLASH, jsonBody)
    }

    /**
     * Fast analysis using gemini-3.1-flash-lite-preview for rapid tip evaluation.
     */
    suspend fun quickEvaluateTip(
        tipDetails: String
    ): GeminiResult = withContext(Dispatchers.IO) {
        val systemPrompt = "You are an instant betting odds and value assessor. Quickly estimate implied probability, identify if odds offer value, and state 2 key risk factors in concise bullet points."
        val jsonBody = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "Quickly evaluate this tip: $tipDetails"))
                    })
                })
            }
            put("contents", contentsArray)

            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemPrompt))
                })
            })
        }

        executeGeminiRequest(MODEL_FLASH_LITE, jsonBody)
    }

    /**
     * Analyze a user-provided text snippet from a prediction site and summarize its credibility.
     */
    suspend fun analyzeSnippetCredibility(
        snippet: String
    ): GeminiResult = withContext(Dispatchers.IO) {
        val systemPrompt = """
            You are an expert sports betting auditor, mathematician, and consumer protection specialist.
            Analyze the user-provided text snippet from a prediction site/tipster service and summarize its credibility.
            
            Structure your summary with clear sections:
            1. 🛡️ Credibility Verdict: (e.g. HIGHLY SUSPECT / DECEPTIVE / LOW CREDIBILITY / MODERATE / CREDIBLE STATISTICAL SERVICE) and a 1-10 Credibility Score.
            2. 🚩 Red Flags & Marketing Tactics: Identify any misleading claims (e.g., '100% fixed games', 'guaranteed profit', '90%+ win rate', fabricated slips, absence of third-party audit).
            3. 📊 Mathematical Reality Check: Contrast their claims against statistical variance, bookmaker vig, and long-term break-even math.
            4. 💡 Safe Testing Advice: Provide practical steps for the user (e.g., how to paper-test free games for 30 days, demand third-party proofing on Blogabet/Tipstrr, or avoid paying subscription fees).
            
            Keep the tone objective, protective, and analytically sharp.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "Please analyze this text snippet from a prediction site and summarize its credibility:\n\n\"\"\"\n$snippet\n\"\"\""))
                    })
                })
            }
            put("contents", contentsArray)

            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemPrompt))
                })
            })
        }

        executeGeminiRequest(MODEL_FLASH, jsonBody)
    }

    /**
     * Multi-turn Chat using conversation history.
     */
    suspend fun chatTurn(
        history: List<ChatMessage>,
        userMessage: String,
        model: String = MODEL_FLASH,
        systemInstruction: String = "You are the Betting Site Auditor & Prediction Analyst AI. You provide objective advice on prediction sites, audit subscription fees, explain variance, and guide users on testing daily free tips without losing money."
    ): GeminiResult = withContext(Dispatchers.IO) {
        val jsonBody = JSONObject().apply {
            val contentsArray = JSONArray()

            // Append historical messages
            history.takeLast(10).forEach { msg ->
                contentsArray.put(JSONObject().apply {
                    put("role", if (msg.role == "user") "user" else "model")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", msg.text))
                    })
                })
            }

            // Append new user message
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", userMessage))
                })
            })

            put("contents", contentsArray)

            // If using Pro model for complex thinking, enable HIGH thinking
            if (model == MODEL_PRO) {
                put("generationConfig", JSONObject().apply {
                    put("thinkingConfig", JSONObject().apply {
                        put("thinkingLevel", "HIGH")
                    })
                })
            } else if (model == MODEL_FLASH) {
                // Enable search grounding for fresh context
                put("tools", JSONArray().apply {
                    put(JSONObject().apply {
                        put("googleSearch", JSONObject())
                    })
                })
            }

            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemInstruction))
                })
            })
        }

        executeGeminiRequest(model, jsonBody)
    }

    private fun executeGeminiRequest(model: String, jsonPayload: JSONObject): GeminiResult {
        val key = apiKey
        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            return GeminiResult(
                text = "API Key not configured. Please add your Gemini API Key in the AI Studio Secrets panel.",
                isError = true,
                errorMessage = "API key missing or placeholder"
            )
        }

        val url = "$BASE_URL/$model:generateContent?key=$key"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonPayload.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e(TAG, "Gemini API error code: ${response.code}, body: $bodyString")
                    return GeminiResult(
                        text = "Unable to complete AI analysis. (${response.code})",
                        isError = true,
                        errorMessage = "HTTP ${response.code}: $bodyString"
                    )
                }

                parseGeminiResponse(bodyString)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini network exception", e)
            GeminiResult(
                text = "Connection error while reaching Gemini: ${e.localizedMessage ?: "Unknown error"}",
                isError = true,
                errorMessage = e.message
            )
        }
    }

    private fun parseGeminiResponse(jsonString: String): GeminiResult {
        return try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates") ?: return GeminiResult(
                text = "No response candidates returned.",
                isError = true
            )

            if (candidates.length() == 0) {
                return GeminiResult(text = "Empty response returned.", isError = true)
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            val textBuilder = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    val partText = part.optString("text")
                    if (partText.isNotBlank()) {
                        textBuilder.append(partText)
                    }
                }
            }

            // Extract Google Search Grounding metadata if present
            val sources = mutableListOf<GroundingSource>()
            val queries = mutableListOf<String>()

            val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
            if (groundingMetadata != null) {
                val webSearchQueries = groundingMetadata.optJSONArray("webSearchQueries")
                if (webSearchQueries != null) {
                    for (i in 0 until webSearchQueries.length()) {
                        queries.add(webSearchQueries.optString(i))
                    }
                }

                val groundingChunks = groundingMetadata.optJSONArray("groundingChunks")
                if (groundingChunks != null) {
                    for (i in 0 until groundingChunks.length()) {
                        val chunk = groundingChunks.getJSONObject(i)
                        val web = chunk.optJSONObject("web")
                        if (web != null) {
                            val uri = web.optString("uri")
                            val title = web.optString("title")
                            if (uri.isNotBlank()) {
                                sources.add(GroundingSource(title = if (title.isBlank()) uri else title, url = uri))
                            }
                        }
                    }
                }
            }

            val fullText = textBuilder.toString().ifBlank { "No text produced." }
            GeminiResult(
                text = fullText,
                searchSources = sources.distinctBy { it.url },
                searchQueries = queries,
                isError = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini response", e)
            GeminiResult(
                text = "Error parsing AI response: ${e.message}",
                isError = true,
                errorMessage = e.message
            )
        }
    }
}
