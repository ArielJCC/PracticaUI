package com.example.practicaui

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class HomeActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var lastShakeTime = 0L
    private var shakeCount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val btnIrRegistro= findViewById<Button>(R.id.btnIrRegistro)
        val btnVerRegistros = findViewById<Button>(R.id.btnVerRegistros)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        btnIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
        btnVerRegistros.setOnClickListener {
            startActivity(Intent(this, ListaUsuariosActivity::class.java))
        }
        findViewById<Button>(R.id.btnVerMapa).setOnClickListener {
            startActivity(Intent(this, MapaUsuariosActivity::class.java))
        }

        // Ejecutar prueba rápida de Firestore al abrir la actividad (solo para verificación)
        FirestoreTest.run(this)

    }
    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,

            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0] // movimiento lateral
            val now = System.currentTimeMillis()
            // Detectar sacudida fuerte hacia la izquierda (X negativo)
            if (x < -8) { // puedes ajustar el valor para mayor o menor sensibilidad
                if (now - lastShakeTime > 1000) {
                    shakeCount = 1
                } else {
                    shakeCount++
                    if (shakeCount >= 2) {
                        Toast.makeText(this, "Vuelva pronto👽", Toast.LENGTH_LONG).show()
                                finishAffinity()
                    }
                }
                lastShakeTime = now
            }
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No se usa en este caso
    }

}