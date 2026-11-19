package com.example.pmdm.data.remote

import kotlinx.serialization.Serializable

/**
 * Producto tal y como lo entrega la API de dummyjson. Su forma la dicta el servidor, no la app.
 * Por eso es distinto de [com.example.pmdm.domain.model.Product]: si el servidor cambia un campo,
 * solo se toca esta clase y el mapeo del repositorio, y el resto de la app no se entera.
 *
 * `@Serializable` hace que el plugin de kotlinx.serialization genere, al compilar, el código que lee el JSON.
 * Cada propiedad debe llamarse igual que el campo del JSON.
 */
@Serializable
data class ProductDto(
    val id: Int,
    val title: String,
    val category: String,
    val price: Double,
    val thumbnail: String,
)

/**
 * Respuesta de `GET /products`: el servidor envuelve la lista en un objeto (con más campos, como `total`,
 * que no se necesitan y se ignoran).
 */
@Serializable
data class ProductsResponse(val products: List<ProductDto>)
