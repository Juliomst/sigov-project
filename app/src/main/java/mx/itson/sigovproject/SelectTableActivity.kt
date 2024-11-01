package mx.itson.sigovproject

import android.content.Intent
import android.os.Bundle
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SelectTableActivity : AppCompatActivity() {
    private var gridMesas: GridLayout? = null
    private var btnVolver: MaterialButton? = null
    private var idMesero: String = ""
    private var userData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_table)

        gridMesas = findViewById(R.id.gridMesas)
        btnVolver = findViewById(R.id.btnVolver)

        userData = intent.getStringExtra("userData")
        if (userData != null){
            try{
                val jsonUser = JSONObject(userData!!)
                idMesero = jsonUser.getString("id_mesero")

                println("Datos del usuario: $userData")
                println("ID Mesero: $idMesero")
            }catch(e: Exception){
                e.printStackTrace()
                Toast.makeText(this, "Error al obtener datos: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        }else{
            Toast.makeText(this, "No se recibieron datos del usuario", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setupTableGrid()
        setupButtons()
    }

    private fun setupTableGrid(){
        for (i in 1..10){
            val button = MaterialButton(this).apply {
                text = i.toString()
                textSize = 18f
                setBackgroundColor(getColor(R.color.buttonPallette))
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }
                setOnClickListener{
                    crearNuevaOrden(i)
                }
            }
            gridMesas?.addView(button)
        }
    }
    private fun setupButtons(){
        btnVolver?.setOnClickListener{
            finish()
        }
    }

    private fun crearNuevaOrden(numeroMesa: Int){
        val url = BuildConfig.SERVER_IP+"crearOrden.php"
        val queue = Volley.newRequestQueue(this)

        val request = object: StringRequest(
            Request.Method.POST, url,
            { response ->
                try{
                    println("Respuesta del servidor: $response")
                    val jsonResponse = JSONObject(response)
                    if(jsonResponse.getString("status") == "success"){
                        val idOrden = jsonResponse.getString("idOrden")
                        val intent = Intent(this, SelectCategory::class.java)
                        intent.putExtra("idOrden", idOrden)
                        intent.putExtra("userData", userData)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(this, "Error: ${jsonResponse.getString("message")}", Toast.LENGTH_LONG).show()
                    }
                }catch(e: Exception){
                    Toast.makeText(this, "Error en la respuesta: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(this,"Error de conexión: ${error.toString()}", Toast.LENGTH_LONG).show()
                error.printStackTrace()
            }) {
            override fun getParams(): MutableMap<String, String>? {
                val params = HashMap<String, String>()
                params["id_mesero"] = idMesero
                params["noMesa"] = numeroMesa.toString()
                params["fechaRegistro"]  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                println("Parametros eviados: $params")
                return params
            }
        }
        queue.add(request)
    }
}