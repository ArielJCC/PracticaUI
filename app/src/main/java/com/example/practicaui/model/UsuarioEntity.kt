package com.example.practicaui.model
import androidx.room.PrimaryKey
import androidx.room.Entity
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val genero: String,
    val edad: Int,
    val notificaciones: Boolean,
    val estado: String,
    val latitud: Double? = null,
    val longitud: Double? = null,
    // NUEVOS CAMPOS:
    val fotoLocalUri: String? = null,   // URI local (galería / MediaStore)
    val fotoRemotaUrl: String? = null   // URL pública en Firebase Storage (guardada en Firestore)

)