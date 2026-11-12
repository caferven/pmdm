package com.example.pmdm.data.repository

import com.example.pmdm.data.remote.FakeProductApi
import com.example.pmdm.data.remote.toDomain
import com.example.pmdm.domain.model.Product
import com.example.pmdm.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

/**
 * Implementación del repositorio que guarda los productos en memoria.
 * Más adelante se sustituirá por una base de datos Room sin tocar dominio ni UI.
 */
class ProductRepositoryImpl(private val api: FakeProductApi) : ProductRepository {

    // Esta es la lista en memoria que la UI observa.
    private val products = MutableStateFlow<List<Product>>(emptyList())

    // Borrado lógico: en lugar de quitar el producto de la lista se apunta su id. Así "Deshacer" solo
    // tiene que quitar el `id` y el producto reaparece en su sitio. Con Room (acceso a bse de datos) será un borrado real.
    private val deletedIds = MutableStateFlow<Set<Int>>(emptySet())

    override fun observeProducts(): Flow<List<Product>> =
        combine(products, deletedIds) { list, deleted -> list.filter { it.id !in deleted } }

    override suspend fun refresh() {
        // Se conservan los favoritos ya marcados: la API no sabe nada de ellos.
        val favoriteIds = products.value.filter { it.favorite }.map { it.id }.toSet()
        products.value = api.getProducts().map { dto ->
            dto.toDomain().let { it.copy(favorite = it.id in favoriteIds) }
        }
    }

    override suspend fun toggleFavorite(id: Int) {
        // Coge el valor actual y lo pasa a la lambda, que debe devolver el nuevo valor.
        // El valor devuelto sustituye al anterior y avisa a quien esté observando.
        products.update { list ->
            list.map { if (it.id == id) it.copy(favorite = !it.favorite) else it }
        }
        // Con `list.map` se ha recorrido la lista y se ha generado otra con el mismo número de elementos.
    }
    /*
        Si se llama a toggleFavorite(2):
            antes:    [ P1(fav=false), P2(fav=false), P3(fav=false) ]
            después:  [ P1(fav=false), P2(fav=true),  P3(fav=false) ]   ← lista nueva, solo P2 es copia
     */

    override suspend fun delete(id: Int) = deletedIds.update { it + id }

    override suspend fun restore(id: Int) = deletedIds.update { it - id }
}
