package mx.itson.sigovproject

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.VolleyLog.TAG
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import retrofit2.http.Tag

class ManageProductForm : AppCompatActivity() {
    private var etNombre: TextInputEditText? = null
    private var etPrecio: TextInputEditText? = null
    private var spinnerTipoProducto: AutoCompleteTextView? = null
    private var etDescripcion: TextInputEditText? = null
    private var btnRegistrarProducto: MaterialButton? = null
    private var txtTituloAgregarProducto: TextView? = null

    private val tiposProducto = listOf("Comida", "Bebida", "Postre")
    private var selectedTipoProducto: String? = null

    private var mode: String? = null
    private var productoId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_product_form)

        mode = intent.getStringExtra("mode")
        val productoData = intent.getStringExtra("producto")

        initializeViews()
        setupSpinner()

        if(mode != null && productoData != null){
            setupForModeAndData(mode!!, productoData)
        }

        setupButtons()
    }

    private fun setupForModeAndData(mode: String, productoData: String){
        try{
            val producto = JSONObject(productoData)
            productoId = producto.getInt("id_producto")

            etNombre?.setText(producto.getString("nombre"))
            etPrecio?.setText(producto.getString("precio"))
            etDescripcion?.setText(producto.getString("descripcion"))
            spinnerTipoProducto?.setText(producto.getString("tipoProducto"), false)
            selectedTipoProducto = producto.getString("tipoProducto")

            when(mode){
                "ACTUALIZAR" -> {
                    txtTituloAgregarProducto?.text = "Actualizar Producto"
                    btnRegistrarProducto?.text = "ACTUALIZAR PRODUCTO"
                }
                "ELIMINAR" -> {
                    txtTituloAgregarProducto?.text = "Eliminar Producto"
                    btnRegistrarProducto?.text = "ELIMINAR PRODUCTO"

                    etNombre?.isEnabled = false
                    etPrecio?.isEnabled = false
                    spinnerTipoProducto?.isEnabled = false
                    etDescripcion?.isEnabled = false
                }
            }
        }catch(e: Exception){
            Log.e("ManageProdcutForm", "Error al cargar datos del producto: ${e.message}")
            Toast.makeText(this, "Error al cargar datos del prodcuto", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initializeViews(){
        etNombre = findViewById(R.id.etNombre)
        etPrecio = findViewById(R.id.etPrecio)
        spinnerTipoProducto = findViewById(R.id.spinnerTipoProducto)
        etDescripcion = findViewById(R.id.etDescripcion)
        btnRegistrarProducto = findViewById(R.id.btnRegistrarProducto)
        txtTituloAgregarProducto = findViewById(R.id.txtTituloAgregarProducto)
    }

    private fun setupSpinner(){
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tiposProducto)
        spinnerTipoProducto?.setAdapter(adapter)

        spinnerTipoProducto?.setText(tiposProducto[0], false)
        selectedTipoProducto = tiposProducto[0]

        spinnerTipoProducto?.setOnItemClickListener{ _, _, position, _ ->
            selectedTipoProducto = tiposProducto[position]
            Log.d(TAG, "Tipo de producto seleccionado: ${selectedTipoProducto}")
        }
    }

    private fun setupButtons(){
        btnRegistrarProducto?.setOnClickListener{
            when (mode){
                null -> if(validateFields()) showConfirmDialog()
                "ACTUALIZAR" -> if (validateFields()) showUpdateConfirmDialog()
                "ELIMINAR" -> showDeleteConfirmDialog()
            }
        }
    }

    private fun showUpdateConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar actualización")
            .setMessage("""
                ¿Estás seguro de actualizar este producto?
                
                Nombre: ${etNombre?.text}
                Precio: ${etPrecio?.text}
                Tipo: $selectedTipoProducto
                Descripción: ${etDescripcion?.text}
            """.trimIndent())
            .setPositiveButton("Sí") { _, _ ->
                actualizarProducto()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showDeleteConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de eliminar este producto? Esta acción no se puede deshacer.")
            .setPositiveButton("Sí") { _, _ ->
                eliminarProducto()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun actualizarProducto() {
        val url = "http://192.168.56.1:8080/sigov/actualizarProducto.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        Toast.makeText(this, "Producto actualizado exitosamente", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        val errorMessage = jsonResponse.getString("message")
                        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
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
                params["id_producto"] = productoId.toString()
                params["nombre"] = etNombre?.text.toString()
                params["precio"] = etPrecio?.text.toString()
                params["tipoProducto"] = selectedTipoProducto ?: ""
                params["descripcion"] = etDescripcion?.text.toString()
                return params
            }
        }
        queue.add(request)
    }

    private fun eliminarProducto() {
        val url = "http://192.168.56.1:8080/sigov/eliminarProducto.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        Toast.makeText(this, "Producto eliminado exitosamente", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        val errorMessage = jsonResponse.getString("message")
                        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
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
                params["id_producto"] = productoId.toString()
                return params
            }
        }
        queue.add(request)
    }

    private fun validateFields(): Boolean{
        val nombre = etNombre?.text.toString()
        val precio = etPrecio?.text.toString()
        val descripcion = etDescripcion?.text.toString()

        if(nombre.isEmpty() || precio.isEmpty() || descripcion.isEmpty()){
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }
        if(selectedTipoProducto == null || !tiposProducto.contains(selectedTipoProducto)){
            Toast.makeText(this, "Por favor seleccione un tipo de producto válido", Toast.LENGTH_SHORT).show()
            return false
        }
        try{
            precio.toDouble()
        }catch (e: NumberFormatException){
            Toast.makeText(this, "El precio debe ser un número válido", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun showConfirmDialog(){
        AlertDialog.Builder(this)
            .setTitle("Confirmar registro")
            .setMessage("""
                ¿Estas seguro de registrar este producto?
                
                Nombre: ${etNombre?.text}
                Precio: ${etPrecio?.text}
                Tipo: $selectedTipoProducto
                Descripcion: ${etDescripcion?.text}
            """.trimIndent())
            .setPositiveButton("Sí"){ _, _ ->
                registrarProducto()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun registrarProducto(){
        val url = "http://192.168.56.1:8080/sigov/agregarProducto.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                Log.d(TAG, "Respuesta del servidor: $response")
                try {
                    val jsonResponse = JSONObject(response)
                    Log.d(TAG, "JSON parseado: $jsonResponse")

                    if (jsonResponse.getString("status") == "success") {
                        Toast.makeText(this, "Producto registrado exitosamente", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        val errorMessage = jsonResponse.getString("message")
                        Log.e(TAG, "Error del servidor: $errorMessage")
                        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al procesar la respuesta: ${e.message}")
                    Log.e(TAG, "Respuesta que causó el error: $response")
                    Toast.makeText(this, "Error al procesar la respuesta: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Log.e(TAG, "Error de Volley: ${error.message}", error)
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["nombre"] = etNombre?.text.toString()
                params["precio"] = etPrecio?.text.toString()
                params["tipoProducto"] = selectedTipoProducto ?: tiposProducto[0]
                params["descripcion"] = etDescripcion?.text.toString()

                Log.d(TAG, "Parámetros enviados: $params")
                return params
            }
        }
        queue.add(request)
    }
}