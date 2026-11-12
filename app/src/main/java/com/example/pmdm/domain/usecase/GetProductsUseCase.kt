package com.example.pmdm.domain.usecase

import com.example.pmdm.domain.model.Product
import com.example.pmdm.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Caso de uso: obtener los productos, opcionalmente filtrados por nombre.
 *
 * @property repository origen de los productos.
 */
class GetProductsUseCase(private val repository: ProductRepository) {

    /**
     * Devuelve un [Flow] con los productos cuyo nombre contiene [filter].
     *
     * La búsqueda ignora mayúsculas y minúsculas. Un filtro vacío (o solo espacios) devuelve
     * todos los productos. Se aplica sobre el flujo para que el resultado se actualice solo
     * cuando cambian los datos del repositorio.
     */
    operator fun invoke(filter: String): Flow<List<Product>> {
        val text = filter.trim()
        return repository.observeProducts().map { products ->
            if (text.isEmpty()) products
            else products.filter { it.name.contains(text, ignoreCase = true) }
        }
    }
}
