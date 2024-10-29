package mx.itson.sigovproject

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import org.json.JSONObject

class MainMenuActivity : AppCompatActivity() {
    private var txtNicknameMainMenu: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        txtNicknameMainMenu = findViewById(R.id.txtNicknameMainMenu)

        val userData = intent.getStringExtra("userData")
        if (userData != null){
            try{
                val jsonUser = JSONObject(userData)
                val userNickname = jsonUser.getString("nickname")
                txtNicknameMainMenu?.text = userNickname
            }catch(e: Exception){
                txtNicknameMainMenu?.text = "Usuario"
            }
        }

        findViewById<MaterialButton>(R.id.btnNuevaOrden).setOnClickListener{
            val intent = Intent(this, SelectTableActivity::class.java)
            intent.putExtra("userData", intent.getStringExtra("userData"))
            startActivity(intent)
        }

    }
}