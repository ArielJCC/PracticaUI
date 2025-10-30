package com.example.practicaui.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.practicaui.dao.UsuarioDao
import com.example.practicaui.model.UsuarioEntity
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [UsuarioEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migración 2 -> 3: añade columnas para la foto
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE usuarios ADD COLUMN fotoLocalUri TEXT")
                database.execSQL("ALTER TABLE usuarios ADD COLUMN fotoRemotaUrl TEXT")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "appseptimob.db"
                )
                    // Conserva datos aplicando la migración:
                    .addMigrations(MIGRATION_2_3)
                    // Si no te importan los datos locales, puedes usar la línea de abajo en lugar de addMigrations:
                    // .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}