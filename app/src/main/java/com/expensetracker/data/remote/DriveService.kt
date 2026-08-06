package com.expensetracker.data.remote

import android.util.Log
import com.google.api.services.drive.Drive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriveService(private val tokenProvider: TokenProvider) {

    private fun getService(): Drive? {
        return tokenProvider.getDriveService()
    }

    suspend fun findOrCreateSpreadsheet(name: String): String? = withContext(Dispatchers.IO) {
        val service = getService() ?: return@withContext null
        try {
            // Search for an existing file with the given name created by this app
            val result = service.files().list()
                .setQ("name = '$name' and mimeType = 'application/vnd.google-apps.spreadsheet' and trashed = false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()

            val files = result.files
            if (!files.isNullOrEmpty()) {
                val fileId = files[0].id
                Log.d("DriveService", "Found existing spreadsheet: $fileId")
                return@withContext fileId
            }

            // If not found, create a new one using Google Drive API
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                this.name = name
                this.mimeType = "application/vnd.google-apps.spreadsheet"
            }
            
            val file = service.files().create(fileMetadata)
                .setFields("id")
                .execute()
                
            Log.d("DriveService", "Created new spreadsheet: ${file.id}")
            return@withContext file.id
            
        } catch (e: Exception) {
            Log.e("DriveService", "Failed to find or create spreadsheet", e)
            return@withContext null
        }
    }
}
