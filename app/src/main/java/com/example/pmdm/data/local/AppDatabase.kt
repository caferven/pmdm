package com.example.pmdm.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos SQLite de la app. Las entidades listadas en `entities` son sus tablas.
 *
 * `version` hay que subirla (y escribir una migración) cada vez que cambie el esquema de una tabla;
 * si no, Room se niega a abrir una base de datos con otra forma.
 */
@Database(entities = [ProductEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /** DAO de productos; Room genera la implementación. */
    abstract fun productDao(): ProductDao

    companion object {
        /** Nombre del archivo de la base de datos dentro del almacenamiento privado de la app. */
        private const val DATABASE_NAME = "mitienda.db"

        /**
         * Abre (o crea la primera vez) la base de datos. Construirla es costoso, por eso solo debe
         * hacerse una vez: lo garantiza el [com.example.pmdm.di.AppContainer].
         */
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME).build()
    }
}
