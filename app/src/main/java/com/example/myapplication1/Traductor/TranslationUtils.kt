package com.example.myapplication1.Traductor



import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

object TranslationUtils {
    private val client = OkHttpClient()

    suspend fun translate(text: String, targetLanguage: String): String {
        val url = "https://translation.googleapis.com/language/translate/v2"
        val json = JSONObject()
        json.put("q", text)
        json.put("source", "es")
        json.put("target", targetLanguage)
        json.put("key", "AIzaSyBn68MRMw8_przJNNOOPgy_An46EUm-6Lc") // Reemplaza "your-google-translate-api-key" con tu clave API de Google Translate
        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json.toString())

        val request = Request.Builder().url(url).post(body).build()

        client.newCall(request).execute().use { response ->
            val jsonResponse = JSONObject(response.body!!.string())
            if (jsonResponse.has("data")) {
                val dataObject = jsonResponse.getJSONObject("data")
                val translationsArray = dataObject.getJSONArray("translations")
                val firstTranslationObject = translationsArray.getJSONObject(0)
                val translatedText = firstTranslationObject.getString("translatedText")
                return translatedText
            } else {
                throw Exception("Translation failed: response does not contain 'data'")
            }
        }
    }
}