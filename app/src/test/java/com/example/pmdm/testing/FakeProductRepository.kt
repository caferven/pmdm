package com.example.pmdm.testing

import com.example.pmdm.domain.model.Product
import com.example.pmdm.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

/**
 * Repositorio falso en memoria, compartido por los tests: evita red y Android.
 * Asignando [refreshError] se puede simular un fallo al refrescar.
 */
class FakeProductRepository(initial: List<Product>) : ProductRepository {
    val products = MutableStateFlow(initial)
    private val deletedIds = MutableStateFlow<Set<Int>>(emptySet())

    /** Si no es `null`, [refresh] lanza esta excepción. */
    var refreshError: Throwable? = null

    override fun observeProducts(): Flow<List<Product>> =
        combine(products, deletedIds) { list, deleted -> list.filter { it.id !in deleted } }

    override suspend fun refresh() {
        refreshError?.let { throw it }
    }

    override suspend fun toggleFavorite(id: Int) {
        products.update { list -> list.map { if (it.id == id) it.copy(favorite = !it.favorite) else it } }
    }

    override suspend fun delete(id: Int) = deletedIds.update { it + id }

    override suspend fun restore(id: Int) = deletedIds.update { it - id }
}
