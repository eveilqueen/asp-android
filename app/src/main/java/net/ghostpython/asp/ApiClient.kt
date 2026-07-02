package net.ghostpython.asp

import android.content.Context
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Parle à ton api.php sur InfinityFree.
 * Change BASE_URL par ton vrai domaine.
 */
object ApiClient {

    const val BASE_URL = "https://vint.42web.io/api.php"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun prefs(ctx: Context) = ctx.getSharedPreferences("asp", Context.MODE_PRIVATE)

    fun saveToken(ctx: Context, token: String) {
        prefs(ctx).edit().putString("token", token).apply()
    }

    fun getToken(ctx: Context): String? = prefs(ctx).getString("token", null)

    fun clearToken(ctx: Context) {
        prefs(ctx).edit().remove("token").apply()
    }

    /** Appel POST générique vers api.php avec les paramètres donnés. */
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
                    callback(null, "Réponse invalide (code ${response.code})")
                }
            }
        })
    }
}
