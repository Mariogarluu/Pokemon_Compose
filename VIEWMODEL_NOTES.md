# 📚 Apuntes sobre ViewModel en Pokémon Compose

## 📖 Índice
1. [¿Qué es un ViewModel?](#qué-es-un-viewmodel)
2. [¿Por qué usar ViewModel?](#por-qué-usar-viewmodel)
3. [Arquitectura MVVM](#arquitectura-mvvm)
4. [Componentes clave en nuestra implementación](#componentes-clave)
5. [StateFlow y gestión de estados](#stateflow-y-gestión-de-estados)
6. [Ciclo de vida del ViewModel](#ciclo-de-vida-del-viewmodel)
7. [Corrutinas y viewModelScope](#corrutinas-y-viewmodelscope)
8. [Análisis del código](#análisis-del-código)
9. [Patrones de UI State](#patrones-de-ui-state)
10. [Buenas prácticas](#buenas-prácticas)

---

## 🎯 ¿Qué es un ViewModel?

Un **ViewModel** es una clase de Jetpack que está diseñada para almacenar y gestionar datos relacionados con la interfaz de usuario (UI) de manera consciente del ciclo de vida de Android.

### Características principales:
- **Sobrevive a cambios de configuración**: Cuando rotas la pantalla o cambias el idioma, el ViewModel mantiene los datos
- **Separa la lógica de negocio de la UI**: La UI solo muestra datos, el ViewModel los gestiona
- **Scope propio para corrutinas**: Tiene su propio alcance que se cancela automáticamente cuando ya no se necesita
- **No guarda referencias a Views o Contextos**: Evita memory leaks

---

## 🤔 ¿Por qué usar ViewModel?

### Sin ViewModel ❌
```kotlin
// En una Activity o Composable directamente
fun MyScreen() {
    var pokemons by remember { mutableStateOf(emptyList<Pokemon>()) }
    
    LaunchedEffect(Unit) {
        // Problema: Se pierde al rotar la pantalla
        pokemons = repository.readAll()
    }
}
```

### Con ViewModel ✅
```kotlin
// Los datos sobreviven a cambios de configuración
class PokemonViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<PokemonUiState>(PokemonUiState.Idle)
    val uiState: StateFlow<PokemonUiState> = _uiState
}
```

### Beneficios:
1. **Persistencia**: Los datos no se pierden al rotar la pantalla
2. **Separación de responsabilidades**: La UI solo observa, el ViewModel gestiona
3. **Testabilidad**: Puedes probar la lógica sin necesidad de Android
4. **Reutilización**: El mismo ViewModel puede servir para múltiples pantallas

---

## 🏗️ Arquitectura MVVM

**MVVM** = Model - View - ViewModel

```
┌─────────────────────────────────────────────────────────────┐
│                        📱 View (UI)                         │
│              (Composables: PokemonListScreen)               │
│                                                             │
│  • Observa el estado del ViewModel                         │
│  • Reacciona a cambios mostrando Loading/Success/Error     │
│  • Envía eventos al ViewModel (clicks, acciones)           │
└─────────────────────┬───────────────────────────────────────┘
                      │ observa
                      │ llama funciones
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    🧠 ViewModel                             │
│                  (PokemonViewModel)                         │
│                                                             │
│  • Gestiona el estado de la UI (StateFlow)                 │
│  • Ejecuta lógica de negocio (cargar Pokémon)             │
│  • Se comunica con el Repository                           │
│  • Transforma datos del Model para la View                 │
└─────────────────────┬───────────────────────────────────────┘
                      │ usa
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    💾 Model (Data)                          │
│           (Repository, Models, Data Sources)                │
│                                                             │
│  • PokemonRepository: Interfaz de acceso a datos           │
│  • PokemonInMemoryRepository: Implementación               │
│  • Pokemon: Modelo de datos                                │
└─────────────────────────────────────────────────────────────┘
```

### Flujo de datos en nuestra app:

1. **View** → **ViewModel**: El usuario abre la pantalla
2. **ViewModel** → **Model**: `loadPokemons()` pide datos al Repository
3. **Model** → **ViewModel**: El Repository devuelve la lista de Pokémon
4. **ViewModel** → **View**: El estado cambia a `Success(pokemons)`
5. **View**: La UI se recompone automáticamente mostrando la lista

---

## 🔑 Componentes clave

### 1. PokemonViewModel
```kotlin
class PokemonViewModel(
    private val repository: PokemonRepository = PokemonInMemoryRepository()
) : ViewModel() {
    // ...
}
```

- **Extiende de `ViewModel`**: Hereda funcionalidad de Jetpack
- **Inyección de dependencias**: Recibe el repository por constructor
- **Por defecto usa InMemoryRepository**: Si no se inyecta otro

### 2. PokemonUiState (Sealed Class)
```kotlin
sealed class PokemonUiState {
    object Idle : PokemonUiState()
    object Loading : PokemonUiState()
    data class Success(val pokemons: List<Pokemon>) : PokemonUiState()
    data class Error(val message: String) : PokemonUiState()
}
```

- **Sealed class**: Solo puede tener subclases definidas aquí
- **Exhaustividad**: El compilador verifica que manejes todos los casos
- **Type-safe**: Cada estado tiene los datos que necesita

### 3. StateFlow
```kotlin
private val _uiState = MutableStateFlow<PokemonUiState>(PokemonUiState.Idle)
val uiState: StateFlow<PokemonUiState> = _uiState
```

- **`_uiState` (privado)**: Mutable, solo el ViewModel puede modificarlo
- **`uiState` (público)**: Inmutable, la UI solo puede observarlo
- **Patrón backing property**: Encapsulación del estado

---

## 🌊 StateFlow y gestión de estados

### ¿Qué es StateFlow?

**StateFlow** es un tipo de **Flow** que:
- Siempre tiene un valor (estado actual)
- Emite el valor actual a nuevos colectores inmediatamente
- Solo emite valores distintos (no duplicados)
- Es thread-safe

### Comparación con LiveData

| Característica | StateFlow | LiveData |
|----------------|-----------|----------|
| Consciente del ciclo de vida | ❌ (necesitas collectAsState) | ✅ |
| Valor inicial requerido | ✅ | ❌ |
| Compatibilidad con Compose | ✅ Excelente | ⚠️ Necesita .observeAsState() |
| Operadores de transformación | ✅ Todos los de Flow | ⚠️ Limitados |
| Corrutinas | ✅ Nativo | ❌ Necesita liveData builder |

### Flujo de estados en nuestra app:

```
1. Inicio → Idle (Estado inicial)
          ↓
2. Usuario abre lista → Loading (Cargando datos)
          ↓
3a. Éxito → Success(lista de pokémon)
   O
3b. Error → Error("mensaje de error")
```

### En código:
```kotlin
// Estado inicial
private val _uiState = MutableStateFlow<PokemonUiState>(PokemonUiState.Idle)

// Transición a Loading
_uiState.value = PokemonUiState.Loading

// Transición a Success
_uiState.value = PokemonUiState.Success(pokemons)

// Transición a Error
_uiState.value = PokemonUiState.Error("Error al cargar")
```

---

## ⏰ Ciclo de vida del ViewModel

```
┌─────────────────────────────────────────────────────────┐
│                   Ciclo de Vida                         │
└─────────────────────────────────────────────────────────┘

1. Creación
   ↓
   Activity/Fragment creado
   ViewModel instanciado
   ↓
2. Uso activo
   ↓
   Rotación de pantalla ──→ Activity destruida y recreada
   │                         ViewModel SOBREVIVE ✅
   │                         Mantiene los datos
   ↓
3. Usuario navega hacia atrás
   ↓
   Activity destruida PERMANENTEMENTE
   ViewModel.onCleared() llamado
   viewModelScope cancelado
   ↓
4. Limpieza automática
   Libera recursos
```

### Ejemplo práctico:

```kotlin
class PokemonViewModel : ViewModel() {
    
    init {
        println("ViewModel creado")
    }
    
    override fun onCleared() {
        super.onCleared()
        println("ViewModel destruido - limpieza de recursos")
        // Las corrutinas en viewModelScope se cancelan automáticamente
    }
}
```

### Cuándo se destruye:
- ❌ NO al rotar la pantalla
- ❌ NO al cambiar idioma/tema
- ❌ NO al minimizar la app
- ✅ SÍ cuando navegas permanentemente hacia atrás
- ✅ SÍ cuando finalizas la Activity
- ✅ SÍ cuando el proceso es matado

---

## 🚀 Corrutinas y viewModelScope

### ¿Qué es viewModelScope?

**viewModelScope** es un CoroutineScope vinculado al ciclo de vida del ViewModel:
- Se crea cuando se crea el ViewModel
- Se cancela automáticamente cuando se destruye el ViewModel
- Usa Dispatchers.Main por defecto

### Ventajas:

1. **No memory leaks**: Se cancela automáticamente
2. **No código de limpieza manual**: Olvídate de `job.cancel()`
3. **Seguro para operaciones largas**: Si el usuario sale, se cancela

### En nuestro código:

```kotlin
fun loadPokemons() {
    viewModelScope.launch {  // ← Lanza corrutina en el scope del ViewModel
        _uiState.value = PokemonUiState.Loading
        delay(1000)  // Simulación de carga (suspending function)
        try {
            val pokemons = repository.readAll()  // Operación que podría fallar
            _uiState.value = PokemonUiState.Success(pokemons)
        } catch (e: Exception) {
            _uiState.value = PokemonUiState.Error("Error al cargar Pokémon")
        }
    }
}
```

### Desglose paso a paso:

1. **`viewModelScope.launch`**: Inicia una corrutina nueva
2. **`_uiState.value = Loading`**: Actualiza estado a "cargando"
3. **`delay(1000)`**: Espera 1 segundo (no bloquea el thread)
4. **`repository.readAll()`**: Obtiene los datos
5. **`_uiState.value = Success`**: Actualiza con éxito
6. **`catch`**: Si hay error, actualiza a estado de error

### ¿Por qué usar try-catch?

```kotlin
// Sin try-catch ❌
fun loadPokemons() {
    viewModelScope.launch {
        _uiState.value = PokemonUiState.Loading
        val pokemons = repository.readAll()  // Si falla, la app crasha
        _uiState.value = PokemonUiState.Success(pokemons)
    }
}

// Con try-catch ✅
fun loadPokemons() {
    viewModelScope.launch {
        _uiState.value = PokemonUiState.Loading
        try {
            val pokemons = repository.readAll()
            _uiState.value = PokemonUiState.Success(pokemons)
        } catch (e: Exception) {
            _uiState.value = PokemonUiState.Error("Error al cargar Pokémon")
            // La app continúa funcionando, muestra mensaje al usuario
        }
    }
}
```

---

## 🔍 Análisis del código

### PokemonViewModel.kt - Línea por línea

```kotlin
// 1. Importaciones necesarias
package com.turingalan.pokemon

import androidx.lifecycle.ViewModel  // Clase base del ViewModel
import androidx.lifecycle.viewModelScope  // Scope para corrutinas
import com.turingalan.pokemon.data.model.Pokemon  // Modelo de datos
import com.turingalan.pokemon.data.repository.PokemonInMemoryRepository
import com.turingalan.pokemon.data.repository.PokemonRepository
import kotlinx.coroutines.delay  // Para simular delay
import kotlinx.coroutines.flow.MutableStateFlow  // Estado mutable
import kotlinx.coroutines.flow.StateFlow  // Estado inmutable
import kotlinx.coroutines.launch  // Para lanzar corrutinas

// 2. Declaración de la clase
class PokemonViewModel(
    // Inyección de dependencias: recibe el repositorio por constructor
    // Valor por defecto: InMemoryRepository (útil para testing y desarrollo)
    private val repository: PokemonRepository = PokemonInMemoryRepository()
) : ViewModel() {  // Extiende de ViewModel de Jetpack

    // 3. Estado privado (mutable)
    // MutableStateFlow: Puede ser modificado solo dentro del ViewModel
    // Tipo: PokemonUiState - puede ser Idle, Loading, Success o Error
    // Valor inicial: Idle (esperando a que se carguen datos)
    private val _uiState = MutableStateFlow<PokemonUiState>(PokemonUiState.Idle)
    
    // 4. Estado público (inmutable)
    // StateFlow: Solo lectura, la UI lo observa pero no lo modifica
    // Backing property pattern: expone _uiState de forma inmutable
    val uiState: StateFlow<PokemonUiState> = _uiState

    // 5. Función para cargar la lista de Pokémon
    fun loadPokemons() {
        // Lanza una corrutina en el scope del ViewModel
        // Se cancelará automáticamente si el ViewModel se destruye
        viewModelScope.launch {
            // Paso 1: Indicar que estamos cargando
            _uiState.value = PokemonUiState.Loading
            
            // Paso 2: Simular delay (en app real sería una llamada a API)
            delay(1000)  // 1 segundo de espera
            
            // Paso 3: Intentar cargar los datos
            try {
                // Llamada al repositorio para obtener todos los Pokémon
                val pokemons = repository.readAll()
                
                // Si tiene éxito, actualizar estado a Success con los datos
                _uiState.value = PokemonUiState.Success(pokemons)
            } catch (e: Exception) {
                // Si hay algún error, actualizar estado a Error con mensaje
                _uiState.value = PokemonUiState.Error("Error al cargar Pokémon")
            }
        }
    }

    // 6. Función para obtener un Pokémon por ID
    // Esta función es síncrona (no usa corrutinas)
    // Devuelve el Pokémon si existe, o null si no se encuentra
    fun getById(id: Long): Pokemon? = repository.readOne(id)
}
```

### PokemonUiState.kt - Explicación completa

```kotlin
package com.turingalan.pokemon

import com.turingalan.pokemon.data.model.Pokemon

// Sealed class: Solo puede tener subclases definidas aquí
// Útil para representar estados mutuamente excluyentes
sealed class PokemonUiState {
    
    // Object: Singleton, solo una instancia
    // Idle: Estado inicial, esperando acción del usuario
    object Idle : PokemonUiState()
    
    // Loading: Cargando datos, mostrar indicador de progreso
    object Loading : PokemonUiState()
    
    // Data class: Puede tener múltiples instancias con diferentes datos
    // Success: Carga exitosa, contiene la lista de Pokémon
    data class Success(val pokemons: List<Pokemon>) : PokemonUiState()
    
    // Error: Falló la carga, contiene mensaje de error
    data class Error(val message: String) : PokemonUiState()
}
```

### ¿Por qué sealed class?

```kotlin
// Con sealed class ✅
when (uiState) {
    is PokemonUiState.Idle -> EmptyScreen()
    is PokemonUiState.Loading -> LoadingScreen()
    is PokemonUiState.Success -> PokemonList(uiState.pokemons)
    is PokemonUiState.Error -> ErrorScreen(uiState.message)
    // El compilador verifica que cubras todos los casos
    // No necesitas "else" branch
}

// Sin sealed class ❌
when (uiState) {
    is PokemonUiState.Idle -> EmptyScreen()
    is PokemonUiState.Loading -> LoadingScreen()
    // Olvidas Success y Error - el compilador NO te avisa
}
```

### Uso en PokemonListScreen.kt

```kotlin
@Composable
fun PokemonListScreen(
    navController: NavController,
    viewModel: PokemonViewModel = viewModel()  // Obtiene o crea ViewModel
) {
    // Observa el estado como State de Compose
    // Cada vez que _uiState cambia, esta variable se actualiza
    // y el Composable se recompone automáticamente
    val uiState by viewModel.uiState.collectAsState()

    // LaunchedEffect: Se ejecuta solo una vez cuando el composable entra en composición
    // Unit como key: solo se ejecuta la primera vez
    LaunchedEffect(Unit) {
        viewModel.loadPokemons()  // Dispara la carga de datos
    }

    // Pattern matching con when para renderizar según el estado
    when (uiState) {
        is PokemonUiState.Idle -> EmptyScreen()
        is PokemonUiState.Loading -> LoadingScreen()
        is PokemonUiState.Success -> {
            val pokemons = (uiState as PokemonUiState.Success).pokemons
            PokemonList(pokemons = pokemons) { pokemon ->
                navController.navigate("Pokemon/${pokemon.id}")
            }
        }
        is PokemonUiState.Error -> ErrorScreen((uiState as PokemonUiState.Error).message)
    }
}
```

---

## 🎨 Patrones de UI State

### 1. Single State Pattern (Nuestro enfoque)

```kotlin
sealed class PokemonUiState {
    object Idle : PokemonUiState()
    object Loading : PokemonUiState()
    data class Success(val pokemons: List<Pokemon>) : PokemonUiState()
    data class Error(val message: String) : PokemonUiState()
}

// Un solo StateFlow que representa TODO el estado de la UI
val uiState: StateFlow<PokemonUiState>
```

**Ventajas:**
- ✅ Estado siempre consistente (no puedes estar en Loading y Error a la vez)
- ✅ Fácil de testear
- ✅ Claro y predecible

**Desventajas:**
- ⚠️ Para UIs complejas puede volverse grande

### 2. Multiple State Pattern (Alternativa)

```kotlin
class PokemonViewModel : ViewModel() {
    val pokemons: StateFlow<List<Pokemon>>
    val isLoading: StateFlow<Boolean>
    val error: StateFlow<String?>
}
```

**Ventajas:**
- ✅ Cada parte del estado es independiente

**Desventajas:**
- ❌ Pueden existir estados inconsistentes (loading=true y error!=null)
- ❌ Más difícil de testear

### 3. UI State con eventos

```kotlin
sealed class PokemonUiState {
    object Idle : PokemonUiState()
    object Loading : PokemonUiState()
    data class Success(val pokemons: List<Pokemon>) : PokemonUiState()
    data class Error(val message: String) : PokemonUiState()
}

sealed class PokemonEvent {
    data class ShowToast(val message: String) : PokemonEvent()
    object NavigateBack : PokemonEvent()
}

class PokemonViewModel : ViewModel() {
    val uiState: StateFlow<PokemonUiState>
    val events: Flow<PokemonEvent>  // Eventos one-shot
}
```

---

## ✨ Buenas prácticas

### 1. Naming conventions

```kotlin
// ❌ Mal
class PokemonVM : ViewModel()
val state: StateFlow<State>

// ✅ Bien
class PokemonViewModel : ViewModel()
val uiState: StateFlow<PokemonUiState>
```

### 2. Backing property para StateFlow

```kotlin
// ❌ Mal - Expone estado mutable
class PokemonViewModel : ViewModel() {
    val uiState = MutableStateFlow<PokemonUiState>(PokemonUiState.Idle)
    // La UI podría modificarlo: uiState.value = ...
}

// ✅ Bien - Solo el ViewModel puede modificar
class PokemonViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<PokemonUiState>(PokemonUiState.Idle)
    val uiState: StateFlow<PokemonUiState> = _uiState
}
```

### 3. No referencias a Context o View

```kotlin
// ❌ Mal - Memory leak
class PokemonViewModel(
    private val context: Context  // Se mantiene referencia a Activity
) : ViewModel()

// ✅ Bien - Solo datos y lógica
class PokemonViewModel(
    private val repository: PokemonRepository  // Solo interfaces
) : ViewModel()
```

### 4. Manejo de errores

```kotlin
// ❌ Mal - App crasha
fun loadPokemons() {
    viewModelScope.launch {
        _uiState.value = PokemonUiState.Loading
        val pokemons = repository.readAll()  // Puede fallar
        _uiState.value = PokemonUiState.Success(pokemons)
    }
}

// ✅ Bien - Maneja errores gracefully
fun loadPokemons() {
    viewModelScope.launch {
        _uiState.value = PokemonUiState.Loading
        try {
            val pokemons = repository.readAll()
            _uiState.value = PokemonUiState.Success(pokemons)
        } catch (e: Exception) {
            _uiState.value = PokemonUiState.Error("Error al cargar Pokémon")
        }
    }
}
```

### 5. Inyección de dependencias

```kotlin
// ❌ Mal - Difícil de testear
class PokemonViewModel : ViewModel() {
    private val repository = PokemonInMemoryRepository()  // Hardcoded
}

// ✅ Bien - Fácil de testear y mockear
class PokemonViewModel(
    private val repository: PokemonRepository = PokemonInMemoryRepository()
) : ViewModel()

// En tests:
val testViewModel = PokemonViewModel(FakePokemonRepository())
```

### 6. Estado inicial apropiado

```kotlin
// ❌ Mal - Null como estado inicial
private val _uiState = MutableStateFlow<PokemonUiState?>(null)

// ✅ Bien - Estado inicial explícito
private val _uiState = MutableStateFlow<PokemonUiState>(PokemonUiState.Idle)
```

### 7. Uso correcto de viewModelScope

```kotlin
// ❌ Mal - Scope global, no se cancela
fun loadPokemons() {
    GlobalScope.launch {  // Continúa después de destruir ViewModel
        _uiState.value = PokemonUiState.Loading
        val pokemons = repository.readAll()
        _uiState.value = PokemonUiState.Success(pokemons)
    }
}

// ✅ Bien - viewModelScope se cancela automáticamente
fun loadPokemons() {
    viewModelScope.launch {  // Se cancela con el ViewModel
        _uiState.value = PokemonUiState.Loading
        val pokemons = repository.readAll()
        _uiState.value = PokemonUiState.Success(pokemons)
    }
}
```

### 8. Funciones públicas solo para la UI

```kotlin
// ❌ Mal - Lógica interna pública
class PokemonViewModel : ViewModel() {
    fun updateUiState(state: PokemonUiState) {  // UI podría llamarlo
        _uiState.value = state
    }
}

// ✅ Bien - Solo acciones de usuario públicas
class PokemonViewModel : ViewModel() {
    fun loadPokemons() { /* ... */ }  // Acción del usuario
    fun retry() { loadPokemons() }     // Acción del usuario
    
    private fun updateUiState(state: PokemonUiState) {  // Internal
        _uiState.value = state
    }
}
```

---

## 🎓 Conceptos avanzados

### 1. StateFlow vs SharedFlow vs Flow

```kotlin
// StateFlow: Siempre tiene valor, emite último valor a nuevos colectores
val stateFlow = MutableStateFlow(0)
stateFlow.value = 1  // Actualizar
stateFlow.value      // Leer valor actual

// SharedFlow: Puede no tener valor inicial, eventos one-shot
val sharedFlow = MutableSharedFlow<Event>()
sharedFlow.emit(Event.ShowToast)  // Solo suspending

// Flow: Cold stream, se crea nuevo stream para cada colector
val flow = flow {
    emit(1)
    emit(2)
}
```

### 2. Transformaciones de Flow

```kotlin
class PokemonViewModel : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    
    val filteredPokemons: StateFlow<List<Pokemon>> = _searchQuery
        .debounce(300)  // Espera 300ms después de último cambio
        .map { query ->  // Transforma el query a lista filtrada
            repository.search(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
```

### 3. Combining multiple flows

```kotlin
class PokemonViewModel : ViewModel() {
    private val _pokemons = MutableStateFlow<List<Pokemon>>(emptyList())
    private val _filter = MutableStateFlow("")
    
    val uiState: StateFlow<PokemonUiState> = combine(
        _pokemons,
        _filter
    ) { pokemons, filter ->
        if (filter.isEmpty()) {
            PokemonUiState.Success(pokemons)
        } else {
            val filtered = pokemons.filter { it.name.contains(filter, ignoreCase = true) }
            PokemonUiState.Success(filtered)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PokemonUiState.Idle
    )
}
```

---

## 📊 Diagrama de flujo completo

```
Usuario abre PokemonListScreen
         │
         ▼
┌────────────────────┐
│ Composable entra   │
│ en composición     │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│ LaunchedEffect(Unit)│ ← Se ejecuta una sola vez
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│ viewModel.         │
│ loadPokemons()     │
└──────┬─────────────┘
       │
       ▼
┌────────────────────────────────────────┐
│ viewModelScope.launch {                │
│   _uiState.value = Loading             │ ← Estado cambia a Loading
│   delay(1000)                          │
│   try {                                │
│     val pokemons = repository.readAll()│ ← Obtiene datos
│     _uiState.value = Success(pokemons) │ ← Estado cambia a Success
│   } catch {                            │
│     _uiState.value = Error(msg)        │ ← O Error si falla
│   }                                    │
│ }                                      │
└──────┬─────────────────────────────────┘
       │
       │ StateFlow emite nuevo valor
       │
       ▼
┌────────────────────┐
│ val uiState by     │
│ viewModel.uiState  │ ← Observa cambios
│ .collectAsState()  │
└──────┬─────────────┘
       │
       │ Estado cambió
       │
       ▼
┌────────────────────┐
│ Composable se      │ ← Recomposición automática
│ recompone          │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│ when (uiState) {   │
│   Loading -> ...   │ ← Muestra spinner
│   Success -> ...   │ ← Muestra lista
│   Error -> ...     │ ← Muestra error
│ }                  │
└────────────────────┘
```

---

## 🧪 Testing del ViewModel

```kotlin
class PokemonViewModelTest {
    
    private lateinit var viewModel: PokemonViewModel
    private lateinit var repository: FakePokemonRepository
    
    @Before
    fun setup() {
        repository = FakePokemonRepository()
        viewModel = PokemonViewModel(repository)
    }
    
    @Test
    fun `loadPokemons updates state to Loading then Success`() = runTest {
        // Given
        val expectedPokemons = listOf(
            Pokemon(1, "Pikachu", R.drawable.pikachu, R.drawable.pikachu_artwork)
        )
        repository.setPokemons(expectedPokemons)
        
        // When
        viewModel.loadPokemons()
        
        // Then
        assertEquals(PokemonUiState.Loading, viewModel.uiState.value)
        
        advanceTimeBy(1000)  // Simula el delay
        
        val finalState = viewModel.uiState.value
        assertTrue(finalState is PokemonUiState.Success)
        assertEquals(expectedPokemons, (finalState as PokemonUiState.Success).pokemons)
    }
    
    @Test
    fun `loadPokemons handles error correctly`() = runTest {
        // Given
        repository.setShouldThrowError(true)
        
        // When
        viewModel.loadPokemons()
        advanceTimeBy(1000)
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state is PokemonUiState.Error)
        assertEquals("Error al cargar Pokémon", (state as PokemonUiState.Error).message)
    }
}
```

---

## 📚 Recursos adicionales

### Documentación oficial
- [ViewModel Overview](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [StateFlow and SharedFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Jetpack Compose State](https://developer.android.com/jetpack/compose/state)

### Mejores prácticas
- [Guide to app architecture](https://developer.android.com/topic/architecture)
- [UI State production](https://developer.android.com/topic/architecture/ui-layer#state-production)

---

## 💡 Resumen ejecutivo

### ViewModel en 5 puntos:

1. **Qué es**: Clase que gestiona datos de UI y sobrevive a cambios de configuración
2. **Por qué**: Separa lógica de UI, previene memory leaks, facilita testing
3. **StateFlow**: Flujo reactivo de estados (Idle → Loading → Success/Error)
4. **viewModelScope**: Scope de corrutinas que se cancela automáticamente
5. **MVVM**: Patrón arquitectónico que separa Model, View y ViewModel

### Flujo típico:

```
View (Composable)
  ↓ observa
ViewModel (StateFlow)
  ↓ usa
Repository (Interfaz)
  ↓ implementa
DataSource (Memoria/BD/API)
```

### Código mínimo necesario:

```kotlin
// ViewModel
class MyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(InitialState)
    val uiState: StateFlow<State> = _uiState
    
    fun doSomething() {
        viewModelScope.launch {
            _uiState.value = NewState
        }
    }
}

// En Composable
val viewModel: MyViewModel = viewModel()
val uiState by viewModel.uiState.collectAsState()

when (uiState) {
    State1 -> ShowState1()
    State2 -> ShowState2()
}
```

---

**¡Feliz codificación! 🎮✨**
