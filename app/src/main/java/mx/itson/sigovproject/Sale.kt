package mx.itson.sigovproject

data class Sale(
    val id: Int,
    val noMesa: Int,
    val monto: Double,
    val fechaRegistro: String,
    val metodoPago: String
)
