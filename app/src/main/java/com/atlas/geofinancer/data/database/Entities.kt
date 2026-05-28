package com.atlas.geofinancer.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val arrivalTime: String,
    val departureTime: String?,
    val durationMinutes: Int = 0
)

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = PlaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["placeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val placeId: Int?,
    val merchant: String,
    val total: Double,
    val timestamp: String,
    val receiptPath: String?
)
