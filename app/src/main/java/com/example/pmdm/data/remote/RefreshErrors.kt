package com.example.pmdm.data.remote

import com.example.pmdm.domain.model.RefreshError
import com.example.pmdm.domain.model.RefreshException
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

/**
 * Traduce una excepción de la capa de red a una [RefreshException] que el resto de la app entiende.
 *
 * Una llamada de red puede fallar de formas muy distintas y cada una pide un mensaje diferente:
 * - Sin conexión o servidor inalcanzable: `IOException` (el servidor ni llega a responder).
 * - Tiempo agotado: `SocketTimeoutException`, que es un tipo de `IOException`, por eso se mira antes.
 * - El servidor responde con un código de error: `HttpException` (4xx: culpa de la petición; 5xx: del servidor).
 * - El servidor responde bien pero el JSON no encaja con las clases: `SerializationException`.
 */
fun Throwable.toRefreshException(): RefreshException = when (this) {
    is RefreshException -> this
    is SocketTimeoutException -> RefreshException(RefreshError.TIMEOUT, this)
    is IOException -> RefreshException(RefreshError.NO_CONNECTION, this)
    is HttpException -> when (code()) {
        in 400..499 -> RefreshException(RefreshError.CLIENT, this)
        in 500..599 -> RefreshException(RefreshError.SERVER, this)
        else -> RefreshException(RefreshError.UNKNOWN, this)
    }
    is SerializationException -> RefreshException(RefreshError.INVALID_RESPONSE, this)
    else -> RefreshException(RefreshError.UNKNOWN, this)
}
