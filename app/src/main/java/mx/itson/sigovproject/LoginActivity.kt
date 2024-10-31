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
        if(txtUsuario?.text.toString().trim().isEmpty() || txtContrasenaLogin?.text.toString().trim().isEmpty()){
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_LONG).show()
            return
        }
        val url = "http://192.168.56.1:8080/sigov/login.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try{
                    val jsonResponse = JSONObject(response)
                    when(jsonResponse.getString("status")) {
                        "success" -> {
                            Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainMenuActivity::class.java)
                            intent.putExtra("userData", jsonResponse.getJSONObject("data").toString())
                            startActivity(intent)
                            finish()
                        }
                        "error" -> {
                            Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show()
                        }
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                    Toast.makeText(this, "Error en la respuesta: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_LONG).show()
            }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["nickname"] = txtUsuario?.text.toString().trim()
                params["contrasena"] = txtContrasenaLogin?.text.toString().trim()
                return params
            }
        }
        queue.add(request)
    }
    fun clickRegistro(view: View){
        startActivity(Intent(this, RegistroActivity::class.java))
    }
}