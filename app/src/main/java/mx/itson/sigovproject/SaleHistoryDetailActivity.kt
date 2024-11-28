package mx.itson.sigovproject


import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class SaleHistoryDetailActivity : AppCompatActivity() {
    private var txtMesaInfo: TextView? = null
    private var txtFechaInfo: TextView? = null
    private var txtDescripcion: TextView? = null
    private var txtProductos: TextView? = null
    private var txtTotal: TextView? = null
    private var txtMetodoPago: TextView? = null
    private var btnRegresar: MaterialButton? = null
    private var idVenta: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sale_history_detail)

        initializeViews()
        idVenta = intent.getStringExtra("idVenta") ?: ""

        if (idVenta.isEmpty()) {
            Toast.makeText(this, "Error: Datos no disponibles", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        cargarDetallesVenta()
        setupButtons()
    }

    private fun initializeViews() {
        txtMesaInfo = findViewById(R.id.txtMesaInfo)
        txtFechaInfo = findViewById(R.id.txtFechaInfo)
        txtDescripcion = findViewById(R.id.txtDescripcion)
        txtProductos = findViewById(R.id.txtProductos)
        txtTotal = findViewById(R.id.txtTotal)
        txtMetodoPago = findViewById(R.id.txtMetodoPago)
        btnRegresar = findViewById(R.id.btnRegresar)
    }

    private fun setupButtons() {
        btnRegresar?.setOnClickListener {
            finish()
        }
    }

    private fun cargarDetallesVenta() {
        val url = BuildConfig.SERVER_IP + "obtenerDetallesVenta.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        mostrarDetallesVenta(jsonResponse)
                    } else {
                        Toast.makeText(this, "Error: ${jsonResponse.getString("message")}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al procesar la respuesta: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["idVenta"] = idVenta
                return params
            }
        }
        queue.add(request)
    }

    private fun mostrarDetallesVenta(jsonResponse: JSONObject) {
        val venta = jsonResponse.getJSONObject("venta")
        val orden = jsonResponse.getJSONObject("orden")
        val productos = jsonResponse.getJSONArray("productos")
        val pago = jsonResponse.getJSONObject("pago")

        txtMesaInfo?.text = "Mesa: ${orden.getInt("noMesa")}"
        txtFechaInfo?.text = "Fecha: ${formatearFecha(venta.getString("fechaRegistro"))}"
        txtDescripcion?.text = "Descripción: ${orden.getString("descripcion")}"

        val productosBuilder = StringBuilder()
        var totalVenta = 0.0

        for (i in 0 until productos.length()) {
            val producto = productos.getJSONObject(i)
            val cantidad = producto.getInt("cantidadProducto")
            val precio = producto.getDouble("precio")
            val subtotal = cantidad * precio

            productosBuilder.append("${producto.getString("nombre")}\n")
            productosBuilder.append("$cantidad x $${precio.format(2)} = $${subtotal.format(2)}\n\n")

            totalVenta += subtotal
        }

        txtProductos?.text = productosBuilder.toString()
        txtTotal?.text = "Total: $${totalVenta.format(2)}"

        val metodoPago = pago.getString("metodoPago")
        val detallesPago = if (metodoPago == "tarjeta") {
            "Método de pago: Tarjeta\nBanco: ${pago.getString("banco")}"
        } else {
            "Método de pago: Efectivo"
        }
        txtMetodoPago?.text = detallesPago
    }

    private fun formatearFecha(fecha: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return outputFormat.format(inputFormat.parse(fecha)!!)
    }

    private fun Double.format(decimales: Int) = "%.${decimales}f".format(this)
}