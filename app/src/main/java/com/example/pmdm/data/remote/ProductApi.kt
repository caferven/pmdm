package com.example.pmdm.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Los endpoints de la API como una interfaz Kotlin: Retrofit genera la implementación en ejecución.
 *
 * Al ser `suspend`, la llamada se hace en un hilo de fondo y no bloquea la interfaz. Si la petición falla,
 * la función lanza una excepción en lugar de devolver un error.
 */
interface ProductApi {

    /**
     * `GET products`: lista de productos.
     *
     * @param limit cuántos productos pedir; el servidor devuelve 30 por defecto.
     * @param select campos que se quieren recibir. Pedir solo lo necesario ahorra datos móviles.
     */
    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int = 15,
        @Query("select") select: String = "id,title,category,price,thumbnail",
    ): ProductsResponse
}
