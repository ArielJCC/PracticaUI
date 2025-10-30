package com.example.practicaui

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar Firebase y loggear el bucket (diagnóstico)
        val app = FirebaseApp.initializeApp(this)
        val bucket = try {
            app?.options?.storageBucket
        } catch (e: Exception) {
            null
        }
        Log.d("MyApplication", "Firebase initialized. storageBucket=$bucket")

        // Activar logging de Firestore para desarrollo
        try {
            FirebaseFirestore.setLoggingEnabled(true)
        } catch (e: Exception) {
            Log.w("MyApplication", "No se pudo activar el logging de Firestore", e)
        }
    }
}