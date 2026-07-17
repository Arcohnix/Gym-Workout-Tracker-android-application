package com.example.forgegym.data.repository

interface BackupRepository {
    suspend fun exportBackup(): String
    suspend fun importBackup(json: String): Result<Unit>
    suspend fun exportHistoryToCsv(): String
    suspend fun deleteAllData()
    suspend fun resetApp()
}
