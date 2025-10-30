# PracticaUI — Instrucciones para activar y probar Firebase Firestore

Estos son los pasos para asegurar que Firebase/Firestore funcione con la app `PracticaUI`.

## Cambios realizados en el código (hechos por mí)
- `app/src/main/java/com/example/practicaui/MyApplication.kt`: Clase `Application` que inicializa Firebase al iniciar la app y habilita logging de Firestore en desarrollo.
- `app/src/main/AndroidManifest.xml`: Registrado `android:name=".MyApplication"` en el elemento `<application>`.
- Verificado `app/google-services.json` (coincide con `project_id: practicaui` y `package_name: com.example.practicaui`).

## Qué hay que hacer en la consola (requisito obligatorio)
1. Habilitar la API de Firestore (si aún no está habilitada):
   - Desde Google Cloud Console: https://console.developers.google.com/apis/api/firestore.googleapis.com/overview?project=practicaui
   - O con `gcloud` (si tienes instalado y autenticado):

```cmd
REM Ejecuta en Windows cmd.exe
gcloud services enable firestore.googleapis.com --project=practicaui
```

2. Crear la base de datos Firestore desde Firebase Console (recomendado):
   - Ve a https://console.firebase.google.com/
   - Selecciona el proyecto `practicaui` → Build → Firestore Database → Create database
   - Para pruebas, puedes seleccionar modo "prueba" y una ubicación cercana.

3. (Temporal para desarrollo) Ajustar reglas de Firestore:
   - En Firebase Console → Firestore → Rules, para pruebas:

```
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

No dejes estas reglas en producción.

## Cómo compilar y probar localmente
- Compilar (desde la raíz del proyecto en `cmd.exe`):

```cmd
cd C:\Users\CompuStore\Downloads\PracticaUI
gradlew.bat :app:assembleDebug
```

- ejecutar en dispositivo/emulador con Android Studio o `adb install` del APK generado en `app/build/outputs/apk/debug/`.

## Verificar en tiempo de ejecución
- Abrir `logcat` en Android Studio y filtrar por `Firebase` o `MyApplication`. Deberías ver mensajes que indican que Firebase se inicializó y (si se habilitó logging) mensajes de Firestore.
- Prueba rápida desde cualquier Activity (ejemplo de snippet):

```kotlin
val db = FirebaseFirestore.getInstance()
db.collection("test").document("ping").set(mapOf("ts" to System.currentTimeMillis()))
    .addOnSuccessListener { Log.d("FirestoreTest", "write ok") }
    .addOnFailureListener { Log.e("FirestoreTest", "write failed", it) }
```

## Notas finales
- Si después de habilitar la API recibes `PERMISSION_DENIED`, espera unos minutos y vuelve a probar (propagación de permisos).
- Si usas `gcloud` asegúrate de que la cuenta tenga permisos para modificar servicios del proyecto `practicaui`.

Si quieres, puedo:
- Añadir un pequeño ejemplo de escritura/lectura en la app y compilarlo por ti (si confirmas que puedo ejecutar compilación aquí), o
- Guiarte paso a paso para habilitar la API y crear la base de datos.

