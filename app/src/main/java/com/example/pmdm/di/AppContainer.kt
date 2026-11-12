package com.example.pmdm.di

import com.example.pmdm.data.remote.FakeProductApi
import com.example.pmdm.data.repository.ProductRepositoryImpl
import com.example.pmdm.domain.repository.ProductRepository
import com.example.pmdm.domain.usecase.GetProductsUseCase
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Contenedor de dependencias manual (más adelante veremos Hilt para la inyección de dependencias).
 *
 * Se crea una sola vez en [com.example.pmdm.MiTiendaApp], así que los objetos que construye
 * viven mientras viva la aplicación y se comparten entre pantallas.
 */
class AppContainer {

    /**
     * Interruptor de demostración: cuando vale `true`, la API falsa falla. Puede activarse desde el catálogo de
     * componentes para poder ver el estado de error de la lista sin tocar el código.
     */
    val forceNetworkError = MutableStateFlow(false)

    private val api = FakeProductApi(simulateError = { forceNetworkError.value })

    /** Repositorio de productos. */
    val productRepository: ProductRepository = ProductRepositoryImpl(api)

    /** Caso de uso para listar y filtrar productos. */
    val getProductsUseCase = GetProductsUseCase(productRepository)
}
