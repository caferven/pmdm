package com.example.pmdm.domain.model

/**
 * Producto del catálogo de MiTienda.
 *
 * Es el modelo que entiende toda la app. Es inmutable: para "modificar" un producto se crea
 * una copia con [copy].
 *
 * @property id identificador único.
 * @property name nombre mostrado al usuario.
 * @property category categoría a la que pertenece (por ejemplo "Home").
 * @property price precio en euros.
 * @property imageUrl dirección de la imagen del producto.
 * @property favorite `true` si el usuario lo ha marcado como favorito.
 */
data class Product(
    val id: Int,
    val name: String,
    val category: String,
    val price: Double,
    val imageUrl: String,
    val favorite: Boolean = false,
)
