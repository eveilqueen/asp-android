package net.ghostpython.asp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Déjà connecté ? On saute direct au feed.
        if (ApiClient.getToken(this) != null) {
            startActivity(Intent(this, FeedActivity::class.java))
            finish()
            return
        }

        val inputIdent = findViewById<EditText>(R.id.inputIdent)
        val inputPassword = findViewById<EditText>(R.id.inputPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtError = findViewById<TextView>(R.id.txtError)

        btnLogin.setOnClickListener {
            val ident = inputIdent.text.toString().trim()
            val pass = inputPassword.text.toString()

            if (ident.isEmpty() || pass.isEmpty()) {
                txtError.text = "Remplis les deux champs."
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            txtError.text = ""

            ApiClient.post(
                this,
                mapOf("action" to "mobile_login", "ident" to ident, "password" to pass, "device" to "android")
            ) { json, error ->
                runOnUiThread {
                    btnLogin.isEnabled = true
                    if (error != null) {
                        txtError.text = error
                        return@runOnUiThread
                    }
                    if (json?.optBoolean("ok") == true) {
                        ApiClient.saveToken(this, json.getString("token"))
                        startActivity(Intent(this, FeedActivity::class.java))
                        finish()
                    } else {
                        txtError.text = json?.optString("error") ?: "Erreur inconnue."
                    }
                }
            }
        }
    }
}
