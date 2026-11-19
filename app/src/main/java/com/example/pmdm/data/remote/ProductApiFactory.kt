package com.example.pmdm.data.remote

import com.example.pmdm.BuildConfig
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Dirección base de la API. Es **HTTPS**: desde Android 9 (API 28) las conexiones HTTP sin cifrar están
 * bloqueadas por defecto, y en producción nunca debe usarse HTTP.
 */
const val BASE_URL = "https://dummyjson.com/"

/**
 * Construye la [ProductApi] montando la pila de red de la presentación, de abajo arriba:
 * OkHttp (conexión, timeouts, interceptores) → Retrofit (endpoints tipados) → conversor JSON.
 *
 * @param baseUrl dirección del servidor; se puede cambiar para apuntar a uno falso en los tests.
 * @param simulateError se consulta en cada petición: si devuelve `true`, falla como si no hubiera red.
 * Permite activar y desactivar el error en marcha desde el catálogo de componentes.
 */
fun createProductApi(baseUrl: String = BASE_URL, simulateError: () -> Boolean = { false }): ProductApi {
    val client = OkHttpClient.Builder()
        // Sin timeouts, una conexión colgada dejaría la pantalla cargando sin fin.
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        // Un interceptor ve cada petición antes de salir y cada respuesta antes de entregarla.
        .addInterceptor(SimulatedErrorInterceptor(simulateError))
        .apply {
            // Solo en el build debug. Nivel BASIC: apunta la URL y el código de respuesta, pero no el contenido.
            // El nivel BODY escribiría en Logcat todo lo que viaja, incluidos datos personales o tokens.
            if (BuildConfig.DEBUG_LOGS) {
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            }
        }
        .build()

    // ignoreUnknownKeys: si el servidor añade campos nuevos al JSON, la app no se rompe por ello.
    val json = Json { ignoreUnknownKeys = true }

    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ProductApi::class.java)
}

/** Interceptor de demostración: si [simulateError] devuelve `true`, la petición falla sin llegar a la red. */
private class SimulatedErrorInterceptor(private val simulateError: () -> Boolean) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (simulateError()) throw IOException("Simulated network error")
        return chain.proceed(chain.request())
    }
}
