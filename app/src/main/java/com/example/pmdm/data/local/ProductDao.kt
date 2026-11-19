package com.example.pmdm.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a la tabla `products` (DAO = *Data Access Object*).
 *
 * Solo se escribe la interfaz con las consultas SQL: Room genera la implementación en compilación
 * y comprueba que el SQL es válido antes de que la app llegue a ejecutarse.
 */
@Dao
interface ProductDao {

    /**
     * Productos visibles, ordenados por id. Al devolver un [Flow], Room vuelve a emitir la lista
     * automáticamente cada vez que la tabla cambia: la UI se actualiza sin pedir nada.
     */
    @Query("SELECT * FROM products WHERE deleted = 0 ORDER BY id")
    fun observeVisible(): Flow<List<ProductEntity>>

    /** Todas las filas, también las borradas. Se usa al refrescar para no perder esas marcas. */
    @Query("SELECT * FROM products")
    suspend fun getAll(): List<ProductEntity>

    /** Inserta las filas nuevas y actualiza las que ya existen (mismo `id`). */
    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>)

    /** Invierte el favorito de [id] directamente en SQL. */
    @Query("UPDATE products SET favorite = NOT favorite WHERE id = :id")
    suspend fun toggleFavorite(id: Int)

    /** Marca (o desmarca) [id] como borrado. */
    @Query("UPDATE products SET deleted = :deleted WHERE id = :id")
    suspend fun setDeleted(id: Int, deleted: Boolean)
}
