package net.ghostpython.asp

import android.content.Context
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiClient {

    const val BASE_URL = "https://vint.42web.io/api.php"
    const val ROOT_URL = "https://vint.42web.io/"

    @Volatile
    var siteCookie: String? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val builder = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .header("Accept", "application/json, text/plain, */*")
            siteCookie?.let { builder.header("Cookie", it) }
            chain.proceed(builder.build())
        }
        .build()

    private fun prefs(ctx: Context) = ctx.getSharedPreferences("asp", Context.MODE_PRIVATE)

    fun saveToken(ctx: Context, token: String) {
        prefs(ctx).edit().putString("token", token).apply()
    }

    fun getToken(ctx: Context): String? = prefs(ctx).getString("token", null)

    fun clearToken(ctx: Context) {
        prefs(ctx).edit().remove("token").apply()
    }

    fun post(ctx: Context, params: Map<String, String>, callback: (JSONObject?, String?) -> Unit) {
        val formBuilder = FormBody.Builder()
        params.forEach { (k, v) -> formBuilder.add(k, v) }

        val requestBuilder = Request.Builder()
            .url(BASE_URL)
            .post(formBuilder.build())

        getToken(ctx)?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        client.newCall(requestBuilder.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, "Connexion impossible : ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body == null) {
                    callback(null, "Réponse vide du serveur")
                    return
                }
                try {
                    callback(JSONObject(body), null)
                } catch (e: Exception) {
                    val snippet = body.take(80).replace("\n", " ")
                    callback(null, "Réponse invalide (code ${response.code}): $snippet")
                }
            }
        })
    }
}
