package com.rafael.inclusimap.data

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.io.FileNotFoundException

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

    fun getFileContent(fileId: String): ByteArray {
        return driveService.files().get(fileId).executeAsInputStream().readBytes()
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
}
