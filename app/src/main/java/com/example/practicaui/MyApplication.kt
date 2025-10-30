package com.example.practicaui

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        // Activar logging de Firestore para desarrollo
        try {
            FirebaseFirestore.setLoggingEnabled(true)
        } catch (e: Exception) {
            Log.w("MyApplication", "No se pudo activar el logging de Firestore", e)
        }
    }
}
