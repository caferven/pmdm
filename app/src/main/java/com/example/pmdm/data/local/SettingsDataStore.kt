package com.example.pmdm.data.local

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

/**
 * DataStore de preferencias de la app, guardado en el archivo `settings.preferences_pb`.
 *
 * Se declara a nivel de archivo (no dentro de una clase) a propósito: el delegado `preferencesDataStore`
 * garantiza que solo existe **una** instancia por archivo, y dos instancias sobre el mismo archivo lanzarían
 * una excepción.
 */
val Context.settingsDataStore by preferencesDataStore(name = "settings")
