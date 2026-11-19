package com.example.pmdm.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

// Datos de ejemplo de la tienda. No son textos de interfaz, así que no van en strings.xml.
private const val STORE_PHONE = "+34900123456"
private const val STORE_LATITUDE = 40.4168
private const val STORE_LONGITUDE = -3.7038

/**
 * Abre el marcador del teléfono con el número de la tienda ya escrito (intent **implícito**).
 *
 * No dice qué app debe responder, solo la acción (`ACTION_DIAL`) y los datos (`tel:`). Android busca las apps
 * que saben hacerlo. `ACTION_DIAL` solo *prepara* la llamada, y es el usuario quien la inicia: por eso no
 * necesita el permiso CALL_PHONE, que sí haría falta con `ACTION_CALL`.
 *
 * @return `false` si no hay ninguna app que pueda atender la acción (por ejemplo, una tablet sin teléfono).
 */
fun dialStore(context: Context): Boolean =
    startSafely(context, Intent(Intent.ACTION_DIAL, "tel:$STORE_PHONE".toUri()))

/**
 * Muestra la ubicación de la tienda en una app de mapas (intent **implícito** con un URI `geo:`).
 *
 * @param label nombre que el mapa pondrá en el marcador.
 * @return `false` si no hay ninguna app de mapas instalada.
 */
fun openStoreMap(context: Context, label: String): Boolean {
    // El texto tras `?q=` se escribe entre paréntesis para que el mapa lo use como etiqueta; hay que codificarlo.
    val uri = "geo:$STORE_LATITUDE,$STORE_LONGITUDE?q=$STORE_LATITUDE,$STORE_LONGITUDE(${Uri.encode(label)})"
    return startSafely(context, Intent(Intent.ACTION_VIEW, uri.toUri()))
}

/**
 * Lanza un intent implícito sin que la app se cierre si nadie puede responder.
 *
 * No se usa `resolveActivity` para comprobarlo antes: desde Android 11 exige declarar `<queries>` en el
 * manifest. Capturar la excepción es más simple y funciona igual.
 */
private fun startSafely(context: Context, intent: Intent): Boolean =
    try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
