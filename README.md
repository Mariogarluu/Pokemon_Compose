# 🎮 Pokémon Compose

Una aplicación Android moderna desarrollada con Jetpack Compose que muestra una lista de Pokémon con sus sprites y artwork oficial.

## 📱 Características

- **Lista de Pokémon**: Visualiza una colección de Pokémon clásicos con sus sprites
- **Detalle de Pokémon**: Toca cualquier Pokémon para ver su artwork en alta calidad
- **Navegación fluida**: Navegación intuitiva entre pantallas usando Jetpack Navigation
- **Diseño Material 3**: Interfaz moderna siguiendo las directrices de Material Design 3
- **Arquitectura MVVM**: Código limpio y mantenible con patrón Model-View-ViewModel

## 🎨 Pokémon Incluidos

La aplicación incluye los siguientes Pokémon:

1. **Bulbasaur** (#001) - Tipo Planta/Veneno
2. **Charmander** (#004) - Tipo Fuego
3. **Squirtle** (#007) - Tipo Agua
4. **Caterpie** (#010) - Tipo Bicho
5. **Pikachu** (#025) - Tipo Eléctrico
6. **Jigglypuff** (#039) - Tipo Normal/Hada
7. **Eevee** (#133) - Tipo Normal
8. **Snorlax** (#143) - Tipo Normal

## 🏗️ Arquitectura

El proyecto sigue una arquitectura limpia con separación de responsabilidades:

```
com.turingalan.pokemon/
├── data/
│   ├── model/
│   │   └── Pokemon.kt              # Modelo de datos
│   └── repository/
│       ├── PokemonRepository.kt     # Interfaz del repositorio
│       └── PokemonInMemoryRepository.kt  # Implementación en memoria
├── ui/
│   ├── list/
│   │   └── PokemonListScreen.kt    # Pantalla de lista
│   ├── detail/
│   │   └── PokemonDetailScreen.kt  # Pantalla de detalle
│   └── theme/
│       ├── Color.kt                # Colores del tema
│       ├── Theme.kt                # Tema de la app
│       └── Type.kt                 # Tipografía
├── PokemonViewModel.kt             # ViewModel principal
└── MainActivity.kt                 # Actividad principal

```

## 🛠️ Tecnologías Utilizadas

### Core
- **Kotlin** (2.2.20) - Lenguaje de programación principal
- **Android SDK** - API Level 34-36
- **Jetpack Compose** (2025.10.00) - UI Toolkit moderno

### Jetpack Libraries
- **Compose Material3** - Componentes de Material Design 3
- **Navigation Compose** (2.9.5) - Navegación entre pantallas
- **ViewModel Compose** (2.9.4) - Gestión del estado de la UI
- **Hilt** (2.57.2) - Inyección de dependencias (configurado)
- **Lifecycle Runtime KTX** (2.9.4) - Componentes conscientes del ciclo de vida

### Build Tools
- **Gradle** (8.12.3) - Sistema de construcción
- **KSP** (2.2.20-2.0.4) - Procesamiento de anotaciones para Kotlin

## 📋 Requisitos Previos

- **Android Studio** Hedgehog (2023.1.1) o superior
- **JDK** 21 o superior
- **Android SDK** con API Level 34 o superior
- **Gradle** 8.0 o superior

## 🚀 Instalación y Configuración

### 1. Clonar el Repositorio

```bash
git clone https://github.com/Mariogarluu/Pokemon_Compose.git
cd Pokemon_Compose
```

### 2. Abrir en Android Studio

1. Abre Android Studio
2. Selecciona `File > Open`
3. Navega hasta la carpeta del proyecto y ábrela
4. Espera a que Gradle sincronice las dependencias

### 3. Ejecutar la Aplicación

#### Usando un Dispositivo Físico:
1. Habilita las opciones de desarrollador en tu dispositivo Android
2. Conecta el dispositivo mediante USB
3. Haz clic en el botón `Run` (▶️) en Android Studio

#### Usando un Emulador:
1. Abre el AVD Manager en Android Studio
2. Crea un dispositivo virtual con API Level 34 o superior
3. Inicia el emulador
4. Haz clic en el botón `Run` (▶️) en Android Studio

## 📂 Estructura del Proyecto

### Modelo de Datos

```kotlin
data class Pokemon(
    val id: Long,
    val name: String,
    val spriteId: Int,     // ID del recurso drawable del sprite
    val artworkId: Int     // ID del recurso drawable del artwork
)
```

### Patrón Repository

El proyecto utiliza el patrón Repository para abstraer el acceso a datos:

- `PokemonRepository`: Interfaz que define las operaciones de datos
- `PokemonInMemoryRepository`: Implementación que almacena los Pokémon en memoria

### ViewModel

`PokemonViewModel` gestiona los datos de la UI y proporciona:
- `getAll()`: Obtiene la lista completa de Pokémon
- `getById(id)`: Obtiene un Pokémon específico por su ID

## 🧠 ViewModel y Gestión de Estado

### Arquitectura MVVM Detallada

Este proyecto implementa el patrón **MVVM (Model-View-ViewModel)** siguiendo las mejores prácticas de Android moderno:

#### 📊 Componentes del patrón

```
┌─────────────────────┐
│   View (UI Layer)   │  ← Composables que observan y muestran datos
│  PokemonListScreen  │
└──────────┬──────────┘
           │ observa StateFlow
           │ llama funciones
           ▼
┌─────────────────────┐
│      ViewModel      │  ← Gestiona estado y lógica de negocio
│  PokemonViewModel   │
└──────────┬──────────┘
           │ usa Repository
           ▼
┌─────────────────────┐
│   Model (Data)      │  ← Repositorios y modelos de datos
│  PokemonRepository  │
└─────────────────────┘
```

### 🔄 StateFlow y UI State

#### PokemonUiState (Sealed Class)

El estado de la UI se representa mediante una **sealed class** que garantiza type-safety y exhaustividad:

```kotlin
sealed class PokemonUiState {
    object Idle : PokemonUiState()                              // Estado inicial
    object Loading : PokemonUiState()                           // Cargando datos
    data class Success(val pokemons: List<Pokemon>) : PokemonUiState()  // Éxito con datos
    data class Error(val message: String) : PokemonUiState()    // Error con mensaje
}
```

**Ventajas:**
- ✅ **Type-safe**: El compilador conoce todos los estados posibles
- ✅ **Exhaustivo**: El `when` debe manejar todos los casos
- ✅ **Sin estados inconsistentes**: Solo puede estar en UN estado a la vez

#### StateFlow

**StateFlow** es un contenedor reactivo de estado que:
- Siempre tiene un valor actual
- Emite actualizaciones a todos los observadores
- Solo emite valores distintos (no duplicados)
- Es thread-safe

```kotlin
// En el ViewModel
private val _uiState = MutableStateFlow<PokemonUiState>(PokemonUiState.Idle)
val uiState: StateFlow<PokemonUiState> = _uiState  // Backing property pattern

// En la UI (Composable)
val uiState by viewModel.uiState.collectAsState()
```

### ⚡ viewModelScope y Corrutinas

El `PokemonViewModel` usa **corrutinas de Kotlin** para operaciones asíncronas:

```kotlin
fun loadPokemons() {
    viewModelScope.launch {  // Scope que se cancela automáticamente
        _uiState.value = PokemonUiState.Loading
        delay(1000)  // Simulación de operación de red
        try {
            val pokemons = repository.readAll()
            _uiState.value = PokemonUiState.Success(pokemons)
        } catch (e: Exception) {
            _uiState.value = PokemonUiState.Error("Error al cargar Pokémon")
        }
    }
}
```

**Características clave:**
- **viewModelScope**: Scope vinculado al ciclo de vida del ViewModel
- **Cancelación automática**: Se cancela cuando el ViewModel se destruye
- **try-catch**: Manejo robusto de errores

### 🔁 Flujo de datos completo

```
1. Usuario abre PokemonListScreen
   ↓
2. LaunchedEffect llama a viewModel.loadPokemons()
   ↓
3. Estado cambia: Idle → Loading
   ↓
4. UI se recompone, muestra CircularProgressIndicator
   ↓
5. ViewModel obtiene datos del Repository
   ↓
6. Estado cambia: Loading → Success(pokemons)
   ↓
7. UI se recompone, muestra lista de Pokémon
```

### 📱 Uso en Composables

```kotlin
@Composable
fun PokemonListScreen(viewModel: PokemonViewModel = viewModel()) {
    // Observa el estado como State de Compose
    val uiState by viewModel.uiState.collectAsState()
    
    // Carga inicial de datos
    LaunchedEffect(Unit) {
        viewModel.loadPokemons()
    }
    
    // Renderiza UI según el estado
    when (uiState) {
        is PokemonUiState.Idle -> EmptyScreen()
        is PokemonUiState.Loading -> LoadingScreen()
        is PokemonUiState.Success -> PokemonList(uiState.pokemons)
        is PokemonUiState.Error -> ErrorScreen(uiState.message)
    }
}
```

### 💡 Beneficios de esta arquitectura

1. **Supervivencia a cambios de configuración**: Los datos persisten al rotar la pantalla
2. **Separación de responsabilidades**: UI solo muestra, ViewModel gestiona
3. **Testabilidad**: Lógica de negocio testeable sin Android Framework
4. **Reactividad**: La UI se actualiza automáticamente cuando cambia el estado
5. **Manejo de errores**: Gestión centralizada y robusta de errores
6. **Reutilización**: El mismo ViewModel puede servir múltiples pantallas

### 📚 Recursos de aprendizaje

Para entender mejor el ViewModel y su implementación, consulta:
- **[VIEWMODEL_NOTES.md](VIEWMODEL_NOTES.md)** - Apuntes detallados sobre ViewModel en español
- Código comentado en `PokemonViewModel.kt`
- Código comentado en `PokemonUiState.kt`
- Código comentado en `PokemonListScreen.kt`

### Navegación

La aplicación utiliza Navigation Compose con dos rutas:
- `PokemonList`: Pantalla principal con la lista de Pokémon
- `Pokemon/{PokemonId}`: Pantalla de detalle con el artwork del Pokémon

## 🎯 Funcionalidades Principales

### Pantalla de Lista
- Muestra todos los Pokémon en una lista scrollable
- Cada tarjeta muestra el sprite y el nombre del Pokémon
- Al tocar una tarjeta, navega a la pantalla de detalle

### Pantalla de Detalle
- Muestra el artwork en alta calidad del Pokémon seleccionado
- Incluye el nombre del Pokémon en la barra superior
- Botón de retroceso para volver a la lista

## 🔄 Flujo de la Aplicación

```
┌─────────────────────┐
│  MainActivity       │
│  (Entry Point)      │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  PokemonListScreen  │
│  (Lista de Cards)   │
└──────────┬──────────┘
           │
           │ onClick(pokemonId)
           ▼
┌─────────────────────┐
│ PokemonDetailScreen │
│ (Artwork grande)    │
└─────────────────────┘
```

## 🧪 Testing

El proyecto incluye configuración para:
- **JUnit** (4.13.2) - Tests unitarios
- **Espresso** (3.7.0) - Tests de UI
- **Compose UI Test** - Tests específicos de Compose

Para ejecutar los tests:

```bash
./gradlew test           # Tests unitarios
./gradlew connectedAndroidTest  # Tests instrumentados
```

## 🔧 Configuración de Gradle

### Versión de SDK
- **Compile SDK**: 36
- **Min SDK**: 34
- **Target SDK**: 36

### Características de Build
- **Jetpack Compose**: Habilitado
- **Java Version**: 21
- **ProGuard**: Configurado para release builds

## 🤝 Contribuir

Las contribuciones son bienvenidas. Si deseas mejorar el proyecto:

1. Haz un fork del repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

### Guía de Estilo
- Sigue las convenciones de código de Kotlin
- Usa nombres descriptivos para variables y funciones
- Añade comentarios cuando sea necesario
- Asegúrate de que el código compile sin warnings

## 📝 Mejoras Futuras

- [ ] Integración con la API de Pokémon (PokeAPI)
- [ ] Implementación de Hilt para inyección de dependencias
- [ ] Base de datos local con Room
- [ ] Búsqueda y filtrado de Pokémon
- [ ] Información detallada (tipos, estadísticas, evoluciones)
- [ ] Modo oscuro
- [ ] Animaciones y transiciones
- [ ] Favoritos y colecciones personalizadas
- [ ] Soporte para más generaciones de Pokémon

## 📄 Licencia

Este proyecto es un proyecto educativo de código abierto. Los sprites y artwork de Pokémon son propiedad de Nintendo, Game Freak y The Pokémon Company.

## 👤 Autor

**Mario García**
- GitHub: [@Mariogarluu](https://github.com/Mariogarluu)

## 🙏 Agradecimientos

- Sprites y artwork obtenidos de recursos oficiales de Pokémon
- Jetpack Compose por hacer el desarrollo de UI más intuitivo
- La comunidad de Android por el soporte y recursos

---

⭐ Si te gusta este proyecto, ¡no olvides darle una estrella!

**Nota**: Esta aplicación es solo para fines educativos y demostración de tecnologías Android modernas.
