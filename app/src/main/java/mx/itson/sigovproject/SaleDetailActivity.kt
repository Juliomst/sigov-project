package mx.itson.sigovproject

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class SaleDetailActivity : AppCompatActivity() {
    private var txtMesaInfo: TextView? = null
    private var txtFechaInfo: TextView? = null
    private var txtDescripcion: TextView? = null
    private var txtProductos: TextView? = null
    private var txtTotal: TextView? = null
    private var btnRealizarVenta: MaterialButton? = null
    private var idOrden: String = ""
    private var userData: String? = null
    private var totalVenta: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sale_detail)

        initializeViews()
        idOrden = intent.getStringExtra("idOrden") ?: ""
        userData = intent.getStringExtra("userData")

        if (userData == null || idOrden.isEmpty()){
            Toast.makeText(this , "Error: Datos no disponibles", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        cargarDetallesOrden()
        setupButtons()
    }

    private fun initializeViews(){
        txtMesaInfo = findViewById(R.id.txtMesaInfo)
        txtFechaInfo = findViewById(R.id.txtFechaInfo)
        txtDescripcion = findViewById(R.id.txtDescripcion)
        txtProductos = findViewById(R.id.txtProductos)
        txtTotal = findViewById(R.id.txtTotal)
        btnRealizarVenta = findViewById(R.id.btnRealizarVenta)
    }

    private fun setupButtons(){
        btnRealizarVenta?.setOnClickListener{
            showPaymentMethodDialog()
        }
    }

    private fun showPaymentMethodDialog(){
        MaterialAlertDialogBuilder(this)
            .setTitle("Método de pago")
            .setMessage("Seleccione el método de pago")
            .setNegativeButton("Efectivo") {_, _ ->
                registrarVenta("efectivo")
            }
            .setPositiveButton("Tarjeta") {_, _ ->
                showCardDetailsDialog()
            }
            .show()
    }

    private fun showCardDetailsDialog(){
        val dialogView = layoutInflater.inflate(R.layout.dialog_card_details, null)
        val etBanco = dialogView.findViewById<EditText>(R.id.etBanco)
        val etFolio = dialogView.findViewById<EditText>(R.id.etFolio)

        MaterialAlertDialogBuilder(this)
            .setTitle("Detalles de la tarjeta")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Aceptar") {_, _ ->
                val banco = etBanco.text.toString()
                val folio = etFolio.text.toString()

                if(banco.isBlank() || folio.isBlank()){
                    Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                registrarVenta("tarjeta", banco, folio)
            }
            .show()
    }

    private fun registrarVenta(metodoPago: String, banco: String = "", folio: String = ""){
        val url = BuildConfig.SERVER_IP + "registrarVenta.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try{
                    val jsonResponse = JSONObject(response)
                    if(jsonResponse.getString("status") == "success"){
                        Toast.makeText(this, "Venta registrada con éxito", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainMenuActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.putExtra("userData", userData)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(this, "Error: ${jsonResponse.getString("message")}", Toast.LENGTH_LONG).show()
                    }
                }catch(e: Exception){
                    Toast.makeText(this, "Error al procesar la respuesta: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["idOrden"] = idOrden
                params["monto"] = totalVenta.toString()
                params["metodoPago"] = metodoPago
                if (metodoPago == "tarjeta"){
                    params["banco"] = banco
                    params["folio"] = folio
                }
                return params
            }
        }
        queue.add(request)
    }

    private fun cargarDetallesOrden(){
        val url = BuildConfig.SERVER_IP + "obtenerDetallesOrden.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try{
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success"){
                        mostrarDetallesOrden(jsonResponse)
                    }else{
                        Toast.makeText(this, "Error ${jsonResponse.getString("message")}", Toast.LENGTH_LONG).show()
                    }
                }catch (e: Exception){
                    Toast.makeText(this, "Error al procesar la respuesta: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_LONG).show()
            }){
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
        txtDescripcion?.text = "Descripción: ${orden.getString("descripcion")}"

        val productosBuilder = StringBuilder()
        totalVenta = 0.0

        for (i in 0 until productos.length()){
            val producto = productos.getJSONObject(i)
            val cantidad = producto.getInt("cantidadProducto")
            val precio = producto.getDouble("precio")
            val subtotal = cantidad * precio

            productosBuilder.append("${producto.getString("nombre")}\n")
            productosBuilder.append("$cantidad x \$${precio.format(2)} = \$${subtotal.format(2)}\n\n")

            totalVenta += subtotal
        }
        txtProductos?.text = productosBuilder.toString()
        txtTotal?.text = "Total: \$${totalVenta.format(2)}"
    }

    private fun formatearFecha(fecha: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return outputFormat.format(inputFormat.parse(fecha)!!)
    }

    private fun Double.format(decimales: Int) = "%.${decimales}f".format(this)
}