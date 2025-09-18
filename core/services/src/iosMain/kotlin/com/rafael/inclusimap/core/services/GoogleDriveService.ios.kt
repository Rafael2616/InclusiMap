package com.rafael.inclusimap.core.services

import cocoapods.GoogleAPIClientForREST.GTLRDataObject
import cocoapods.GoogleAPIClientForREST.GTLRDriveQuery_FilesCreate
import cocoapods.GoogleAPIClientForREST.GTLRDriveQuery_FilesDelete
import cocoapods.GoogleAPIClientForREST.GTLRDriveQuery_FilesGet
import cocoapods.GoogleAPIClientForREST.GTLRDriveQuery_FilesList
import cocoapods.GoogleAPIClientForREST.GTLRDriveQuery_FilesUpdate
import cocoapods.GoogleAPIClientForREST.GTLRDriveService
import cocoapods.GoogleAPIClientForREST.GTLRDrive_File
import cocoapods.GoogleAPIClientForREST.GTLRDrive_FileList
import cocoapods.GoogleAPIClientForREST.GTLRUploadParameters
import cocoapods.GoogleAPIClientForREST.GTMOAuth2ServiceAccount
import com.rafael.inclusimap.core.services.domain.DriveFile
import io.github.agropec.core.services.util.asDomainDriveFile
import io.github.agropec.core.services.util.toNSData
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSBundle
import platform.Foundation.NSLog
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

