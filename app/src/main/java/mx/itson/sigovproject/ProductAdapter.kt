package mx.itson.sigovproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject

class ProductAdapter(private val onItemClick: (JSONObject) -> Unit) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var products = mutableListOf<JSONObject>()

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNombre: TextView = view.findViewById(R.id.txtNombre)
        val txtPrecio: TextView = view.findViewById(R.id.txtPrecio)
        val txtTipo: TextView = view.findViewById(R.id.txtTipo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.txtNombre.text = product.getString("nombre")
        holder.txtPrecio.text = "$ ${product.getDouble("precio")}"
        holder.txtTipo.text = product.getString("tipoProducto")

        holder.itemView.setOnClickListener { onItemClick(product) }
    }

    override fun getItemCount() = products.size

    fun updateProducts(newProducts: List<JSONObject>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }
}