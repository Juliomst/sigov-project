package mx.itson.sigovproject

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
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
import java.util.Locale

class OrderSummary : AppCompatActivity() {
    private var txtMesaInfo: TextView? = null
    private var txtFechaInfo: TextView? = null
    private var txtProductos: TextView? = null
    private var txtTotal: TextView? = null
    private var btnConfirmar: MaterialButton? = null
    private var idOrden: String = ""
    private var userData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_order_summary)

        initializeView()
        idOrden = intent.getStringExtra("idOrden") ?: ""
        userData = intent.getStringExtra("userData")

        if (userData == null){
            Toast.makeText(this, "Error: Datos de usuario no disponibles", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (idOrden.isEmpty()){
            Toast.makeText(this, "Error: Orden no valida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        cargarDetallesOrden()
        setupButtons()
    }

    private fun initializeView(){
        txtMesaInfo = findViewById(R.id.txtMesaInfo)
        txtFechaInfo = findViewById(R.id.txtFechaInfo)
        txtProductos = findViewById(R.id.txtProductos)
        txtTotal = findViewById(R.id.txtTotal)
        btnConfirmar = findViewById(R.id.btnConfirmar)
    }

    private fun setupButtons(){
        btnConfirmar?.setOnClickListener{
            val intent = Intent(this, MainMenuActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("userData", userData)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun cargarDetallesOrden(){
        val url = "http://192.168.56.1:8080/sigov/obtenerDetallesOrden.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try{
                    val jsonResponse = JSONObject(response)
                    if(jsonResponse.getString("status") == "success"){
                        mostrarDetallesOrden(jsonResponse)
                    }else{
                        Toast.makeText(this, "Error: ${jsonResponse.getString("message")}", Toast.LENGTH_LONG).show()
                    }
                }catch(e: Exception){
                    Toast.makeText(this, "Error al procesar la respuesta: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error de conexion: ${error.message}", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["idOrden"] = idOrden
                return params
            }
        }
        queue.add(request)
    }

    private fun mostrarDetallesOrden(jsonResponse: JSONObject){
        val orden = jsonResponse.getJSONObject("orden")
        val productos = jsonResponse.getJSONArray("productos")

        txtMesaInfo?.text = "Mesa: ${orden.getInt("noMesa")}"
        txtFechaInfo?.text = "Fecha: ${formatearFecha(orden.getString("fechaRegistro"))}"

        val productosBuilder = StringBuilder()
        var total = 0.0

        for (i in 0 until productos.length()){
            val producto = productos.getJSONObject(i)
            val cantidad = producto.getInt("cantidadProducto")
            val precio = producto.getDouble("precio")
            val subtotal = cantidad * precio

            productosBuilder.append("${producto.getString("nombre")}\n")
            productosBuilder.append("$cantidad x \$${precio.format(2)} = \$${subtotal.format(2)}\\n\\n")

            total += subtotal
        }
        txtProductos?.text = productosBuilder.toString()
        txtTotal?.text = "Total: $${total.format(2)}"
    }

    private fun formatearFecha(fecha: String): String{
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return outputFormat.format(inputFormat.parse(fecha)!!)
    }

    private fun Double.format(decimales: Int) = "%.${decimales}f".format(this)
}