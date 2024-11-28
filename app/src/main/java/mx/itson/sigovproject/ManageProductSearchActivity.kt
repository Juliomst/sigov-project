package mx.itson.sigovproject

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONObject

class ManageProductSearchActivity : AppCompatActivity() {
    private var etBuscarProducto: TextInputEditText? = null
    private var rvProductos: RecyclerView? = null
    private var mode: String? = null
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_product_search)

        mode = intent.getStringExtra("mode")

        initializeViews()
        setupRecyclerView()
        setupSearchListener()
    }

    private fun initializeViews(){
        etBuscarProducto = findViewById(R.id.etBuscarProducto)
        rvProductos = findViewById(R.id.rvProductos)
    }

    private fun setupRecyclerView(){
        adapter = ProductAdapter { producto ->
            val intent = Intent(this, ManageProductFormActivity::class.java)
            intent.putExtra("mode", mode)
            intent.putExtra("producto", producto.toString())
            startActivity(intent)
            finish()
        }
        rvProductos?.layoutManager = LinearLayoutManager(this)
        rvProductos?.adapter = adapter
    }

    private fun setupSearchListener(){
        etBuscarProducto?.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
            override fun afterTextChanged(s: Editable?) {
                buscarProductos(s.toString())
            }
        })
    }

    private fun buscarProductos(query: String){
        val url = BuildConfig.SERVER_IP+"buscarProductos.php"
        val queue = Volley.newRequestQueue(this)

        val request = object: StringRequest(
            com.android.volley.Request.Method.POST, url,
            { response ->
                try{
                    val jsonResponse = JSONArray(response)
                    val productos = mutableListOf<JSONObject>()

                    for(i in 0 until jsonResponse.length()){
                        productos.add(jsonResponse.getJSONObject(i))
                    }
                    adapter.updateProducts(productos)
                }catch(e: Exception){
                    Log.e("ProductSearch", "Error: ${e.message}")
                    Toast.makeText(this, "Error al buscar productos", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("ProductSearch", "Error: ${error.message}")
                Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
            }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["query"] = query
                return params
            }
        }
        queue.add(request)
    }
}