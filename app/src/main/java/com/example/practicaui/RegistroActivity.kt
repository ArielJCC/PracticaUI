package com.example.practicaui
import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.practicaui.cloud.FirebaseService
import com.example.practicaui.database.AppDatabase
import com.example.practicaui.model.UsuarioEntity
import kotlinx.coroutines.launch

class RegistroActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitud: Double? = null
    private var longitud: Double? = null
    private var yaMostroPermisoBloqueado = false
    private lateinit var btnRegistrar: Button

    // Foto
    private lateinit var btnFoto: Button
    private lateinit var imgPreview: ImageView
    private var fotoUri: Uri? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imgPreview.setImageURI(fotoUri)
        } else {
            fotoUri = null
        }
        validarPuedeRegistrar()
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            fotoUri = uri
            imgPreview.setImageURI(uri)
        }
        validarPuedeRegistrar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val edtNombre = findViewById<EditText>(R.id.edtNombre)
        val edtEdad = findViewById<EditText>(R.id.edtEdad)
        val spnGenero = findViewById<Spinner>(R.id.spnGenero)
        val chkNotificaciones = findViewById<CheckBox>(R.id.chkNotificaciones)
        val rgpEstado = findViewById<RadioGroup>(R.id.rgpEstado)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        btnFoto = findViewById(R.id.btnFoto)
        imgPreview = findViewById(R.id.imgPreview)

        btnRegistrar.isEnabled = false
        btnRegistrar.alpha = 0.5f
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val generos = arrayOf("Seleccione", "Masculino", "Femenino", "Otro")
        spnGenero.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, generos)

        btnFoto.setOnClickListener { mostrarOpcionesFoto() }

        btnRegistrar.setOnClickListener {
            val nombre = edtNombre.text.toString().trim()
            val edadText = edtEdad.text.toString().trim()

            if (nombre.isEmpty()) {
                Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (edadText.isEmpty()) {
                Toast.makeText(this, "La edad es obligatoria", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val edad = edadText.toIntOrNull()
            if (edad == null) {
                Toast.makeText(this, "Edad inválida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val genero = spnGenero.selectedItem.toString()
            if (genero == "Seleccione") {
                Toast.makeText(this, "Selecciona un género", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val estadoId = rgpEstado.checkedRadioButtonId
            if (estadoId == -1) {
                Toast.makeText(this, "Selecciona un estado civil", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val estado = findViewById<RadioButton>(estadoId).text.toString()

            val uriFoto = fotoUri
            if (uriFoto == null) {
                Toast.makeText(this, "Debes tomar o seleccionar una foto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usuario = UsuarioEntity(
                nombre = nombre,
                edad = edad,
                genero = genero,
                notificaciones = chkNotificaciones.isChecked,
                estado = estado,
                latitud = latitud,
                longitud = longitud,
                fotoLocalUri = uriFoto.toString()
            )

            val dao = AppDatabase.getInstance(this).usuarioDao()
            lifecycleScope.launch {
                val idLocal = dao.insertar(usuario)

                FirebaseService.guardarUsuarioConFoto(usuario, uriFoto) { ok, remoteUrl ->
                    if (ok && !remoteUrl.isNullOrEmpty()) {
                        lifecycleScope.launch {
                            val actualizado = usuario.copy(id = idLocal.toInt(), fotoRemotaUrl = remoteUrl)
                            dao.actualizar(actualizado)
                        }
                    }
                }

                runOnUiThread {
                    Toast.makeText(this@RegistroActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    edtNombre.setText("")
                    edtEdad.setText("")
                    chkNotificaciones.isChecked = false
                    rgpEstado.clearCheck()
                    spnGenero.setSelection(0)
                    fotoUri = null
                    imgPreview.setImageDrawable(null)
                    validarPuedeRegistrar()

                }
            }
        }
    }

    private fun mostrarOpcionesFoto() {
        val opciones = arrayOf("Tomar foto", "Elegir de la galería")
        AlertDialog.Builder(this)
            .setTitle("Foto de perfil")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> tomarFoto()
                    1 -> elegirDeGaleria()
                }
            }
            .show()
    }

    private fun tomarFoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 200)
            return
        }
        val uri = crearImagenEnGaleria()
        fotoUri = uri
        takePicture.launch(uri)
    }

    private fun crearImagenEnGaleria(): Uri {
        val nombre = "IMG_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, nombre)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PracticaUI")
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }

    private fun elegirDeGaleria() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 201)
            return
        }
        pickImage.launch("image/*")
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            btnRegistrar.isEnabled = false
            btnRegistrar.alpha = 0.5f
        } else {
            obtenerUbicacion()
            validarPuedeRegistrar()
        }
    }

    private fun validarPuedeRegistrar() {
        val tieneUbicacionPermiso = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val tieneFoto = fotoUri != null
        val habilitado = tieneUbicacionPermiso && tieneFoto
        btnRegistrar.isEnabled = habilitado
        btnRegistrar.alpha = if (habilitado) 1.0f else 0.5f
    }

    private fun obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                latitud = it.latitude
                longitud = it.longitude
                findViewById<TextView>(R.id.tvUbicacion).text = "Ubicación: ${latitud}, ${longitud}"
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacion()
                validarPuedeRegistrar()
            } else {
                if (!yaMostroPermisoBloqueado) {
                    yaMostroPermisoBloqueado = true
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Permiso de ubicación necesario")
                    builder.setMessage("Debes ir a Ajustes > Aplicaciones > Nombre_proyecto > Permisos y permitir la ubicación manualmente.")
                    builder.setPositiveButton("Ir a Ajustes") { _, _ ->
                        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    builder.setNegativeButton("Cancelar") { _, _ -> finish() }
                    builder.setCancelable(false)
                    builder.show()
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
        kotlin.system.exitProcess(0)
    }
}