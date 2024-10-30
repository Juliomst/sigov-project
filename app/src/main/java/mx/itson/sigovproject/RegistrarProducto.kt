package mx.itson.sigovproject

import android.R
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class RegistrarProducto : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mx.itson.sigovproject.R.layout.activity_registrar_producto)

        // Obtener referencia al botón de registrar
        val btnRegistrar: Button = findViewById(mx.itson.sigovproject.R.id.btn_registrar)

        // Configurar OnClickListener para mostrar el cuadro de diálogo
        btnRegistrar.setOnClickListener {
            mostrarDialogoConfirmacion()
        }
    }

    private fun mostrarDialogoConfirmacion() {
        AlertDialog.Builder(this)
            .setTitle("Confirmacion de Registro")
            .setMessage("¿Deseas Registrar El Producto?")
            .setPositiveButton("CONFIRMAR") { dialog, _ ->
                Toast.makeText(this, "Producto registrado exitosamente", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("VOLVER") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}