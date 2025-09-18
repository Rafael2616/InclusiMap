package com.rafael.inclusimap.core.services

import com.rafael.inclusimap.core.services.domain.DriveFile

expect class GoogleDriveService() {

    suspend fun getFileContent(fileId: String): ByteArray?

    suspend fun getFileMetadata(fileId: String): DriveFile?

    suspend fun listFiles(folderId: String): Result<List<DriveFile>>

    suspend fun listSharedFolders(): List<DriveFile>

    suspend fun uploadFile(
        fileContent: ByteArray?,
        fileName: String,
        folderId: String,
    ): String?

    suspend fun deleteFile(fileId: String): Boolean

    suspend fun createFolder(
        folderName: String,
        parentFolderId: String? = null,
    ): String?

    suspend fun updateFile(
        fileId: String,
        fileName: String,
        content: ByteArray,
    ): DriveFile?

    suspend fun createFile(
        fileName: String,
        content: String,
        parentFolderId: String? = null,
    ): String?
}
