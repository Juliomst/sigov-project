package mx.itson.sigovproject

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ActualizarProducto : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actualizar_producto)
        val btnActualizar: Button = findViewById(mx.itson.sigovproject.R.id.btn_actualizar)
        btnActualizar.setOnClickListener {
            mostrarDialogoConfirmacion()
        }
    }

    private fun mostrarDialogoConfirmacion() {
        AlertDialog.Builder(this)
            .setTitle("Confirmacion de Actualizacion")
            .setMessage("¿Deseas Actualizar El Producto?")
            .setPositiveButton("CONFIRMAR") { dialog, _ ->
                //AQUI HAY QUE ACTUALIZARLO PARA CUANDO DEVUELVA LA CONFIRMACION LA BD DE QUE SE
                //ACTUALIZO EXITOSAMENTE
                Toast.makeText(this, "Producto registrado exitosamente", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("VOLVER") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}