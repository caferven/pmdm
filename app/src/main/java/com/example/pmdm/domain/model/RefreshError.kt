package com.example.pmdm.domain.model

/**
 * Por qué ha fallado la descarga de productos, en categorías que la UI sabe explicar al usuario.
 *
 * El dominio no conoce HTTP ni Retrofit: la capa de datos traduce cada excepción técnica a una de estas razones.
 */
enum class RefreshError {
    /** El dispositivo no tiene conexión o no se puede llegar al servidor. */
    NO_CONNECTION,

    /** El servidor tardó demasiado en responder. */
    TIMEOUT,

    /** El servidor falló (códigos HTTP 5xx). Reintentar más tarde suele bastar. */
    SERVER,

    /** El servidor rechazó la petición (códigos HTTP 4xx). Reintentar no lo arregla. */
    CLIENT,

    /** El servidor respondió, pero con datos que la app no sabe interpretar. */
    INVALID_RESPONSE,

    /** Cualquier otro fallo inesperado. */
    UNKNOWN,
}

/**
 * Excepción que lanza el repositorio cuando no puede refrescar los productos.
 *
 * @property reason causa clasificada, pensada para mostrarse al usuario.
 * @param cause excepción original, por si hace falta depurarla.
 */
class RefreshException(val reason: RefreshError, cause: Throwable? = null) : Exception(reason.name, cause)
