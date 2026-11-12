package com.example.pmdm.domain.repository

import com.example.pmdm.domain.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de acceso a los productos.
 *
 * Vive en el dominio y lo implementa la capa de datos. Así el dominio define qué necesita
 * sin saber de dónde salen los datos (red, base de datos, memoria...).
 */
interface ProductRepository {

    /** Emite la lista actual de productos y vuelve a emitir cada vez que cambia. */
    fun observeProducts(): Flow<List<Product>>

    /** Vuelve a pedir los productos a la fuente de datos. Puede lanzar excepción si falla. */
    suspend fun refresh()

    /** Marca como favorito el producto [id], o le quita la marca si ya lo era. */
    suspend fun toggleFavorite(id: Int)

    /** Elimina el producto [id] de la lista (puede deshacerse con [restore]). */
    suspend fun delete(id: Int)

    /** Deshace un [delete]: el producto vuelve a aparecer en su posición original. */
    suspend fun restore(id: Int)
}
