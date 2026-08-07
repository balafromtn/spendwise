package com.expensetracker.data.remote

import android.util.Log
import com.expensetracker.di.SpreadsheetConfig
import com.google.api.services.drive.Drive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriveService(private val tokenProvider: TokenProvider) {

    private fun getService(): Drive? {
        return tokenProvider.getDriveService()
    }

    /**
     * Finds or creates the SpendWise spreadsheet in the user's Google Drive.
     *
     * To prevent race conditions (two devices creating duplicate sheets),
     * we check SpreadsheetConfig first. If a valid ID is already stored,
     * we skip the Drive search entirely.
     *
     * Created files are tagged with appProperties so the search query
     * filters precisely on SpendWise-created files.
     */
    suspend fun findOrCreateSpreadsheet(name: String): String? = withContext(Dispatchers.IO) {
        // Short-circuit: if we already have a valid spreadsheet ID stored, use it
        val existingId = SpreadsheetConfig.getSpreadsheetId()
        if (existingId.isNotBlank() && existingId != "YOUR_SPREADSHEET_ID_HERE") {
            Log.d("DriveService", "Using cached spreadsheet ID: $existingId")
            return@withContext existingId
        }

        val service = getService() ?: return@withContext null
        try {
            // Escape single quotes in name defensively (Fix #10)
            val safeName = name.replace("'", "\\'")

            // Search for an existing file tagged by this app
            val result = service.files().list()
                .setQ("name = '$safeName' and mimeType = 'application/vnd.google-apps.spreadsheet' and trashed = false and appProperties has { key='spendwise' and value='true' }")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()

            val files = result.files
            if (!files.isNullOrEmpty()) {
                val fileId = files[0].id
                Log.d("DriveService", "Found existing spreadsheet: $fileId")
                SpreadsheetConfig.setSpreadsheetId(fileId)
                return@withContext fileId
            }

            // Also check for legacy untagged files (from before this fix)
            val legacyResult = service.files().list()
                .setQ("name = '$safeName' and mimeType = 'application/vnd.google-apps.spreadsheet' and trashed = false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()

            if (!legacyResult.files.isNullOrEmpty()) {
                val fileId = legacyResult.files[0].id
                Log.d("DriveService", "Found legacy untagged spreadsheet: $fileId, tagging it now")
                // Tag the legacy file so future searches find it via the primary query
                val updateMetadata = com.google.api.services.drive.model.File()
                    .setAppProperties(mapOf("spendwise" to "true"))
                service.files().update(fileId, updateMetadata).execute()
                SpreadsheetConfig.setSpreadsheetId(fileId)
                return@withContext fileId
            }

            // If not found, create a new one
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                this.name = name
                this.mimeType = "application/vnd.google-apps.spreadsheet"
                this.appProperties = mapOf("spendwise" to "true")
            }

            val file = service.files().create(fileMetadata)
                .setFields("id")
                .execute()

            Log.d("DriveService", "Created new spreadsheet: ${file.id}")
            SpreadsheetConfig.setSpreadsheetId(file.id)
            return@withContext file.id

        } catch (e: Exception) {
            Log.e("DriveService", "Failed to find or create spreadsheet", e)
            return@withContext null
        }
    }
}
