package com.example.pmdm.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.pmdm.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Ajustes del usuario guardados en un [DataStore] de preferencias (pares clave-valor).
 *
 * @property dataStore almacén de datos; se recibe como parámetro para poder usar otro en los tests.
 */
class SettingsRepositoryImpl(private val dataStore: DataStore<Preferences>) : SettingsRepository {

    // Cada preferencia se identifica por una clave tipada: así no se puede leer un Boolean como un String.
    private val onlyFavoritesKey = booleanPreferencesKey("only_favorites")

    // `data` es un Flow que emite todas las preferencias cada vez que alguna cambia.
    // Si la clave aún no existe (primera ejecución) devuelve null, y se usa `false` como valor por defecto.
    override val onlyFavorites: Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[onlyFavoritesKey] ?: false }

    // `edit` es suspend y transaccional: aplica el cambio en disco de forma segura aunque haya
    // varias corrutinas escribiendo a la vez (el problema clásico de SharedPreferences).
    override suspend fun setOnlyFavorites(value: Boolean) {
        dataStore.edit { preferences -> preferences[onlyFavoritesKey] = value }
    }
}
