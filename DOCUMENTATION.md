# Documentación de Arquitectura de The Last Outpost

Este documento describe la estructura del código, los componentes principales y la función de cada archivo en el juego **The Last Outpost**. 

El proyecto sigue una arquitectura basada en **MVVM (Model-View-ViewModel)** recomendada por Google, utilizando **Single Activity** con múltiples fragmentos gestionados por el componente de navegación (Navigation Component).

---

## 📁 1. Capa de Interfaz de Usuario (`ui/`)

Esta capa contiene todos los componentes visuales con los que interactúa el jugador (Pantallas, Vistas personalizadas y Adaptadores).

* **`MainActivity.kt`**: La actividad principal y única de la aplicación. Actúa como el contenedor base donde se infla el grafo de navegación (`nav_graph.xml`).
* **`WelcomeFragment.kt`**: Pantalla inicial del juego. Gestiona la lista de partidas guardadas (ranuras) y da la opción de crear una nueva aventura.
* **`NameEntryFragment.kt`**: Pantalla simple para que un jugador primerizo introduzca su nombre antes de pasar al menú principal.
* **`MenuFragment.kt`**: Menú principal de la partida actual. Contiene los botones de "Jugar", "Ajustes", "Logros" y "Estadísticas".
* **`GameFragment.kt`**: Es el núcleo del juego. Contiene la interfaz de juego (UI superpuesta, botones de pausa/tienda) y coordina las actualizaciones provenientes del `GameViewModel`.
* **`EnvironmentDecorationsView.kt`**: Una vista personalizada (Custom View) que hereda de `View`. Actúa como el *Lienzo (Canvas)* 2D donde se dibuja el escenario medieval, los enemigos, la torre y las animaciones frame a frame generadas en el bucle del juego.
* **`SpriteAnimator.kt`**: Clase de utilidad usada para gestionar las animaciones en el `EnvironmentDecorationsView` (ej. gestionar los fotogramas del personaje caminando o atacando).
* **`SkillsFragment.kt`**: Pantalla donde el jugador gasta oro o puntos para mejorar sus habilidades (Salud, Daño, Velocidad).
* **`AchievementsFragment.kt`**: Pantalla que visualiza la lista de logros desbloqueados y bloqueados. Funciona con un `RecyclerView`.
* **`AchievementsAdapter.kt`**: El adaptador necesario para inflar cada fila de logro de forma eficiente en el `AchievementsFragment`.
* **`StatisticsFragment.kt`**: Muestra las métricas pasadas del usuario, como enemigos totales derrotados o mayores oleadas alcanzadas (estadísticas almacenadas en el repositorio).
* **`SettingsFragment.kt`**: Pantalla de opciones, donde se controla el sonido, los controles u otros ajustes técnicos del juego.
* **`CreditsFragment.kt`**: Muestra los créditos (desarrollo, arte, etc.).
* **`SaveSlotAdapter.kt`**: Adaptador para mostrar horizontalmente las partidas guardadas en el `WelcomeFragment`.

---

## 📁 2. Capa de Lógica y Estado (`viewmodel/`)

Los **ViewModels** separan la lógica y el estado de la UI de forma que los datos sobrevivan a la rotación de pantalla o ciclo de vida. Utilizan `LiveData` y variables de estado observables.

* **`GameViewModel.kt`**: Es el cerebro de la partida. Ejecuta el bucle principal de juego (game loop), maneja la lógica de físicas, spawn de oleadas de enemigos, vida, oro, proyectiles, actualizaciones de colisión y el ciclo día/noche. 
* **`MenuViewModel.kt`**: Contiene el estado puro y simple de la pantalla de menú (como el nombre del jugador cargado).
* **`SettingsViewModel.kt`**: Maneja el estado en directo de los ajustes antes de persistirlos en las preferencias.
* **`SkillsViewModel.kt`**: Gestiona la lógica de compra de mejoras. Agrupa las habilidades por categorías (Daño, Velocidad, Vida), comprueba si el usuario tiene suficiente oro y desbloquea el nuevo nodo, actualizando el repositorio.

---

## 📁 3. Capa de Acceso a Datos (`repository/`)

Esta capa encapsula el origen de los datos. Provee una API limpia al resto de la app para que interactúen con la persistencia, abstrayéndolos de cómo se guardan.

* **`GameRepository.kt`**: En este proyecto, utiliza `SharedPreferences` y la librería `Gson` para guardar rápida y estructuradamente el progreso general y los ajustes. Se encarga de:
  - Guardar, crear y eliminar "Slots" (Partidas guardadas).
  - Almacenar el estado de las habilidades.
  - Guardar y leer los registros globales de la cuenta (Máximo oro, estadísticas acumuladas).
  - Gestionar la lista de logros (`Achievement`) globales.
  - Guardar el estado intermedio de la oleada (`saveGameProgress`).

---

## 📁 4. Capa de Modelos (`model/`)

Contiene los "Data Classes" de Kotlin, que son pura representación de los datos u objetos físicos del juego, sin lógica de negocio compleja integrada y diseñados para facilitar la serialización a JSON.

* **`GameModels.kt`**: Alberga diversas estructuras de datos que requiere la lógica del juego. Aquí se declaran entidades como `Enemy`, `Projectile`, propiedades numéricas base, etc.
* **`Achievement.kt`**: Datos de un Logro de la cuenta (ID, nombre, descripción, fecha si está desbloqueado).
* **`SaveSlot.kt`**: Entidad que describe una partida guardada (ID de partida, Nombre de usuario insertado y tiempo de juego / nivel actual).
* **`Skill.kt`**: Entidad de la habilidad, define lo que cuesta una mejora, a qué rama pertenece y si está actualmente desbloqueada en la cuenta o ranura.

---

## 📁 5. Recursos XML Principales (`res/`)

Los archivos estáticos de metadatos, interfaz y gráficos sobre los que se apoya la capa visual.

* **`navigation/nav_graph.xml`**: El mapa de carreteras de toda tu app. Define quién va a donde. Describe que el inicio es `NameEntryFragment` -> `WelcomeFragment` -> `MenuFragment` -> ... y define cómo se mueven las pantallas y qué argumentos (ej. el nombre `playerName`) viajan entre ellas.
* **`layout/`**: Define de forma visual con etiquetas XML dónde se posicionan los botones, fondos, y textos (DataBinding habilitado). Incluye `fragment_game.xml`, `fragment_menu.xml`, etc.
* **`drawable/`** y **`mipmap/`**: Carpeta de assets, conteniendo los fondos pre-renderizados e interfaces, y los íconos de la app respectivamente. `mipmap` se usó como target del App Icon (`ic_launcher`).
* **`AndroidManifest.xml`**: La "identidad" legal y obligatoria de la app en Android; define qué clase es la actividad principal, el icono y las orientaciones permitidas (como forzar `screenOrientation="sensorLandscape"` para que solo se juegue en horizontal).

---

> **📝 Nota sobre Limpieza:** Elementos que ya no se usan en el desarrollo, como la clase obsoleta de gráficos `SnowView.kt`, así como los fondos visuales de prueba `imagen*.png` ya han sido desvinculados de la compilación para mantener el APK con peso ligero.