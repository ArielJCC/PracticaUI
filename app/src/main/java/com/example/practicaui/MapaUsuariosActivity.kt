package com.example.practicaui

import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.practicaui.model.UsuarioEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
class MapaUsuariosActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa_usuarios)
        webView = findViewById(R.id.webViewMapa)
        configurarWebView()
        cargarUsuarios()
    }
    private fun configurarWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowContentAccess = true
            allowFileAccess = true
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?)
            {
                super.onPageFinished(view, url)
                cargarUsuarios()
            }
        }
        webView.loadUrl("file:///android_asset/mapa.html")
    }
    private fun cargarUsuarios(){
        db.collection("usuarios").get()
            .addOnSuccessListener { documentos ->
                val lista = documentos.mapNotNull { doc ->
                    val nombre = doc.getString("nombre") ?:
                    return@mapNotNull null
                    val pais = doc.getString("pais") ?: ""
                    val lat = doc.getDouble("latitud")
                    val lon = doc.getDouble("longitud")
                    if (lat !=null && lon !=null){
                        UsuarioMapa(nombre, pais, lat, lon)
                    }else null
                }
                val json = Gson().toJson(lista)
                val escapedJson = json.replace("\"", "\\\"")
                Log.d("MAPA_JSON_ESCAPED", escapedJson)

                webView.evaluateJavascript("cargarUsuariosDesdeAndroid(\"$escapedJson\");", null)
            }
            .addOnFailureListener { excepcion ->
                val errorMsg = excepcion.message ?: "Error esconocido"
                Toast.makeText(this, "Error al cargar los datos del usuario: $errorMsg", Toast.LENGTH_LONG).show()
            }
    }
    data class UsuarioMapa (
        val nombre: String,
        val pais: String,
        val latitud: Double,
        val longitud: Double
    )
}
