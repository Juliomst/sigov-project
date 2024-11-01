package mx.itson.sigovproject

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

class RegistroActivity : AppCompatActivity() {
    private var txtNombres: EditText? = null
    private var txtApellido: EditText? = null
    private var txtNickname: EditText? = null
    private var txtContrasena: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        txtNombres = findViewById(R.id.txtNombres)
        txtApellido = findViewById(R.id.txtApellido)
        txtNickname = findViewById(R.id.txtNickname)
        txtContrasena = findViewById(R.id.txtContrasena)

    }

    fun clickRegistrar(view: View){
        val campos = mapOf(
            "Nombres" to txtNombres?.text.toString().trim(),
            "Apellido" to txtApellido?.text.toString().trim(),
            "Nombre de usuario" to txtNickname?.text.toString().trim(),
            "Contraseña" to txtContrasena?.text.toString().trim()
        )

        for ((nombre, valor) in campos){
            if(valor.isEmpty()){
                Toast.makeText(this, "El campo $nombre es obligatorio", Toast.LENGTH_LONG).show()
                return
            }
            if(campos["Contraseña"]!!.length < 4){
                Toast.makeText(this, "La contraseña debe tener al menos 4 caracteres", Toast.LENGTH_LONG).show()
                return
            }
        }

        val url = BuildConfig.SERVER_IP+"registro.php"
        val queue = Volley.newRequestQueue(this)

        val request = object: StringRequest(
            Request.Method.POST, url,
            { response ->
                try{
                    val jsonResponse = JSONObject(response)
                    Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show()
                    if (jsonResponse.getString("status") == "success") {
                        finish()
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                    Toast.makeText(this, "Error en la respuesta: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["nombres"] = campos["Nombres"]!!
                params["apellido"] = campos["Apellido"]!!
                params["nickname"] = campos["Nombre de usuario"]!!
                params["contrasena"] = campos["Contraseña"]!!
                return params
            }
        }
        queue.add(request)
    }
    fun clickRegresar(view: View){
        finish()
    }
}