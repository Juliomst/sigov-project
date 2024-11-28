package mx.itson.sigovproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class SaleAdapter(
    private val sales: List<Sale>,
    private val onItemClick: (Sale) -> Unit
) : RecyclerView.Adapter<SaleAdapter.SaleViewHolder>() {

    class SaleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtMesa: TextView = view.findViewById(R.id.txtMesa)
        val txtFecha: TextView = view.findViewById(R.id.txtFecha)
        val txtMonto: TextView = view.findViewById(R.id.txtMonto)
        val txtMetodoPago: TextView = view.findViewById(R.id.txtMetodoPago)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sale, parent, false)
        return SaleViewHolder(view)
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        val sale = sales[position]

        holder.txtMesa.text = "Mesa ${sale.noMesa}"
        holder.txtFecha.text = formatearFecha(sale.fechaRegistro)
        holder.txtMonto.text = "$${sale.monto.format(2)}"
        holder.txtMetodoPago.text = sale.metodoPago.capitalize()

        holder.itemView.setOnClickListener { onItemClick(sale) }
    }

    override fun getItemCount() = sales.size

    private fun formatearFecha(fecha: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return outputFormat.format(inputFormat.parse(fecha)!!)
    }

    private fun Double.format(decimales: Int) = "%.${decimales}f".format(this)
}