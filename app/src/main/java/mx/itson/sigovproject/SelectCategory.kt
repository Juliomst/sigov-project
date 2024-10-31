package mx.itson.sigovproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
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

class SelectCategory : AppCompatActivity() {
    private var btnComida: ImageButton? = null
    private var btnBebida: ImageButton? = null
    private var btnPostre: ImageButton? = null
    private var btnVolverCategoria: MaterialButton? = null
    private var btnFinalizarOrden: MaterialButton? = null
    private var idOrden: String? = null
    private var hasProducts: Boolean = false
    private var userData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_category)

        btnComida = findViewById(R.id.btnComida)
        btnBebida = findViewById(R.id.btnBebida)
        btnPostre = findViewById(R.id.btnPostre)
        btnVolverCategoria = findViewById(R.id.btnVolverCategoria)
        btnFinalizarOrden = findViewById(R.id.btnFinalizarOrden)

        idOrden = intent.getStringExtra("idOrden")
        userData = intent.getStringExtra("userData")
        hasProducts = intent.getBooleanExtra("hasProducts", false)

        if(userData == null){
            Toast.makeText(this, "Error: Datos de usuario no disponibles", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        updateFinalizarOrdenVisibility()

        setupButtons()
    }

    private fun updateFinalizarOrdenVisibility(){
        btnFinalizarOrden?.visibility = if(hasProducts) View.VISIBLE else View.GONE
    }

    private fun setupButtons(){
        btnComida?.setOnClickListener{
            navigateToProducts("Comida")
        }
        btnBebida?.setOnClickListener{
            navigateToProducts("Bebida")
        }
        btnPostre?.setOnClickListener{
            navigateToProducts("Postre")
        }
        btnVolverCategoria?.setOnClickListener{
            eliminarOrden()
        }
        btnFinalizarOrden?.setOnClickListener{
            val intent = Intent(this, OrderSummary::class.java)
            intent.putExtra("idOrden", idOrden)
            intent.putExtra("userData", userData)
            startActivity(intent)
            finish()
        }
    }


    private fun eliminarOrden(){
        val url = "http://192.168.56.1:8080/sigov/eliminarOrden.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try{
                    val jsonResponse = JSONObject(response)
                    if(jsonResponse.getString("status") == "success"){
                        finish()
                    }else{
                        Toast.makeText(this, "Error ${jsonResponse.getString("message")}", Toast.LENGTH_LONG).show()
                    }
                }catch(e: Exception){
                    Toast.makeText(this, "Error en la respuesta: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(this, "Error de conexión: ${error.toString()}", Toast.LENGTH_LONG).show()
                error.printStackTrace()
            }) {
            override fun getParams(): MutableMap<String, String>? {
                val params = HashMap<String, String>()
                params["idOrden"] = idOrden ?: ""
                return params
            }
        }
        queue.add(request)
    }

    private fun navigateToProducts(categoria: String){
        val intent = Intent(this, SelectProductActivity::class.java)
        intent.putExtra("categoria", categoria)
        intent.putExtra("idOrden", idOrden)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        checkForProducts()
    }

    private fun checkForProducts(){
        val url = "http://192.168.56.1:8080/sigov/verificarProductos.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try{
                    val jsonResponse = JSONObject(response)
                    hasProducts = jsonResponse.getBoolean("hasProducts")
                    updateFinalizarOrdenVisibility()
                }catch(e: Exception){
                    e.printStackTrace()
                }
            },
            { error -> error.printStackTrace() }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["idOrden"] = idOrden ?: ""
                return params
            }
        }
        queue.add(request)
    }
}