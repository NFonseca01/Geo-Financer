package com.atlas.geofinancer.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.atlas.geofinancer.data.database.AppDatabase
import com.atlas.geofinancer.data.repository.FinancialRepository
import com.atlas.geofinancer.data.database.ExpenseEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * APRENDIZAJE - ¿Por qué AndroidViewModel?
 * Heredamos de 'AndroidViewModel' en lugar de 'ViewModel' a secas porque la base de datos Room
 * instalada en el dispositivo necesita acceder al "Contexto" (Application) del sistema operativo 
 * para saber en qué parte del disco duro físico del teléfono va a guardar el archivo SQLite.
 */
class FinancialViewModel(application: Application) : AndroidViewModel(application) {

    // 1. CONEXIÓN DE LA ARQUITECTURA
    // Instanciamos de forma única la base de datos local y el repositorio intermedio.
    private val database = AppDatabase.getDatabase(application)
    private val repository = FinancialRepository(database.financialDao())

    // 2. CONVERTIR FLUJOS EN ESTADOS DE INTERFAZ (STATEFLOW)
    // 'repository.allExpenses' es un flujo asíncrono continuo (Flow). Para que Jetpack Compose 
    // pueda leerlo sin consumir CPU de más, lo transformamos en un StateFlow mediante '.stateIn()'.
    // Esto mantendrá siempre la lista de gastos optimizada en la memoria RAM para la pantalla.
    val listaGastos: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(
            scope = viewModelScope, // Atado al ciclo de vida de este ViewModel (evita fugas de memoria)
            started = SharingStarted.WhileSubscribed(5000), // Si el usuario minimiza la app, pausa la lectura tras 5 segundos
            initialValue = emptyList() // El contenedor por defecto mientras lee el almacenamiento físico
        )

    /**
     * Función que se dispara al pulsar "Confirmar y Guardar" en la interfaz.
     * Al usar 'viewModelScope.launch', Kotlin crea una "Corrutina" (un hilo de ejecución secundario).
     * Esto evita que la interfaz del teléfono se congele o de tirones mientras escribe en disco.
     */
    fun registrarGastoManual(comercio: String, total: Double) {
        viewModelScope.launch {
            // Generamos la fecha y hora exacta del momento del registro usando el estándar ISO 8601
            val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val timestampActual = formatoFecha.format(Date())

            // Mandamos los datos limpios al repositorio para que los inserte en la tabla SQLite
            repository.saveExpense(
                placeId = null, // De momento es null porque entra por voz o teclado, no por GPS automático
                merchant = comercio,
                total = total,
                timestamp = timestampActual,
                receiptPath = null // En la siguiente fase aquí guardaremos la ruta local de la foto
            )
        }
    }
}
