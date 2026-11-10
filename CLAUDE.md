# CLAUDE.md — MiTienda

## Contexto del proyecto

Proyecto **docente** para alumnos del módulo "Desarrollo de aplicaciones para dispositivos móviles".
La app se llama **MiTienda** (catálogo de productos) y se construye en **pasos incrementales**;
cada paso corresponde a un bloque del temario:

| Paso | Bloque del temario                  |
|------|-------------------------------------|
| 01   | Herramientas                        |
| 02   | Componentes / arquitectura          |
| 03   | Recursos                            |
| 04   | Interfaces y controles              |
| 05   | Imágenes y tema                     |
| 06   | Entrada y eventos                   |
| 07   | Preferencias                        |
| 08   | Persistencia                        |
| 09   | Cierre                              |

## Stack

- Kotlin, Jetpack Compose, Material 3.
- Version catalog: `gradle/libs.versions.toml` (todas las dependencias y plugins se declaran ahí).
- Arquitectura MVVM + Clean Architecture con tres capas: `ui` / `domain` / `data`.
- Coroutines + Flow.
- **Sin Hilt**: inyección manual mediante una clase `AppContainer` creada en la `Application`.
- Usa versiones estables actuales de las librerías; no uses APIs deprecadas.

## Principios de código

- **Claridad sobre sofisticación.** Código sencillo, sin abstracciones innecesarias.
- Comentarios y documentación en **español**.
- Los comentarios explican el **porqué**, no el qué.
- KDoc en todas las clases y funciones públicas.

## Flujo de trabajo

### Antes de cada paso
Muestra un **plan breve** y **espera la confirmación** del profesor antes de implementar.

### Al terminar cada paso
1. Compila con `./gradlew assembleDebug`, ejecuta `./gradlew lint` y los tests que existan.
   Si algo falla, arréglalo antes de seguir.
2. Crea `docs/NN-nombre.md` con:
   - Objetivo.
   - Conceptos del temario que cubre.
   - Archivos creados o modificados.
   - Explicación paso a paso con fragmentos de código.
   - Errores típicos de los alumnos.
   - 2-3 ejercicios propuestos.
3. Actualiza el índice de `docs/README.md`.
4. Haz commit con el mensaje `Paso NN: <título>` y crea el tag `paso-NN`.