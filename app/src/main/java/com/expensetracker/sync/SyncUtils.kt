package com.expensetracker.sync

object SyncUtils {
    // Spec: latest updatedAt wins; if equal, highest version wins.
    fun isRemoteNewer(
        remoteUpdated: Long,
        remoteVersion: Int,
        localUpdated: Long,
        localVersion: Int
    ): Boolean {
        return when {
            remoteUpdated > localUpdated -> true
            remoteUpdated < localUpdated -> false
            else -> remoteVersion > localVersion
        }
    }
}
