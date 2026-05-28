package com.atlas.geofinancer.data.repository

import com.atlas.geofinancer.data.database.FinancialDao
import com.atlas.geofinancer.data.database.PlaceEntity
import com.atlas.geofinancer.data.database.ExpenseEntity
import kotlinx.coroutines.flow.Flow

/**
 * Clase Repositorio. Recibe el DAO por "inyección de dependencias" manual.
 * No le importa cómo funciona la base de datos por dentro, solo expone métodos limpios.
 */
class FinancialRepository(private val financialDao: FinancialDao) {

    // 1. EXPONER FLUJOS RECTIVOS (FLOW)
    // Al asignar directamente el Flow del DAO, cualquier pantalla que observe estas variables
    // se actualizará sola cuando un dato cambie en SQLite de forma local.
    val allPlaces: Flow<List<PlaceEntity>> = financialDao.getAllPlaces()
    val allExpenses: Flow<List<ExpenseEntity>> = financialDao.getAllExpenses()

    /**
     * Guarda un nuevo lugar visitado cuando el GPS detecta estabilidad.
     * Es una función 'suspend' porque escribir en el disco del teléfono toma tiempo
     * y debe hacerse fuera del hilo principal de la pantalla.
     * * @return El ID (Long) generado automáticamente por SQLite para este lugar.
     */
    suspend fun saveNewPlace(latitude: Double, longitude: Double, arrivalTime: String): Long {
        val nuevoLugar = PlaceEntity(
            latitude = latitude,
            longitude = longitude,
            arrivalTime = arrivalTime,
            departureTime = null // Es null porque el usuario acaba de llegar
        )
        return financialDao.insertPlace(nuevoLugar)
    }

    /**
     * Actualiza el registro de un lugar cuando el usuario se marcha de él.
     * Modifica la hora de salida y calcula la permanencia exacta.
     */
    suspend fun closePlaceStay(placeId: Int, departureTime: String, durationMinutes: Int) {
        financialDao.updatePlaceDeparture(placeId, departureTime, durationMinutes)
    }

    /**
     * Inserta un gasto en la base de datos. Puede estar vinculado a un lugar o ser manual.
     */
    suspend fun saveExpense(
        placeId: Int?, 
        merchant: String, 
        total: Double, 
        timestamp: String, 
        receiptPath: String?
    ): Long {
        val nuevoGasto = ExpenseEntity(
            placeId = placeId,
            merchant = merchant,
            total = total,
            timestamp = timestamp,
            receiptPath = receiptPath
        )
        return financialDao.insertExpense(nuevoGasto)
    }
}
