package net.ghostpython.asp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FeedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        val token = ApiClient.getToken(this)
        if (token == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val txtContent = findViewById<TextView>(R.id.txtContent)
        txtContent.text = "Connecté ✅\n\nProchaine étape : brancher un vrai endpoint JSON pour lister le feed (api.php n'en a pas encore, il génère du HTML pour l'instant)."
    }
}
