package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class CallRepository(private val callLogDao: CallLogDao) {
    val allCallLogs: Flow<List<CallLogEntity>> = callLogDao.getAllCallLogs()

    suspend fun insert(callLog: CallLogEntity) {
        callLogDao.insertCallLog(callLog)
    }

    suspend fun delete(callLog: CallLogEntity) {
        callLogDao.deleteCallLog(callLog)
    }

    suspend fun clear() {
        callLogDao.clearAll()
    }

    suspend fun seedDatabaseIfEmpty() {
        if (callLogDao.getCount() == 0) {
            val now = System.currentTimeMillis()
            val hourMs = 60 * 60 * 1000L
            val dayMs = 24 * hourMs

            val initialLogs = listOf(
                // TODAY
                CallLogEntity(
                    callerName = "Alex Mercer",
                    phoneNumber = "+1 415-555-0198",
                    timestamp = now - 2 * hourMs, // 2 hours ago (Today)
                    durationSeconds = 142,
                    callType = "ANSWERED",
                    isUnknown = false
                ),
                CallLogEntity(
                    callerName = "Unknown Number",
                    phoneNumber = "+1 800-555-0143",
                    timestamp = now - 4 * hourMs, // 4 hours ago (Today, Unknown)
                    durationSeconds = 0,
                    callType = "REJECTED",
                    isUnknown = true
                ),
                CallLogEntity(
                    callerName = "Mother",
                    phoneNumber = "+1 212-555-7821",
                    timestamp = now - 6 * hourMs, // 6 hours ago (Today)
                    durationSeconds = 312,
                    callType = "ANSWERED",
                    isUnknown = false
                ),
                CallLogEntity(
                    callerName = "+1 650-555-0112",
                    phoneNumber = "+1 650-555-0112",
                    timestamp = now - 18 * hourMs, // Evening/last night (within 24h, Today/Yesterday depending on exact hr)
                    durationSeconds = 0,
                    callType = "MISSED",
                    isUnknown = true
                ),

                // THIS WEEK (Yesterday to 6 days ago)
                CallLogEntity(
                    callerName = "Unknown Telemarketer",
                    phoneNumber = "+1 888-555-9000",
                    timestamp = now - 1 * dayMs - 1 * hourMs, // Yesterday
                    durationSeconds = 0,
                    callType = "REJECTED",
                    isUnknown = true
                ),
                CallLogEntity(
                    callerName = "Boss",
                    phoneNumber = "+1 202-555-3211",
                    timestamp = now - 2 * dayMs, // 2 days ago
                    durationSeconds = 95,
                    callType = "ANSWERED",
                    isUnknown = false
                ),
                CallLogEntity(
                    callerName = "Jessica Parker",
                    phoneNumber = "+1 310-555-9012",
                    timestamp = now - 3 * dayMs - 3 * hourMs, // 3 days ago, afternoon
                    durationSeconds = 0,
                    callType = "MISSED",
                    isUnknown = false
                ),
                CallLogEntity(
                    callerName = "Spam Shield",
                    phoneNumber = "+1 800-555-1212",
                    timestamp = now - 5 * dayMs, // 5 days ago
                    durationSeconds = 0,
                    callType = "REJECTED",
                    isUnknown = true
                ),

                // THIS MONTH (7 to 28 days ago)
                CallLogEntity(
                    callerName = "Delivery Courier",
                    phoneNumber = "+1 800-555-8844",
                    timestamp = now - 8 * dayMs, // 8 days ago
                    durationSeconds = 45,
                    callType = "ANSWERED",
                    isUnknown = false
                ),
                CallLogEntity(
                    callerName = "Father",
                    phoneNumber = "+1 212-555-7822",
                    timestamp = now - 12 * dayMs, // 12 days ago
                    durationSeconds = 1205, // Long answered call!
                    callType = "ANSWERED",
                    isUnknown = false
                ),
                CallLogEntity(
                    callerName = "+1 408-555-3435",
                    phoneNumber = "+1 408-555-3435",
                    timestamp = now - 15 * dayMs, // 15 days ago
                    durationSeconds = 0,
                    callType = "MISSED",
                    isUnknown = true
                ),
                CallLogEntity(
                    callerName = "Dentist Clinic",
                    phoneNumber = "+1 415-555-9988",
                    timestamp = now - 22 * dayMs, // 22 days ago
                    durationSeconds = 75,
                    callType = "ANSWERED",
                    isUnknown = false
                )
            )
            callLogDao.insertAll(initialLogs)
        }
    }
}
