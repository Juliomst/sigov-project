package mx.itson.sigovproject

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import org.json.JSONObject

class MenuControlActivity : AppCompatActivity() {
    private var txtNicknameMenuControl: TextView? = null
    private var userData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_control)

        userData = intent.getStringExtra("userData")
        if (userData != null){
            try {
                val jsonUser = JSONObject(userData!!)
                if (jsonUser.has("nombres") && jsonUser.has("apellido")){
                    val userName = "${jsonUser.getString("nombres")} ${jsonUser.getString("apellido")}"
                    txtNicknameMenuControl?.text = userName
                }
            }catch (e: Exception){
                e.printStackTrace()
                txtNicknameMenuControl?.text = "Usuario"
            }
        }

        findViewById<MaterialButton>(R.id.btnAgregarProducto).setOnClickListener{
            val intent = Intent(this, ManageProductFormActivity::class.java)
            startActivity(intent)
        }
        findViewById<MaterialButton>(R.id.btnActualizarProducto).setOnClickListener{
            val intent = Intent(this, ManageProductSearchActivity::class.java)
            intent.putExtra("mode", "ACTUALIZAR")
            startActivity(intent)
        }
        findViewById<MaterialButton>(R.id.btnEliminarProducto).setOnClickListener{
            val intent = Intent(this, ManageProductSearchActivity::class.java)
            intent.putExtra("mode", "ELIMINAR")
            startActivity(intent)
        }
        findViewById<MaterialButton>(R.id.btnRegresarMainMenu).setOnClickListener{
            finish()
        }
    }
}