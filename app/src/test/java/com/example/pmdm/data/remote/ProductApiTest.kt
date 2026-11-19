package com.example.pmdm.data.remote

import com.example.pmdm.domain.model.RefreshError
import com.example.pmdm.domain.model.RefreshException
import java.net.SocketTimeoutException
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Prueba la capa de red completa (Retrofit + OkHttp + JSON) contra un **servidor falso** que corre en el propio
 * ordenador: no necesita internet y permite provocar cualquier respuesta, también las que fallan.
 */
class ProductApiTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() = server.close()

    private fun api(simulateError: () -> Boolean = { false }) =
        createProductApi(baseUrl = server.url("/").toString(), simulateError = simulateError)

    private fun respond(code: Int, body: String = "") {
        server.enqueue(MockResponse.Builder().code(code).body(body).build())
    }

    /** Llama a la API y devuelve la razón del fallo, tal y como la vería el resto de la app. */
    private suspend fun failureReason(api: ProductApi): RefreshError =
        try {
            api.getProducts()
            error("Expected the call to fail")
        } catch (e: Exception) {
            e.toRefreshException().reason
        }

    @Test
    fun getProducts_parsesJsonAndIgnoresUnknownFields() = runTest {
        respond(
            200,
            """{"products":[{"id":1,"title":"Lamp","category":"home","price":9.99,"thumbnail":"u","extra":true}],"total":1}""",
        )

        val products = api().getProducts().products

        assertEquals(listOf(ProductDto(1, "Lamp", "home", 9.99, "u")), products)
    }

    @Test
    fun getProducts_sendsLimitAndSelect() = runTest {
        respond(200, """{"products":[]}""")

        api().getProducts(limit = 5, select = "id")

        val request = server.takeRequest()
        assertEquals("/products?limit=5&select=id", request.url.encodedPath + "?" + request.url.encodedQuery)
    }

    @Test
    fun notFound_isClientError() = runTest {
        respond(404)
        assertEquals(RefreshError.CLIENT, failureReason(api()))
    }

    @Test
    fun serverFailure_isServerError() = runTest {
        respond(500)
        assertEquals(RefreshError.SERVER, failureReason(api()))
    }

    @Test
    fun malformedJson_isInvalidResponse() = runTest {
        respond(200, """{"products": "not a list"}""")
        assertEquals(RefreshError.INVALID_RESPONSE, failureReason(api()))
    }

    @Test
    fun unreachableServer_isNoConnection() = runTest {
        val api = api()
        server.close() // nadie escucha ya en esa dirección: el equivalente a no tener red

        assertEquals(RefreshError.NO_CONNECTION, failureReason(api))
    }

    @Test
    fun simulatedError_isNoConnectionAndNeverReachesTheServer() = runTest {
        assertEquals(RefreshError.NO_CONNECTION, failureReason(api(simulateError = { true })))
        assertEquals(0, server.requestCount)
    }

    @Test
    fun timeout_isTimeout() {
        assertEquals(RefreshError.TIMEOUT, SocketTimeoutException().toRefreshException().reason)
    }

    @Test
    fun unexpectedException_isUnknown() {
        assertEquals(RefreshError.UNKNOWN, IllegalStateException().toRefreshException().reason)
    }

    @Test
    fun refreshException_isKeptAsIs() {
        val original = RefreshException(RefreshError.SERVER)
        assertTrue(original === original.toRefreshException())
    }
}
