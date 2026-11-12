package com.example.pmdm.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pmdm.MiTiendaApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel del catálogo de componentes. Solo gestiona el interruptor que fuerza el error de red
 * de la API falsa, para poder ver el estado de error de la lista.
 */
class ComponentCatalogViewModel(
    private val forceError: MutableStateFlow<Boolean>,
) : ViewModel() {

    /** `true` si la API falsa está forzada a fallar. */
    val forceNetworkError: StateFlow<Boolean> = forceError.asStateFlow()

    /** Activa o desactiva el error forzado. */
    fun setForceNetworkError(enabled: Boolean) {
        forceError.value = enabled
    }

    companion object {
        /** Fábrica que obtiene el interruptor del [com.example.pmdm.di.AppContainer]. */
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as MiTiendaApp).container
                ComponentCatalogViewModel(container.forceNetworkError)
            }
        }
    }
}
