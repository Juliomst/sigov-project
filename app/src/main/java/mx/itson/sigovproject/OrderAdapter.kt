package mx.itson.sigovproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderAdapter (
    private val orders: List<Order>,
    private val onOrderClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>(){

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val txtMesa: TextView = view.findViewById(R.id.txtMesa)
        val txtDescripcion: TextView  = view.findViewById(R.id.txtDescripcion)
        val txtFecha: TextView = view.findViewById(R.id.txtFecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.txtMesa.text = "Mesa: ${order.mesa}"
        holder.txtDescripcion.text = order.descripcion
        holder.txtFecha.text = order.fecha
        holder.itemView.setOnClickListener{ onOrderClick(order) }
    }

    override fun getItemCount() = orders.size
}