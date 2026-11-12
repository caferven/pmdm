package com.example.pmdm

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import com.example.pmdm.di.AppContainer

/**
 * Clase [Application] de MiTienda (registrada en el manifest).
 * Android la crea antes que cualquier Activity, por eso es el sitio adecuado para el [AppContainer]
 * y para configurar el cargador de imágenes de toda la app.
 */
class MiTiendaApp : Application(), SingletonImageLoader.Factory {

    /** Dependencias compartidas por toda la app. */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }

    /**
     * Crea el único [ImageLoader] de la app (Coil lo pide la primera vez que se carga una imagen).
     *
     * Coil busca cada imagen por este orden y se queda con la primera que encuentra:
     * 1. **Caché de memoria**: bitmaps ya decodificados en RAM. Es instantánea, pero se vacía al cerrar
     *    la app o cuando el sistema necesita memoria.
     * 2. **Caché de disco**: los bytes descargados, guardados en archivos. Sobrevive entre ejecuciones y
     *    evita volver a descargar, aunque hay que decodificar de nuevo.
     * 3. **Red**: solo si no estaba en ninguna de las anteriores; el resultado se guarda en ambas cachés.
     *
     * Coil ya trae valores por defecto razonables; se declaran aquí para que se vean y se puedan cambiar.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    // Hasta el 25 % de la memoria disponible de la app: más cachea más, pero deja menos RAM libre.
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    // En cacheDir: el sistema puede borrarlo si falta espacio y no cuenta como datos del usuario.
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024) // 50 MB; al llenarse se descartan las imágenes más antiguas
                    .build()
            }
            .build()
}
