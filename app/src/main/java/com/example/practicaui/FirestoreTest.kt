package com.example.practicaui

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreTest {
    fun run(context: Context) {
        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "ts" to System.currentTimeMillis(),
            "source" to "app_smoke_test"
        )
        db.collection("diagnostics").document("smoke_test")
            .set(data)
            .addOnSuccessListener {
                Log.d("FirestoreTest", "write ok")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreTest", "write failed", e)
                Toast.makeText(context, "Firestore: write failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}

