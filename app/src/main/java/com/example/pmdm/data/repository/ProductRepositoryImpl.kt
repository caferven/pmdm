package com.example.pmdm.data.repository

import com.example.pmdm.data.local.ProductDao
import com.example.pmdm.data.local.ProductEntity
import com.example.pmdm.data.local.toDomain
import com.example.pmdm.data.remote.ProductApi
import com.example.pmdm.data.remote.toRefreshException
import com.example.pmdm.domain.model.Product
import com.example.pmdm.domain.repository.ProductRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementación del repositorio con la base de datos como **única fuente de verdad**.
 *
 * - La UI siempre lee de la base de datos ([observeProducts]), nunca directamente de la red.
 * - [refresh] descarga los datos de la API y los guarda en la base de datos; al cambiar la tabla, Room avisa
 *   solo a quien esté observando.
 *
 * Esto es una **caché**: al abrir la app los productos aparecen al instante desde el disco y, si la red
 * falla, se siguen viendo los últimos que se descargaron.
 */
class ProductRepositoryImpl(
    private val api: ProductApi,
    private val dao: ProductDao,
) : ProductRepository {

    override fun observeProducts(): Flow<List<Product>> =
        dao.observeVisible().map { rows -> rows.map { it.toDomain() } }

    override suspend fun refresh() {
        // Primero la red: si falla, la base de datos queda como estaba (la caché sigue válida).
        val remote = try {
            api.getProducts().products
        } catch (e: CancellationException) {
            // Cancelar una corrutina también se señala con una excepción: nunca debe confundirse con un error.
            throw e
        } catch (e: Exception) {
            // Se traduce a un error del dominio para que la UI explique qué ha pasado sin conocer Retrofit.
            throw e.toRefreshException()
        }
        // La API no sabe nada de favoritos ni de borrados, así que se conservan los que ya estaban guardados.
        val saved = dao.getAll().associateBy { it.id }
        dao.upsertAll(
            remote.map { dto ->
                val old = saved[dto.id]
                ProductEntity(
                    id = dto.id,
                    name = dto.title,
                    category = dto.category,
                    price = dto.price,
                    imageUrl = dto.thumbnail,
                    favorite = old?.favorite ?: false,
                    deleted = old?.deleted ?: false,
                )
            },
        )
    }

    override suspend fun toggleFavorite(id: Int) = dao.toggleFavorite(id)

    // Borrado lógico (ver ProductEntity.deleted): permite deshacer sin guardar el producto en memoria.
    override suspend fun delete(id: Int) = dao.setDeleted(id, deleted = true)

    override suspend fun restore(id: Int) = dao.setDeleted(id, deleted = false)
}
