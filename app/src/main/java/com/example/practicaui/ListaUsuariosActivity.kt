package com.example.practicaui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practicaui.cloud.FirebaseService
import com.example.practicaui.database.AppDatabase
import com.example.practicaui.model.UsuarioEntity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
class ListaUsuariosActivity : AppCompatActivity() {
    private lateinit var rvUsuarios: RecyclerView
    private lateinit var adapter: UsuarioAdapter
    private lateinit var btnFirebase: Button
    private lateinit var btnRoom: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_usuarios)
        rvUsuarios = findViewById(R.id.rvUsuarios)
        btnFirebase = findViewById(R.id.btnFirebase)
        btnRoom = findViewById(R.id.btnRoom)
        // 🔗 Configurar RecyclerView
        rvUsuarios.layoutManager = LinearLayoutManager(this)
        adapter = UsuarioAdapter { usuario ->
            abrirUbicacionEnMaps(usuario) }
        rvUsuarios.adapter = adapter

        val dao = AppDatabase.getInstance(this).usuarioDao()
        // 🔹 Ver usuarios desde Room
        btnRoom.setOnClickListener {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    dao.obtenerTodos().collectLatest { lista ->
                        adapter.submitList(lista)
                    }
                }
            }
        }
        //  Ver usuarios desde Firestore
        btnFirebase.setOnClickListener {
            FirebaseService.obtenerUsuarios { listaFirestore ->
                adapter.submitList(listaFirestore)
            }
        }
    }
    private fun abrirUbicacionEnMaps(usuario: UsuarioEntity) {
        if (usuario.latitud != null && usuario.longitud != null) {
            val uri = Uri.parse("geo:${usuario.latitud},${usuario.longitud}?q=${usuario.latitud},${usuario.longitud} (${usuario.nombre})")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        } else {
            Toast.makeText(this, "Este usuario no tiene ubicación registrada", Toast.LENGTH_SHORT).show()
        }
    }
}
