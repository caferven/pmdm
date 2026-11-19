package com.example.pmdm.testing

import com.example.pmdm.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow

/** Ajustes falsos en memoria para los tests: evitan DataStore y Android. */
class FakeSettingsRepository(initialOnlyFavorites: Boolean = false) : SettingsRepository {
    private val state = MutableStateFlow(initialOnlyFavorites)

    override val onlyFavorites = state

    override suspend fun setOnlyFavorites(value: Boolean) {
        state.value = value
    }
}
