package com.example.pmdm.ui.navigation

/**
 * Rutas de navegación de la app. Una ruta es un texto que identifica cada destino,
 * como la URL de una página web. Se centralizan aquí para no repetir.
 */
object Routes {
    /** Nombre del argumento que viaja en la ruta del detalle. */
    const val PRODUCT_ID_ARG = "id"

    /** Lista de productos (destino inicial). */
    const val PRODUCTS = "products"

    /** Detalle de un producto. `{id}` es un hueco que se rellena al navegar. */
    const val PRODUCT_DETAIL = "product/{$PRODUCT_ID_ARG}"

    /** Catálogo de componentes de Material 3. */
    const val CATALOG = "catalog"

    /** Construye la ruta concreta del detalle: `productDetail(3)` → `"product/3"`. */
    fun productDetail(id: Int) = "product/$id"
}
