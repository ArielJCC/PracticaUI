package com.example.practicaui.cloud

import android.net.Uri
import android.util.Log
import com.example.practicaui.model.UsuarioEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseService {
    private val db = FirebaseFirestore.getInstance()

    // Inicializamos FirebaseStorage intentando usar el bucket reportado por FirebaseApp,
    // y si no hay uno válido, usamos un fallback con gs://practicaui-1aef1.appspot.com
    private val storage: FirebaseStorage by lazy {
        val reportedBucket = try {
            FirebaseApp.getInstance().options.storageBucket
        } catch (e: Exception) {
            null
        }

        val bucketUri = when {
            reportedBucket.isNullOrBlank() -> {
                // Fallback: usa el bucket basado en el project_id conocido
                // Ajusta aquí si tu bucket real es distinto
                "gs://practicaui-1aef1.appspot.com"
            }
            reportedBucket.startsWith("gs://") -> reportedBucket
            else -> "gs://$reportedBucket"
        }

        Log.d("FirebaseService", "Inicializando FirebaseStorage con bucket: $bucketUri (reported: $reportedBucket)")
        FirebaseStorage.getInstance(bucketUri)
    }

    fun guardarUsuario(usuario: UsuarioEntity){
        val data = hashMapOf(
            "nombre" to usuario.nombre,
            "genero" to usuario.genero,
            "edad" to usuario.edad,
            "notificaciones" to usuario.notificaciones,
            "estado" to usuario.estado,
            "latitud" to usuario.latitud,
            "longitud" to usuario.longitud
        )

        db.collection("usuarios").add(data)
            .addOnSuccessListener { Log.d("FirebaseService", "Usuario guardado exitosamente en firestore") }
            .addOnFailureListener {
                Log.e("FirebaseService", "Error al guardar el usuario en firestore", it)
            }
    }

    // Ejemplo de método de subida que incluye logs detallados y usa el storage inicializado arriba.
    fun subirFoto(localUri: Uri, onComplete: (Boolean, String?) -> Unit) {
        val refPath = "usuarios/${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child(refPath)
        Log.d("FirebaseService", "Subiendo foto a referencia: ${ref.path} en bucket: ${storage.reference.bucket}")

        ref.putFile(localUri)
            .addOnSuccessListener {
                ref.downloadUrl
                    .addOnSuccessListener { url ->
                        Log.d("FirebaseService", "Foto subida. downloadUrl=$url")
                        onComplete(true, url.toString())
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirebaseService", "No se pudo obtener downloadUrl (ref=${ref.path})", e)
                        onComplete(false, null)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Fallo al subir la foto (ref=${ref.path}, bucket=${storage.reference.bucket})", e)
                onComplete(false, null)
            }
    }

    fun obtenerUsuarios(callback:(List<UsuarioEntity>)->Unit) {
        db.collection("usuarios").get()
            .addOnSuccessListener { result ->
                val lista = result.map { doc ->
                    UsuarioEntity(
                        id = 0,
                        nombre = doc.getString("nombre") ?: "",
                        genero = doc.getString("genero") ?: "",
                        notificaciones = doc.getBoolean("notificaciones") ?: false,
                        estado = doc.getString("estado") ?: "",
                        edad = doc.getLong("edad")?.toInt() ?: 0,
                        latitud = doc.getDouble("latitud"),
                        longitud = doc.getDouble("longitud"),
                        fotoLocalUri = null,
                        fotoRemotaUrl = doc.getString("fotoUrl")
                    )
                }
                callback(lista)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al obtener los usuarios de firestore", e)
                callback(emptyList())
            }
    }
}