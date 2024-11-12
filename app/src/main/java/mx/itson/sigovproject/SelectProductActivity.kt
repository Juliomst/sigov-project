package mx.itson.sigovproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONObject

class SelectProductActivity : AppCompatActivity() {
    private var gridProductos: GridLayout? = null
    private var btnVolverProducto: MaterialButton? = null
    private var idOrden: String = ""
    private var categoria: String = ""

    companion object {
        private const val TIMEOUT_MS = 15000
        private const val MAX_RETRIES = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_product)

        initializeViews()
        validateIntentExtras()
        setupButtons()
        cargarProductos()
    }

    private fun initializeViews() {
        gridProductos = findViewById(R.id.gridProductos)
        btnVolverProducto = findViewById(R.id.btnVolverProducto)
    }

    private fun validateIntentExtras() {
        idOrden = intent.getStringExtra("idOrden") ?: ""
        categoria = intent.getStringExtra("categoria") ?: ""

        if (idOrden.isEmpty() || categoria.isEmpty()) {
            showError("Error: Datos insuficientes")
            finish()
        }
    }

    private fun setupButtons() {
        btnVolverProducto?.setOnClickListener { finish() }
    }

    private fun mostrarDialogoCantidad(producto: JSONObject) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_cantidad_producto, null)

        val txtCantidad = dialogView.findViewById<TextView>(R.id.txtCantidad)
        val btnMenos = dialogView.findViewById<MaterialButton>(R.id.btnMenos)
        val btnMas = dialogView.findViewById<MaterialButton>(R.id.btnMas)
        val etDescripcion = dialogView.findViewById<TextInputEditText>(R.id.etDescripcion)

        var cantidad = 1
        txtCantidad.text = cantidad.toString()

        setupCantidadControls(btnMenos, btnMas, txtCantidad) { newCantidad ->
            cantidad = newCantidad
        }

        showProductDialog(dialogView, producto) { descripcion ->
            if (validateProductData(cantidad)) {
                agregarProductoOrden(producto, cantidad, descripcion)
            }
        }
    }

    private fun setupCantidadControls(
        btnMenos: MaterialButton,
        btnMas: MaterialButton,
        txtCantidad: TextView,
        onCantidadChanged: (Int) -> Unit
    ) {
        var cantidad = 1

        btnMenos.setOnClickListener {
            if (cantidad > 1) {
                cantidad--
                txtCantidad.text = cantidad.toString()
                onCantidadChanged(cantidad)
            }
        }

        btnMas.setOnClickListener {
            cantidad++
            txtCantidad.text = cantidad.toString()
            onCantidadChanged(cantidad)
        }
    }

    private fun showProductDialog(
        dialogView: android.view.View,
        producto: JSONObject,
        onConfirm: (String) -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle(producto.getString("nombre"))
            .setView(dialogView)
            .setPositiveButton("Agregar") { _, _ ->
                val descripcion = dialogView.findViewById<TextInputEditText>(R.id.etDescripcion)
                    ?.text?.toString() ?: ""
                onConfirm(descripcion)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun validateProductData(cantidad: Int): Boolean {
        if (cantidad <= 0) {
            showError("La cantidad debe ser mayor a 0")
            return false
        }
        return true
    }

    private fun cargarProductos() {
        val url = BuildConfig.SERVER_IP+"obtenerProductos.php"

        val request = object : StringRequest(
            Method.POST,
            url,
            { response -> handleProductosResponse(response) },
            { error -> handleError(error) }
        ) {
            override fun getParams() = hashMapOf("categoria" to categoria)
        }

        request.retryPolicy = DefaultRetryPolicy(
            TIMEOUT_MS,
            MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun handleProductosResponse(response: String) {
        try {
            val jsonResponse = JSONArray(response)
            setupProductGrid(jsonResponse)
        } catch (e: Exception) {
            showError("Error al procesar productos: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupProductGrid(productos: JSONArray) {
        gridProductos?.removeAllViews()

        for (i in 0 until productos.length()) {
            val producto = productos.getJSONObject(i)
            val button = createProductButton(producto)
            gridProductos?.addView(button)
        }
    }

    private fun createProductButton(producto: JSONObject): MaterialButton {
        return MaterialButton(this).apply {
            text = "${producto.getString("nombre")}\n$${producto.getDouble("precio")}"
            textSize = 16f
            setBackgroundColor(getColor(R.color.buttonPallette))
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(8, 8, 8, 8)
            }
            setOnClickListener { mostrarDialogoCantidad(producto) }
        }
    }

    private fun agregarProductoOrden(producto: JSONObject, cantidad: Int, descripcion: String) {
        val url = BuildConfig.SERVER_IP+"agregarProductoOrden.php"

        // Debug logging
        Log.d("SIGOV", """
        Intentando agregar producto:
        URL: $url
        ID Orden: $idOrden
        ID Producto: ${producto.getString("id_producto")}
        Cantidad: $cantidad
        Descripción: $descripcion
    """.trimIndent())

        val request = object : StringRequest(
            Method.POST,
            url,
            { response ->
                Log.d("SIGOV", "Respuesta del servidor: $response")
                handleAgregarResponse(response)
            },
            { error ->
                Log.e("SIGOV", "Error Volley: ${error.message}", error)
                if (error.networkResponse != null) {
                    try {
                        val responseData = String(error.networkResponse.data)
                        Log.e("SIGOV", "Error response data: $responseData")
                    } catch (e: Exception) {
                        Log.e("SIGOV", "Error al leer response data", e)
                    }
                }
                handleError(error)
            }
        ) {
            override fun getParams() = hashMapOf<String, String>().apply {
                put("id_orden", idOrden)
                put("id_producto", producto.getString("id_producto"))
                put("cantidadProducto", cantidad.toString())
                if (descripcion.isNotBlank()) {
                    put("descripcion", descripcion.trim())
                }
            }
        }

        request.retryPolicy = DefaultRetryPolicy(
            TIMEOUT_MS,
            MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun handleAgregarResponse(response: String) {
        try {
            val jsonResponse = JSONObject(response)
            when (jsonResponse.getString("status")) {
                "success" -> {
                    showSuccess(jsonResponse.getString("message"))
                    finish()
                }
                else -> showError(jsonResponse.getString("message"))
            }
        } catch (e: Exception) {
            showError("Error al procesar respuesta: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun handleError(error: com.android.volley.VolleyError) {
        showError("Error de conexión: ${error.message}")
        error.printStackTrace()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}