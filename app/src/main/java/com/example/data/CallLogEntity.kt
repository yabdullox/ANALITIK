package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val callerName: String,
    val phoneNumber: String,
    val timestamp: Long,
    val durationSeconds: Int,
    val callType: String, // "ANSWERED", "MISSED", "REJECTED"
    val isUnknown: Boolean
)
