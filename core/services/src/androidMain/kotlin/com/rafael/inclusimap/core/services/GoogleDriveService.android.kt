package com.rafael.inclusimap.core.services

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.rafael.inclusimap.core.services.domain.DriveFile
import java.io.FileNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class GoogleDriveService {
    private val driveService: Drive

    init {
        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()
        val credentialsStream =
            this.javaClass.getResourceAsStream("/credentials.json")
                ?: throw FileNotFoundException("Resource not found: credentials.json")

        val credentials =
            GoogleCredentials
                .fromStream(credentialsStream)
                .createScoped(listOf("https://www.googleapis.com/auth/drive"))

        val httpRequestInitializer = HttpCredentialsAdapter(credentials)
        driveService =
            Drive.Builder(transport, jsonFactory, httpRequestInitializer)
                .setApplicationName("ChatOn")
                .build()
    }

    actual suspend fun getFileContent(fileId: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            driveService.files().get(fileId).executeMediaAsInputStream().readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual suspend fun getFileMetadata(fileId: String): DriveFile? = try {
        driveService.files().get(fileId).execute().toDriveFile()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    actual suspend fun listFiles(folderId: String): Result<List<DriveFile>> {
        val result = mutableListOf<DriveFile>()
        var pageToken: String? = null

        return withContext(Dispatchers.IO) {
            try {
                do {
                    val request =
                        driveService.files().list().setQ("'$folderId' in parents and trashed=false")
                            .setFields("nextPageToken, files(id, name)")
                            .setPageToken(pageToken)

                    val files = request.execute()
                    result.addAll(files.files?.map { it.toDriveFile() } ?: emptyList())
                    pageToken = files.nextPageToken
                } while (!pageToken.isNullOrEmpty())
                Result.success(result)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(Error(e.message ?: "Unknown error"))
            }
        }
    }

    actual suspend fun listSharedFolders(): List<DriveFile> {
        val result = mutableListOf<DriveFile>()
        var pageToken: String? = null

        do {
            val request =
                driveService.files().list().setQ("mimeType='application/vnd.google-apps.folder' and sharedWithMe=true")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .setSupportsAllDrives(true)

            val files = request?.execute()
            result.addAll(files?.files?.map { it.toDriveFile() } ?: emptyList())
            pageToken = files?.nextPageToken
        } while (!pageToken.isNullOrEmpty())

        return result
    }

    actual suspend fun uploadFile(
        fileContent: ByteArray?,
        fileName: String,
        folderId: String,
    ): String? = try {
        val fileMetadata = File()
        fileMetadata.name = fileName
        fileMetadata.parents = listOf(folderId)

        val mimeType =
            when {
                fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
                fileName.endsWith(".png", true) -> "image/png"
                else -> "application/octet-stream"
            }
        val mediaContent = InputStreamContent(mimeType, fileContent?.inputStream())

        val file = driveService.files().create(fileMetadata, mediaContent).setFields("id").execute()

        file.id
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    actual suspend fun deleteFile(fileId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            driveService.files().delete(fileId).execute()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    actual suspend fun createFolder(
        folderName: String,
        parentFolderId: String?,
    ): String? = withContext(Dispatchers.IO) {
        try {
            val folderMetadata =
                File().apply {
                    name = folderName
                    mimeType = "application/vnd.google-apps.folder"
                    if (parentFolderId != null) {
                        parents = listOf(parentFolderId)
                    }
                }

            val folder = driveService.files().create(folderMetadata).setFields("id").execute()

            folder.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual suspend fun updateFile(
        fileId: String,
        fileName: String,
        content: ByteArray,
    ) = withContext(Dispatchers.IO) {
        try {
            val fileMetadata = File()
            fileMetadata.name = fileName
            val mediaContent = InputStreamContent("application/json", content.inputStream())

            driveService.files().update(fileId, fileMetadata, mediaContent).execute().toDriveFile()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual suspend fun createFile(
        fileName: String,
        content: String,
        parentFolderId: String?,
    ): String? = withContext(Dispatchers.IO) {
        try {
            val fileMetadata =
                File().apply {
                    name = fileName
                    mimeType = "application/json"
                    if (parentFolderId != null) {
                        parents = listOf(parentFolderId)
                    }
                }

            val mediaContent = ByteArrayContent(fileMetadata.mimeType, content.toByteArray())

            val file =
                driveService.files().create(fileMetadata, mediaContent).setFields("id").execute()

            file.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun File.toDriveFile() = DriveFile(id = id, name = name)
