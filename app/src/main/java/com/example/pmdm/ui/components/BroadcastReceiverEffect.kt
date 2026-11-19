package com.example.pmdm.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Escucha un *broadcast* (anuncio) mientras este Composable esté en pantalla.
 *
 * Un [BroadcastReceiver] se registra en código (no en el manifest): desde Android 8 casi todos los anuncios
 * del sistema ya no se entregan a receivers declarados en el manifest, para ahorrar batería.
 *
 * Registrar un receiver y olvidarse de darlo de baja es una fuga clásica. [DisposableEffect] lo evita:
 * registra al entrar en la composición y ejecuta `onDispose` al salir.
 *
 * @param action acción a escuchar, por ejemplo [Intent.ACTION_AIRPLANE_MODE_CHANGED].
 * @param onReceive se llama cada vez que llega el anuncio, con el [Intent] recibido.
 */
@Composable
fun BroadcastReceiverEffect(action: String, onReceive: (Intent) -> Unit) {
    val context = LocalContext.current
    // rememberUpdatedState: el receiver siempre llama a la versión más reciente de onReceive
    // sin tener que registrarse de nuevo cada vez que la lambda cambia.
    val currentOnReceive by rememberUpdatedState(onReceive)

    DisposableEffect(context, action) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = currentOnReceive(intent)
        }
        // RECEIVER_NOT_EXPORTED: solo recibe anuncios del sistema y de esta app, no de otras aplicaciones.
        ContextCompat.registerReceiver(context, receiver, IntentFilter(action), ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }
}
