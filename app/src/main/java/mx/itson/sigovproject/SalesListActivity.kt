package mx.itson.sigovproject

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.util.Log

class SalesListActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var userData: String? = null
    private var txtTotalVentas: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_list)

        userData = intent.getStringExtra("userData")
        recyclerView = findViewById(R.id.recyclerViewSales)
        txtTotalVentas = findViewById(R.id.txtTotalVentas)
        recyclerView?.layoutManager = LinearLayoutManager(this)

        cargarVentas()
    }

    private fun cargarVentas() {
        val url = BuildConfig.SERVER_IP + "obtenerVentasDelDia.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    Log.d("SalesListActivity", "Respuesta completa: $response")

                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        val ventas = jsonResponse.getJSONArray("ventas")
                        val salesList = mutableListOf<Sale>()
                        val totalDia = jsonResponse.getDouble("total_dia")

                        for (i in 0 until ventas.length()) {
                            val venta = ventas.getJSONObject(i)
                            salesList.add(
                                Sale(
                                    venta.getInt("id_venta"),
                                    venta.getInt("noMesa"),
                                    venta.getDouble("monto"),
                                    venta.getString("fechaRegistro"),
                                    venta.getString("metodoPago")
                                )
                            )
                        }

                        if (salesList.isEmpty()) {
                            txtTotalVentas?.text = "No hay ventas registradas hoy"
                        } else {
                            txtTotalVentas?.text = "Total del día: $${String.format("%.2f", totalDia)}"
                        }

                        val adapter = SaleAdapter(salesList) { selectedSale ->
                            val intent = Intent(this, SaleHistoryDetailActivity::class.java)
                            intent.putExtra("idVenta", selectedSale.id.toString())
                            intent.putExtra("userData", userData)
                            startActivity(intent)
                        }
                        recyclerView?.adapter = adapter
                    } else {
                        val debugInfo = if (jsonResponse.has("debug_info")) {
                            "\nHora servidor: ${jsonResponse.getJSONObject("debug_info").getString("server_time")}"
                        } else ""

                        txtTotalVentas?.text = "No hay ventas registradas"
                        Toast.makeText(this,
                            "Mensaje: ${jsonResponse.getString("message")}$debugInfo",
                            Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("SalesListActivity", "Error al procesar respuesta", e)
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Log.e("SalesListActivity", "Error de conexión", error)
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_LONG).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                return HashMap<String, String>().apply {
                    put("Content-Type", "application/json; charset=utf-8")
                }
            }
        }
        queue.add(request)
    }
}