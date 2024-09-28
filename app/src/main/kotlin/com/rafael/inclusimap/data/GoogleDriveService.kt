package com.rafael.inclusimap.data

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.InputStream

class GoogleDriveService {
    val driveService: Drive

    init {
        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()
        val credentialsStream = this.javaClass.getResourceAsStream("/credentials.json")
            ?: throw FileNotFoundException("Resource not found: credentials.json")

        val credentials = GoogleCredentials.fromStream(credentialsStream)
            .createScoped(listOf("https://www.googleapis.com/auth/drive"))

        val httpRequestInitializer = HttpCredentialsAdapter(credentials)
        driveService = Drive.Builder(transport, jsonFactory, httpRequestInitializer)
            .setApplicationName("InclusiMap")
            .build()
    }

    suspend fun getFileContent(fileId: String): ByteArray {
        return withContext(Dispatchers.IO) {
            driveService.files().get(fileId).executeMediaAsInputStream().readBytes()
        }
    }

    fun getFileMetadata(fileId: String): File? {
        return driveService.files().get(fileId).execute()
    }

    fun listFiles(folderId: String): List<File> {
        val result = mutableListOf<File>()
        var pageToken: String? = null

        do {
            val request = driveService.files().list()
                .setQ("'${folderId}' in parents and trashed=false")
                .setFields("nextPageToken, files(id, name)")
                .setPageToken(pageToken)

            val files = request.execute()
            result.addAll(files.files ?: emptyList())
            pageToken = files.nextPageToken
        } while (!pageToken.isNullOrEmpty())

        return result
    }

    fun listSharedFolders(): List<File> {
        val result = mutableListOf<File>()
        var pageToken: String? = null

        do {
            val request = driveService.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder' and sharedWithMe=true")
                .setFields("nextPageToken, files(id, name)")
                .setPageToken(pageToken)
                .setSupportsAllDrives(true)

            val files = request?.execute()
            result.addAll(files?.files ?: emptyList())
            pageToken = files?.nextPageToken
        } while (!pageToken.isNullOrEmpty())

        return result
    }

    fun uploadFile(fileContent: InputStream?, fileName: String, folderId: String): String? {
        return try {
            val fileMetadata = File()
            fileMetadata.name = fileName
            fileMetadata.parents = listOf(folderId)

            val mimeType = when {
                fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
                fileName.endsWith(".png", true) -> "image/png"
                else -> "application/octet-stream"
            }
            val mediaContent = InputStreamContent(mimeType, fileContent)

            val file = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()

            file.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteFile(fileId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                driveService.files().delete(fileId).execute()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun createFolder(folderName: String, parentFolderId: String? = null): String? {
        return withContext(Dispatchers.IO) {
            try {
                val folderMetadata = File().apply {
                    name = folderName
                    mimeType = "application/vnd.google-apps.folder"
                    if (parentFolderId != null) {
                        parents = listOf(parentFolderId)
                    }
                }

                val folder = driveService.files().create(folderMetadata)
                    .setFields("id")
                    .execute()

                folder.id
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun updateFile(fileId: String, fileName: String, content: InputStream) {
        return withContext(Dispatchers.IO) {
            try {
                val fileMetadata = File()
                fileMetadata.name = fileName
                val mediaContent = InputStreamContent("application/json", content)
                driveService.files().update(fileId, fileMetadata, mediaContent).execute()
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
