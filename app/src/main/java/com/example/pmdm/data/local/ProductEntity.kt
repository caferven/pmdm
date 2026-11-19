package com.example.pmdm.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pmdm.domain.model.Product

/**
 * Fila de la tabla `products` de la base de datos. Cada propiedad es una columna.
 *
 * Es distinta de [Product] a propósito: el modelo de dominio no debe conocer Room (`domain` es Kotlin puro),
 * así que la anotación `@Entity` vive solo en la capa de datos.
 *
 * @property deleted borrado lógico: el producto sigue en la tabla pero no se muestra. Así «Deshacer» solo
 * tiene que poner el valor a `false`, y el borrado sobrevive a cerrar la app.
 */
@Entity(tableName = "products")
data class ProductEntity(
    // Se usa el id del servidor: no hace falta que Room genere uno propio.
    @PrimaryKey val id: Int,
    val name: String,
    val category: String,
    val price: Double,
    val imageUrl: String,
    val favorite: Boolean,
    val deleted: Boolean,
)

/** Convierte la fila en el modelo de dominio que usa el resto de la app. */
fun ProductEntity.toDomain() = Product(
    id = id,
    name = name,
    category = category,
    price = price,
    imageUrl = imageUrl,
    favorite = favorite,
)
