package com.example.pmdm.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Prueba el repositorio de ajustes con un DataStore **real** que escribe en una carpeta temporal:
 * no hace falta emulador y cada test empieza con el archivo vacío.
 */
class SettingsRepositoryImplTest {

    @get:Rule
    val folder = TemporaryFolder()

    // backgroundScope: DataStore necesita un ámbito donde trabajar; el test lo cancela al terminar.
    private fun TestScope.createRepository(file: File = File(folder.root, "settings.preferences_pb")) =
        SettingsRepositoryImpl(
            PreferenceDataStoreFactory.create(scope = backgroundScope) { file },
        )

    @Test
    fun onlyFavorites_defaultsToFalse() = runTest {
        assertEquals(false, createRepository().onlyFavorites.first())
    }

    @Test
    fun setOnlyFavorites_isSaved() = runTest {
        val repository = createRepository()

        repository.setOnlyFavorites(true)

        assertEquals(true, repository.onlyFavorites.first())
    }
}
