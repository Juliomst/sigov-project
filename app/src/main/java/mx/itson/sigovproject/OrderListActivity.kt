package mx.itson.sigovproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class OrderListActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var userData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_list)

        userData = intent.getStringExtra("userData")
        recyclerView = findViewById(R.id.recyclerViewOrders)
        recyclerView?.layoutManager = LinearLayoutManager(this)

        cargarOrdenes()
    }

    private fun cargarOrdenes(){
        val url = BuildConfig.SERVER_IP + "obtenerOrdenesDelDia.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.GET, url,
            { response ->
                try{
                    val jsonResponse = JSONObject(response)
                    if(jsonResponse.getString("status") == "success"){
                        val ordenes = jsonResponse.getJSONArray("ordenes")
                        val orderList = mutableListOf<Order>()

                        for (i in 0 until ordenes.length()){
                            val orden = ordenes.getJSONObject(i)
                            orderList.add(
                                Order(
                                    orden.getInt("id_orden"),
                                    orden.getInt("noMesa"),
                                    orden.getString("descripcion"),
                                    orden.getString("fechaRegistro")
                                )
                            )
                        }

                        val adapter = OrderAdapter(orderList) { selectedOrder ->
                            val intent = Intent(this, SaleDetailActivity::class.java)
                            intent.putExtra("idOrden", selectedOrder.id.toString())
                            intent.putExtra("descripcion", selectedOrder.descripcion)
                            intent.putExtra("userData", userData)
                            startActivity(intent)
                        }
                        recyclerView?.adapter = adapter
                    }else{
                        Toast.makeText(this, "Error: ${jsonResponse.getString("message")}", Toast.LENGTH_LONG).show()
                    }
                }catch(e: Exception){
                    Toast.makeText(this, "Error al procesar la respuesta: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_LONG).show()
            }) {}
        queue.add(request)
    }
}