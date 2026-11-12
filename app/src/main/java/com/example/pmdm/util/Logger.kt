package com.example.pmdm.util

import android.util.Log
import com.example.pmdm.BuildConfig

/**
 * Etiqueta común de todos los logs de la app.
 * Con una única etiqueta se puede filtrar Logcat con `tag:MiTienda`.
 */
const val TAG = "MiTienda"

/**
 * Escribe [message] en Logcat (nivel debug) solo cuando `BuildConfig.DEBUG_LOGS` es `true`,
 * es decir, en el build debug.
 *
 * Se comprueba aquí y no en cada llamada para que el resto del código no tenga que
 * preocuparse de si está en producción.
 */
fun logDebug(message: String) {
    if (BuildConfig.DEBUG_LOGS) {
        Log.d(TAG, message)
    }
}