@OptIn(ExperimentalForeignApi::class)
actual class GoogleDriveService {

    private var driveService: GTLRDriveService? = null

    init {
        val serviceAccountEmail = "chaton@bubbly-granite-459018-c8.iam.gserviceaccount.com"
        val privateKeyResourceName = "credentials"
        val privateKeyFileType = "json"

        var privateKeyData: String? = null
        try {
            val path = NSBundle.mainBundle.pathForResource(privateKeyResourceName, ofType = privateKeyFileType)
            if (path != null) {
                privateKeyData = NSString.stringWithContentsOfFile(path, encoding = NSUTF8StringEncoding, error = null)
            } else {
                NSLog("ERROR: Service account key file not found: $privateKeyResourceName.$privateKeyFileType")
            }
        } catch (e: Exception) {
            NSLog("ERROR reading service account key file: ${e.message}")
        }

        if (privateKeyData != null) {
            val authorizer = GTMOAuth2ServiceAccount(
                serviceAccountEmail = serviceAccountEmail,
                privateKey = privateKeyData,
                scopes = listOf("https://www.googleapis.com/auth/drive"),
                keychainItemName = null,
            )
            authorizer?.authorizeRequest(null) { ticket, object2, error ->
                if (error != null) {
                    NSLog("Service account authorization error: ${error.localizedDescription}")
                    driveService = null
                } else {
                    driveService = GTLRDriveService().apply {
                        this.authorizer = authorizer
                        this.retryEnabled = true
                    }
                    NSLog("Service account GoogleDriveService initialized successfully.")
                }
            }
        } else {
            NSLog("GoogleDriveService WARN: Private key data not available. Service account will not be functional.")
        }
    }

    private fun getInitializedService(): GTLRDriveService = driveService ?: throw IllegalStateException(
        "GoogleDriveService is not initialized. " +
            "Ensure Google Sign-In is complete and Drive permissions are granted.",
    )

    actual suspend fun getFileContent(fileId: String): ByteArray? = suspendCancellableCoroutine { continuation ->
        val service = try {
            getInitializedService()
        } catch (e: IllegalStateException) {
            continuation.resumeWithException(e)
            return@suspendCancellableCoroutine
        }

        val query = GTLRDriveQuery_FilesGet.queryForMediaWithFileId(fileId)
        service.executeQuery(query) { _, fileObject, error ->
            if (error != null) {
                NSLog("GoogleDriveService getFileContent error for fileId '$fileId': ${error.localizedDescription}")
                continuation.resume(null)
                return@executeQuery
            }
            val data = (fileObject as? GTLRDataObject)?.data
            continuation.resume(data?.toByteArray())
        }
    }

    actual suspend fun getFileMetadata(fileId: String): DriveFile? = suspendCancellableCoroutine { continuation ->
        val service = try {
            getInitializedService()
        } catch (e: IllegalStateException) {
            continuation.resumeWithException(e)
            return@suspendCancellableCoroutine
        }
        val query = GTLRDriveQuery_FilesGet.queryWithFileId(fileId)
        query.fields = "id,name"

        service.executeQuery(query) { _, file, error ->
            if (error != null) {
                NSLog("GoogleDriveService getFileMetadata error for fileId '$fileId': ${error.localizedDescription}")
                continuation.resume(null)
                return@executeQuery
            }
            continuation.resume((file as? GTLRDrive_File)?.asDomainDriveFile())
        }
    }

    actual suspend fun listFiles(folderId: String): Result<List<DriveFile>> = suspendCancellableCoroutine { continuation ->
        val service = try {
            getInitializedService()
        } catch (e: IllegalStateException) {
            continuation.resume(Result.failure(e))
            return@suspendCancellableCoroutine
        }

        val resultList = mutableListOf<DriveFile>()
        var pageToken: String? = null

        fun fetchPage() {
            val query = GTLRDriveQuery_FilesList.query()
            query.q = "'$folderId' in parents and trashed=false"
            query.fields = "nextPageToken, files(id, name)"
            query.pageToken = pageToken

            service.executeQuery(query) { _, fileListObject, error ->
                if (error != null) {
                    NSLog("GoogleDriveService listFiles error for folderId '$folderId': ${error.localizedDescription}")
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                    return@executeQuery
                }

                val gtlrFileList = fileListObject as? GTLRDrive_FileList
                gtlrFileList?.files?.forEach { gtlrFile ->
                    (gtlrFile as? GTLRDrive_File)?.asDomainDriveFile()?.let { resultList.add(it) }
                }

                pageToken = gtlrFileList?.nextPageToken
                if (pageToken?.isNotEmpty() == true) {
                    fetchPage()
                } else {
                    continuation.resume(Result.success(resultList))
                }
            }
        }
        fetchPage()
    }

    actual suspend fun listSharedFolders(): List<DriveFile> = suspendCancellableCoroutine { continuation ->
        val service = try {
            getInitializedService()
        } catch (e: IllegalStateException) {
            continuation.resumeWithException(e)
            return@suspendCancellableCoroutine
        }
        val resultList = mutableListOf<DriveFile>()
        var pageToken: String? = null

        fun fetchPage() {
            val query = GTLRDriveQuery_FilesList.query()
            query.q = "mimeType='application/vnd.google-apps.folder' and sharedWithMe=true"
            query.fields = "nextPageToken, files(id, name)"
            query.supportsAllDrives = true
            query.pageToken = pageToken

            service.executeQuery(query) { _, fileListObject, error ->
                if (error != null) {
                    NSLog("GoogleDriveService listSharedFolders error: ${error.localizedDescription}")
                    continuation.resumeWithException(Exception(error.localizedDescription))
                    return@executeQuery
                }
                val gtlrFileList = fileListObject as? GTLRDrive_FileList
                gtlrFileList?.files?.forEach { gtlrFile ->
                    (gtlrFile as? GTLRDrive_File)?.asDomainDriveFile()?.let { resultList.add(it) }
                }
                pageToken = gtlrFileList?.nextPageToken
                if (pageToken?.isNotEmpty() == true) {
                    fetchPage()
                } else {
                    continuation.resume(resultList)
                }
            }
        }
        fetchPage()
    }

    actual suspend fun uploadFile(
        fileContent: ByteArray?,
        fileName: String,
        folderId: String,
    ): String? = suspendCancellableCoroutine { continuation ->
        val service = try {
            getInitializedService()
        } catch (e: IllegalStateException) {
            continuation.resumeWithException(e)
            return@suspendCancellableCoroutine
        }
        if (fileContent == null) {
            NSLog("GoogleDriveService uploadFile error: fileContent is null for fileName '$fileName'")
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        val driveFile = GTLRDrive_File().apply {
            this.name = fileName
            this.parents = listOf(folderId)
        }
        val mimeType = when {
            fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
            fileName.endsWith(".png", true) -> "image/png"
            else -> "application/octet-stream"
        }
        val uploadParameters = GTLRUploadParameters().apply {
            this.data = fileContent.toNSData()
            this.MIMEType = mimeType
        }
        val query = GTLRDriveQuery_FilesCreate.queryWithObject(driveFile, uploadParameters)
        query.fields = "id"

        service.executeQuery(query) { _, createdFileObject, error ->
            if (error != null) {
                NSLog("GoogleDriveService uploadFile error for fileName '$fileName': ${error.localizedDescription}")
                continuation.resume(null)
                return@executeQuery
            }
            continuation.resume((createdFileObject as? GTLRDrive_File)?.identifier)
        }
    }

    actual suspend fun deleteFile(fileId: String): Boolean = suspendCancellableCoroutine { continuation ->
        val service = try {
            getInitializedService()
        } catch (e: IllegalStateException) {
            continuation.resumeWithException(e)
            return@suspendCancellableCoroutine
        }
        val query = GTLRDriveQuery_FilesDelete.queryWithFileId(fileId)
        service.executeQuery(query) { _, _, error ->
            if (error != null) {
                NSLog("GoogleDriveService deleteFile error for fileId '$fileId': ${error.localizedDescription}")
                continuation.resume(false)
            } else {
                continuation.resume(true)
            }
        }
    }

    actual suspend fun createFolder(
        folderName: String,
        parentFolderId: String?,
    ): String? = suspendCancellableCoroutine { continuation ->
        val service = try {
            getInitializedService()
        } catch (e: IllegalStateException) {
            continuation.resumeWithException(e)
            return@suspendCancellableCoroutine
        }
        val folderMetadata = GTLRDrive_File().apply {
            name = folderName
            mimeType = "application/vnd.google-apps.folder"
            if (parentFolderId != null) {
                parents = listOf(parentFolderId)
            }
        }
        val query = GTLRDriveQuery_FilesCreate.queryWithObject(folderMetadata, null)
        query.fields = "id"

        service.executeQuery(query) { _, createdFolderObject, error ->
            if (error != null) {
                NSLog("GoogleDriveService createFolder error for folderName '$folderName': ${error.localizedDescription}")
                continuation.resume(null)
                return@executeQuery
            }
            continuation.resume((createdFolderObject as? GTLRDrive_File)?.identifier)
        }
    }

    actual suspend fun updateFile(
        fileId: String,
        fileName: String,
        content: ByteArray,
    ): DriveFile? = suspendCancellableCoroutine { continuation ->
        val service = try {
            getInitializedService()
        } catch (e: IllegalStateException) {
            continuation.resumeWithException(e)
            return@suspendCancellableCoroutine
        }
        val fileMetadata = GTLRDrive_File().apply {
            this.name = fileName
        }
        val uploadParameters = GTLRUploadParameters().apply {
            this.data = content.toNSData()
            this.MIMEType = "application/json"
        }
        val query = GTLRDriveQuery_FilesUpdate.queryWithObject(fileMetadata, fileId, uploadParameters)
        query.fields = "id,name"

        service.executeQuery(query) { _, updatedFileObject, error ->
            if (error != null) {
                NSLog("GoogleDriveService updateFile error for fileId '$fileId': ${error.localizedDescription}")
                continuation.resume(null)
                return@executeQuery
            }
            continuation.resume((updatedFileObject as? GTLRDrive_File)?.asDomainDriveFile())
        }
    }

    actual suspend fun createFile(
        fileName: String,
        content: String,
        parentFolderId: String?,
    ): String? = suspendCancellableCoroutine { continuation ->
        val service = try {
            getInitializedService()
        } catch (e: IllegalStateException) {
            continuation.resumeWithException(e)
            return@suspendCancellableCoroutine
        }

        val fileMetadata = GTLRDrive_File().apply {
            this.name = fileName
            this.mimeType = "application/json"
            if (parentFolderId != null) {
                this.parents = listOf(parentFolderId)
            }
        }

        val contentData = content.encodeToByteArray().toNSData()

        val uploadParameters = GTLRUploadParameters().apply {
            this.data = contentData
            this.MIMEType = fileMetadata.mimeType
        }

        val query = GTLRDriveQuery_FilesCreate.queryWithObject(fileMetadata, uploadParameters)
        query.fields = "id"

        service.executeQuery(query) { _, createdFileObject, error ->
            if (error != null) {
                NSLog("GoogleDriveService createFile error for fileName '$fileName': ${error.localizedDescription}")
                continuation.resume(null)
                return@executeQuery
            }
            continuation.resume((createdFileObject as? GTLRDrive_File)?.identifier)
        }
    }
}
