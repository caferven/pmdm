package com.example.pmdm.di

import android.content.Context
import com.example.pmdm.data.local.AppDatabase
import com.example.pmdm.data.local.SettingsRepositoryImpl
import com.example.pmdm.data.local.settingsDataStore
import com.example.pmdm.data.remote.createProductApi
import com.example.pmdm.data.repository.ProductRepositoryImpl
import com.example.pmdm.domain.repository.ProductRepository
import com.example.pmdm.domain.repository.SettingsRepository
import com.example.pmdm.domain.usecase.GetProductsUseCase
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Contenedor de dependencias manual (más adelante veremos Hilt para la inyección de dependencias).
 *
 * Se crea una sola vez en [com.example.pmdm.MiTiendaApp], así que los objetos que construye
 * viven mientras viva la aplicación y se comparten entre pantallas. Recibe el [Context] porque la base de datos
 * necesita acceso al almacenamiento de la app.
 */
class AppContainer(context: Context) {

    /**
     * Interruptor de demostración: cuando vale `true`, la API falsa falla. Puede activarse desde el catálogo de
     * componentes para poder ver el estado de error de la lista sin tocar el código.
     */
    val forceNetworkError = MutableStateFlow(false)

    private val api = createProductApi(simulateError = { forceNetworkError.value })

    private val database = AppDatabase.create(context)

    /** Repositorio de productos: red + base de datos local. */
    val productRepository: ProductRepository = ProductRepositoryImpl(api, database.productDao())

    /** Ajustes del usuario (DataStore). */
    val settingsRepository: SettingsRepository = SettingsRepositoryImpl(context.applicationContext.settingsDataStore)

    /** Caso de uso para listar y filtrar productos. */
    val getProductsUseCase = GetProductsUseCase(productRepository)
}
