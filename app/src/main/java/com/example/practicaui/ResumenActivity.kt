package com.example.practicaui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResumenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumen)

        val txtResumen = findViewById<TextView>(R.id.txtResumen)

        val nombre = intent.getStringExtra("nombre") ?: "No especificado"
        val genero = intent.getStringExtra("genero") ?: "No especificado"
        val notificaciones = intent.getStringExtra("notificaciones") ?: "No especificado"
        val estado = intent.getStringExtra("estado") ?: "No especificado"

        val resumen = """
            Nombre: $nombre
            Género: $genero
            Notificaciones: $notificaciones
            Estado: $estado
        """.trimIndent()

        txtResumen.text = resumen
    }
}
