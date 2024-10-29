package mx.itson.sigovproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class SelectCategory : AppCompatActivity() {
    private var btnComida: ImageButton? = null
    private var btnBebida: ImageButton? = null
    private var btnPostre: ImageButton? = null
    private var btnVolverCategoria: MaterialButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_category)

        btnComida = findViewById(R.id.btnComida)
        btnBebida = findViewById(R.id.btnBebida)
        btnPostre = findViewById(R.id.btnPostre)
        btnVolverCategoria = findViewById(R.id.btnVolverCategoria)

        setupButtons()
    }

    private fun setupButtons(){
        btnComida?.setOnClickListener{
            navigateToProducts("Comida")
        }
        btnBebida?.setOnClickListener{
            navigateToProducts("Bebida")
        }
        btnPostre?.setOnClickListener{
            navigateToProducts("Postre")
        }
        btnVolverCategoria?.setOnClickListener{
            finish()
        }
    }

    private fun navigateToProducts(categoria: String){
        val intent = Intent(this, SelectProductActivity::class.java)
        intent.putExtra("categoria", categoria)
        intent.putExtra("idOrden", getIntent().getStringExtra("idOrden"))
        startActivity(intent)
    }
}