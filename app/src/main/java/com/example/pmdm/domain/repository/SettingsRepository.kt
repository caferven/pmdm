package com.example.pmdm.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Contrato de los ajustes del usuario: datos pequeños y sueltos que la app debe recordar entre sesiones.
 *
 * Es otra interfaz distinta de [ProductRepository] porque son datos de naturaleza distinta: los productos
 * son muchos y tienen estructura (Room); un ajuste es un único valor (DataStore).
 */
interface SettingsRepository {

    /** Emite `true` si el usuario quiere ver solo sus favoritos, y vuelve a emitir cuando cambia. */
    val onlyFavorites: Flow<Boolean>

    /** Guarda si la lista debe mostrar solo los favoritos. */
    suspend fun setOnlyFavorites(value: Boolean)
}
