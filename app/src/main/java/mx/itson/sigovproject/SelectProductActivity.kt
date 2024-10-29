package mx.itson.sigovproject

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.LayoutInflaterCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ReportFragment.Companion.reportFragment
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
    private var categoria: String = ""
    private var idOrden: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_product)

        gridProductos = findViewById(R.id.gridProductos)
        btnVolverProducto = findViewById(R.id.btnVolverProducto)

        categoria = intent.getStringExtra("categoria") ?: ""
        idOrden = intent.getStringExtra("idOrden") ?: ""
        if (categoria.isEmpty() || idOrden.isEmpty()){
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

    private fun mostrarDialogoAgregarProducto(producto: JSONObject){
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val etDescripcion = dialogView.findViewById<TextInputEditText>(R.id.etDescripcion)
        val etCantidad = dialogView.findViewById<TextInputEditText>(R.id.etCantidad)
        val btnCancelar = dialogView.findViewById<MaterialButton>(R.id.btnCancelar)
        val btnAgregar = dialogView.findViewById<MaterialButton>(R.id.btnAgregar)

        btnCancelar.setOnClickListener{
            dialog.dismiss()
        }

        btnAgregar.setOnClickListener{
            val cantidad = etCantidad.text.toString().toIntOrNull() ?: 1
            val descripcion = etDescripcion.text.toString()
            agregarProductoOrden(producto.getInt("id_producto"), cantidad, descripcion)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun agregarProductoOrden(idProducto: Int, cantidad: Int, descripcion: String){
        val url = "http://192.168.56.1:8080/sigov/agregarProductoOrden.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try{
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success"){
                        Toast.makeText(this, "Producto agregado exitosamente", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show()
                    }
                }catch(e: Exception){
                    Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "id_orden" to idOrden,
                    "id_producto" to idProducto.toString(),
                    "cantidadProducto" to cantidad.toString(),
                    "descripcion" to descripcion
                )
            }
        }
        queue.add(request)
    }

    private fun cargarProductos(){
        val url = "http://192.168.56.1:8080/sigov/obtenerProductos.php"
        val queue = Volley.newRequestQueue(this)

        val request = object: StringRequest(
            Request.Method.POST, url,
            { response ->
                try{
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success"){
                        val productos = jsonResponse.getJSONArray("data")
                        setupProductGrid(productos)
                    }else{
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show()
                    }
                }catch(e: Exception){
                    Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error de conexión ${error.message}", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String>? {
                val params = HashMap<String, String>()
                params["tipoProducto"] = categoria
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
                    mostrarDialogoAgregarProducto(producto)
                }
            }
            gridProductos?.addView(button)
        }
    }
}