package com.example.practicaui.cloud
import android.net.Uri
import android.util.Log
import com.example.practicaui.model.UsuarioEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseService {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

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

    // Nuevo: crear documento, subir foto a Storage y actualizar con fotoUrl
    fun guardarUsuarioConFoto(
        usuario: UsuarioEntity,
        fotoUri: Uri,
        onComplete: (success: Boolean, remoteUrl: String?) -> Unit = { _, _ -> }
    ) {
        val data = hashMapOf(
            "nombre" to usuario.nombre,
            "genero" to usuario.genero,
            "edad" to usuario.edad,
            "notificaciones" to usuario.notificaciones,
            "estado" to usuario.estado,
            "latitud" to usuario.latitud,
            "longitud" to usuario.longitud
        )

        val docRef = db.collection("usuarios").document()
        docRef.set(data)
            .addOnSuccessListener {
                val photoRef = storage.reference.child("usuarios/${docRef.id}.jpg")
                photoRef.putFile(fotoUri)
                    .addOnSuccessListener {
                        photoRef.downloadUrl
                            .addOnSuccessListener { url ->
                                docRef.update("fotoUrl", url.toString())
                                    .addOnSuccessListener {
                                        Log.d("FirebaseService", "Usuario y foto guardados")
                                        onComplete(true, url.toString())
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("FirebaseService", "No se pudo actualizar fotoUrl", e)
                                        onComplete(false, null)
                                    }
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirebaseService", "No se pudo obtener downloadUrl", e)
                                onComplete(false, null)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirebaseService", "Fallo al subir la foto", e)
                        onComplete(false, null)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "No se pudo crear documento de usuario", e)
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