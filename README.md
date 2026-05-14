# The Last Outpost 🏰 🎮

**The Last Outpost** es un proyecto piloto de un juego para Android desarrollado de forma nativa utilizando **Kotlin**. Adéntrate en un mundo medieval, defiende tu castillo, gestiona tus habilidades y desbloquea logros mientras sobrevives a las oleadas enemigas.

> ⚠️ **Nota:** Este es un proyecto piloto/demostración.

## 🌟 Características Principales

*   **Defensa y Supervivencia:** Resiste contra enemigos como Goblins, Esqueletos y más.
*   **Gestión de Habilidades:** Árbol de habilidades para mejorar tu personaje y fortalecer tus defensas.
*   **Sistema de Logros:** Desbloquea retos a medida que avanzas en el juego.
*   **Múltiples Ranuras de Guardado:** Guarda tu progreso sin perder tu avance anterior.
*   **Estadísticas del Jugador:** Mantén un seguimiento de tu desempeño.
*   **Integración con Firebase:** Respaldado por Firebase para almacenamiento / base de datos (según implementación).

## 🛠️ Tecnologías Utilizadas

*   **Lenguaje:** [Kotlin](https://kotlinlang.org/)
*   **Plataforma:** Android SDK
*   **Arquitectura / Herramientas:**
    *   Room (Base de datos local)
    *   Arquitectura Nav Graph (Jetpack Navigation)
    *   Firebase (Base de datos remota / Auth)
    *   Gradle (Kotlin DSL)

## 🚀 Instalación y Configuración

Sigue estos pasos para compilar y probar el juego en tu dispositivo local o emulador:

1. **Clona el repositorio:**
   ```bash
   git clone https://github.com/TU_USUARIO/TheLastOutpost.git
   cd TheLastOutpost-main
   ```

2. **Abre el proyecto en Android Studio:**
   * Abre Android Studio.
   * Selecciona `File > Open...` y busca la carpeta del proyecto.

3. **Configura Firebase (IMPORTANTE):**
   * El archivo `google-services.json` que conecta la app con Firebase **no está incluido** por motivos de seguridad. 
   * Deberás crear un proyecto en la [Consola de Firebase](https://console.firebase.google.com/), registrar tu app de Android y descargar tu propio `google-services.json`.
   * Coloca el archivo descargado en la ruta: `app/google-services.json`.

4. **Compila y Ejecuta:**
   * Conecta tu dispositivo Android mediante depuración USB o usa un emulador.
   * Presiona el botón **Run** (Play) en Android Studio o usa el wrapper de gradle:
     ```bash
     ./gradlew installDebug
     ```

## 🔒 Seguridad

Se han aplicado buenas prácticas omitiendo exponer claves API sensibles directamente en repositorios públicos. Asegúrate de nunca hacer un `git commit` de tu archivo `google-services.json` u otros tokens de servicios privados. (El repositorio está configurado para ignorarlo automáticamente).

## 📄 Créditos y Licencias

*   **Sprites y Assets:** Incluye recursos visuales gratuitos y de dominio público/licenciados para uso en juegos. (EVil Wizard 2, Goblin, Skeleton). Por favor revisa las licencias dentro de la carpeta `app/raw_assets/` (e.g. `License.txt`).
---