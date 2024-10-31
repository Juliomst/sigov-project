package mx.itson.sigovproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.LayoutInflaterCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.json.JSONObject

class SelectProductActivity : AppCompatActivity() {
    private var gridProductos: GridLayout? = null
    private var btnVolverProducto: MaterialButton? = null
    private var idOrden: String = ""
    private var categoria: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_product)

        gridProductos = findViewById(R.id.gridProductos)
        btnVolverProducto = findViewById(R.id.btnVolverProducto)

        idOrden = intent.getStringExtra("idOrden") ?: ""
        categoria = intent.getStringExtra("categoria") ?: ""

        if (idOrden.isEmpty() || categoria.isEmpty()){
            Toast.makeText(this, "Error: Datos insuficientes", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        cargarProductos()
        setupButtons()
    }

    private fun setupButtons(){
        btnVolverProducto?.setOnClickListener{
            finish()
        }
    }

    private fun mostrarDialogoCantidad(producto: JSONObject){
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_cantidad_producto, null)

        val txtCantidad = dialogView.findViewById<TextView>(R.id.txtCantidad)
        val btnMenos = dialogView.findViewById<MaterialButton>(R.id.btnMenos)
        val btnMas = dialogView.findViewById<MaterialButton>(R.id.btnMas)
        val etDescripcion = dialogView.findViewById<TextInputEditText>(R.id.etDescripcion)

        var cantidad = 1
        txtCantidad.text = cantidad.toString()

        btnMenos.setOnClickListener{
            if(cantidad > 1){
                cantidad--
                txtCantidad.text = cantidad.toString()
            }
        }

        btnMas.setOnClickListener{
            cantidad++
            txtCantidad.text = cantidad.toString()
        }

        AlertDialog.Builder(this)
            .setTitle(producto.getString("nombre"))
            .setView(dialogView)
            .setPositiveButton("Agregar"){ _, _ ->
                val descripcion = etDescripcion.text?.toString() ?: ""
                agregarProductoOrden(producto, cantidad, descripcion)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cargarProductos(){
        val url = "http://192.168.56.1:8080/sigov/obtenerProductos.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try{
                    val jsonResponse = JSONArray(response)
                    setupProductGrid(jsonResponse)
                }catch (e: Exception){
                    Toast.makeText(this, "Error al procesar productos: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(this, "Error de conexion: ${error.message}", Toast.LENGTH_LONG).show()
                error.printStackTrace()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["categoria"] = categoria
                return params
            }
        }
        queue.add(request)
    }

    private fun setupProductGrid(productos: JSONArray){
        gridProductos?.removeAllViews()

        for (i in 0 until productos.length()){
            val producto = productos.getJSONObject(i)
            val button = MaterialButton(this).apply {
                text = "${producto.getString("nombre")}\n$${producto.getDouble("precio")}"
                textSize = 16f
                setBackgroundColor(getColor(R.color.buttonPallette))
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }
                setOnClickListener{
                    mostrarDialogoCantidad(producto)
                }
            }
            gridProductos?.addView(button)
        }
    }

    private fun agregarProductoOrden(producto: JSONObject, cantidad: Int, descripcion: String) {
        val url = "http://192.168.56.1:8080/sigov/agregarProductoOrden.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    when (jsonResponse.getString("status")) {
                        "success" -> {
                            Toast.makeText(
                                this,
                                jsonResponse.getString("message"),
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        else -> {
                            Toast.makeText(
                                this,
                                "Error: ${jsonResponse.getString("message")}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Error al procesar respuesta: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(this,
                    "Error de conexión: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
                error.printStackTrace()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return HashMap<String, String>().apply {
                    put("id_orden", idOrden)
                    put("id_producto", producto.getString("id_producto"))
                    put("cantidadProducto", cantidad.toString())
                    if(descripcion.isNotBlank()) {
                        put("descripcion", descripcion.trim())
                    }
                }
            }
        }

        queue.add(request)
    }

}