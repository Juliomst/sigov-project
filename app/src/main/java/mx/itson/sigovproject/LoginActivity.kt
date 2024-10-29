package mx.itson.sigovproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private var txtUsuario: EditText? = null
    private var txtContrasenaLogin: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        txtUsuario = findViewById(R.id.txtUsuario)
        txtContrasenaLogin = findViewById(R.id.txtContrasenalogin)

    }

    fun clickLogin(view: View){
        val url = "http://192.168.56.1:8080/sigov/login.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try{
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        val intent = Intent(this, MainMenuActivity::class.java)
                        intent.putExtra("userData", response)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show()
                    }
                }catch(e: Exception){
                    Toast.makeText(this, "Error en la respuesta", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error de conexión ${error.message}", Toast.LENGTH_LONG).show()
            }){
            override fun getParams(): MutableMap<String, String>? {
                val params = HashMap<String, String>()
                params["nickname"] = txtUsuario?.text.toString()
                params["contrasena"] = txtContrasenaLogin?.text.toString()
                return params
            }
        }
        queue.add(request)
    }
    fun clickRegistro(view: View){
        startActivity(Intent(this, RegistroActivity::class.java))
    }
}