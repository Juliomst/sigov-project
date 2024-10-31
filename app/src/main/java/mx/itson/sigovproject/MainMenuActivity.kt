package mx.itson.sigovproject

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import org.json.JSONObject

class MainMenuActivity : AppCompatActivity() {
    private var txtNicknameMainMenu: TextView? = null
    private var userData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        txtNicknameMainMenu = findViewById(R.id.txtNicknameMainMenu)

        userData = intent.getStringExtra("userData")
        if (userData != null){
            try{
                val jsonUser = JSONObject(userData!!)
                if (jsonUser.has("nombres") && jsonUser.has("apellido")) {
                    val userName = "${jsonUser.getString("nombres")} ${jsonUser.getString("apellido")}"
                    txtNicknameMainMenu?.text = userName
                }else{
                    txtNicknameMainMenu?.text = "Usuario"
                    Toast.makeText(this, "Error: Datos de usuario incompletos", Toast.LENGTH_SHORT).show()
                }
            }catch(e: Exception){
                e.printStackTrace()
                txtNicknameMainMenu?.text = "Usuario"
                Toast.makeText(this, "Error al procesar datos de usuario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }else{
            txtNicknameMainMenu?.text = "Usuario"
            Toast.makeText(this, "No se recibieron datos del usuario", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.btnNuevaOrden).setOnClickListener{
            val intent = Intent(this, SelectTableActivity::class.java)
            intent.putExtra("userData", userData)
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.btnAdministrarMenu).setOnClickListener {
            val intent = Intent(this, MenuControlActivity::class.java)
            intent.putExtra("userData", userData)
            startActivity(intent)
        }
    }
}