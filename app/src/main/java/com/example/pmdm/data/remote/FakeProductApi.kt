package com.example.pmdm.data.remote

import java.io.IOException
import java.net.URLEncoder
import kotlinx.coroutines.delay

/**
 * API falsa de productos para poder desarrollar sin servidor.
 *
 * @property simulateError se consulta en cada llamada: si devuelve `true`, [getProducts] falla.
 * Es una función (y no un `Boolean`) para poder activarlo y desactivarlo en marcha desde el catálogo
 * de componentes y ver cómo reacciona la app ante un error de red.
 */
class FakeProductApi(private val simulateError: () -> Boolean = { false }) {

    /** Devuelve el catálogo tras una espera de 1,5 s que imita la latencia de la red. */
    suspend fun getProducts(): List<ProductDto> {
        delay(1500)
        if (simulateError()) throw IOException("Simulated network error")
        return catalog
    }

    private companion object {
        // Tripletas (nombre, categoría, precio); el `id` es la posición + 1.
        // Son datos del "servidor", no textos de interfaz, por eso no van en strings.xml.
        private val rawProducts = listOf(
            Triple("Desk lamp", "Home", 24.90),
            Triple("Ceramic mug", "Home", 8.50),
            Triple("Decorative cushion", "Home", 15.00),
            Triple("Wireless headphones", "Tech", 59.99),
            Triple("USB-C charger", "Tech", 19.95),
            Triple("Ergonomic mouse", "Tech", 32.00),
            Triple("Mechanical keyboard", "Tech", 79.90),
            Triple("Urban backpack", "Fashion", 44.00),
            Triple("Sports cap", "Fashion", 14.50),
            Triple("Running shoes", "Fashion", 69.00),
            Triple("Thermal bottle", "Sport", 17.90),
            Triple("Yoga mat", "Sport", 21.00),
            Triple("Football", "Sport", 25.50),
            Triple("Notebook", "Stationery", 5.90),
            Triple("Pen set", "Stationery", 9.75),
        )

        val catalog = rawProducts.mapIndexed { index, (title, section, amount) ->
            val id = index + 1
            val text = URLEncoder.encode(title, "UTF-8")
            ProductDto(id, title, section, amount, "https://placehold.co/400x300/png?text=$text")
        }
    }
}
