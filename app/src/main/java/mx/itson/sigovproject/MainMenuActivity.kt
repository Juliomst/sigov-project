package mx.itson.sigovproject

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject

class MainMenuActivity : AppCompatActivity() {
    private var txtNicknameMainMenu: TextView? = null
    private var userData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        txtNicknameMainMenu = findViewById(R.id.txtNicknameMainMenu)

        findViewById<ImageView>(R.id.imgUsuario).setOnClickListener {
            showLogoutDialog()
        }

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

        findViewById<MaterialButton>(R.id.btnRegistrarVenta).setOnClickListener{
            val intent = Intent(this, OrderListActivity::class.java)
            intent.putExtra("userData", userData)
            startActivity(intent)
        }
    }

    private fun showLogoutDialog(){
        MaterialAlertDialogBuilder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estas seguro que deseas cerrar sesión?")
            .setNegativeButton("Cancelar"){ dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Cerrar sesión"){ _, _ ->
                logout()
            }
            .show()
    }

    private fun logout(){
        userData = null

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}