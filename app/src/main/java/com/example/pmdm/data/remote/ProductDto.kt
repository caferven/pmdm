package com.example.pmdm.data.remote

import com.example.pmdm.domain.model.Product

/**
 * Producto tal y como lo entrega la "API". Su forma la dicta el servidor, no la app.
 * Por eso es distinto de [Product]: si el servidor cambia un campo, solo se toca esta clase
 * y [toDomain], y el resto de la app no se entera.
 */
data class ProductDto(
    val id: Int,
    val title: String,
    val section: String,
    val amount: Double,
    val image: String,
)

/**
 * Convierte el DTO en el modelo de dominio.
 * El DTO no trae información de favoritos, así que arranca en `false`.
 */
fun ProductDto.toDomain() = Product(
    id = id,
    name = title,
    category = section,
    price = amount,
    imageUrl = image,
    favorite = false,
)
