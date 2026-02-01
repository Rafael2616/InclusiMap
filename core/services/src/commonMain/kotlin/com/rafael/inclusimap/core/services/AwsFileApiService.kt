package com.rafael.inclusimap.core.services

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

class AwsFileApiService(
    private val client: HttpClient,
) {
    val lambdaUrl = "https://26ud6geu2npjs3yz5tslb7gg4q0cmvfx.lambda-url.sa-east-1.on.aws/"

    suspend fun uploadFile(fileName: String, content: String): Result<Int> {
        try {
            val response: HttpResponse = client.post("$lambdaUrl?action=upload") {
                header("filename", fileName)
                contentType(ContentType.Text.Plain)
                setBody(content)
            }
            println("Upload file $fileName status: ${response.status}")
            return Result.success(response.status.value)
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    suspend fun downloadFile(fileName: String): Result<ByteArray> {
        try {
            val response = client.get("$lambdaUrl?action=download&filename=$fileName") {
                contentType(ContentType.Text.Plain)
            }
            val content = response.bodyAsBytes()

            println("Download file $fileName status: ${response.status}")
            return Result.success(content)
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    suspend fun uploadImage(fileName: String, content: ByteArray): Result<Int> {
        if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
            return Result.failure(IllegalArgumentException("Apenas imagens JPEG são suportadas."))
        }
        try {
            val response = client.post("$lambdaUrl?action=uploadImage") {
                header("filename", fileName)
                contentType(ContentType.Image.JPEG)
                setBody(content)
            }
            println("Upload image $fileName status: ${response.status}")
            return Result.success(response.status.value)
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    suspend fun downloadImage(fileName: String): Result<ByteArray> {
        try {
            val response = client.get("$lambdaUrl?action=downloadImage&filename=$fileName") {
                contentType(ContentType.Image.Any)
            }
            val content = response.bodyAsBytes()

            println("Download image $fileName status: ${response.status}")
            return Result.success(content)
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    suspend fun deleteFile(fileName: String): Result<Boolean> {
        try {
            val response = client.get("$lambdaUrl?action=delete&filename=$fileName") {
                contentType(ContentType.Image.Any)
            }
            val content = response.bodyAsText()

            println("Delete file $fileName status: ${response.status}")
            return Result.success(content == "true")
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    suspend fun createFolder(folderName: String): Result<Int> = try {
        val response = client.post("$lambdaUrl?action=createFolder") {
            header("foldername", folderName)
            contentType(ContentType.Text.Plain)
            setBody("")
        }
        println("Create folder $folderName status: ${response.status}")
        Result.success(response.status.value)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }

    suspend fun listFolder(folderName: String): Result<List<String>> = try {
        val response = client.get("$lambdaUrl?action=listFolder&foldername=$folderName") {
            contentType(ContentType.Application.Json)
        }
        val content = response.bodyAsText()

        println("List folder $folderName: $content")
        val files = Json
            .decodeFromString<List<String>>(content)
            .map { it.trim('/') }
            .map { it.substringAfterLast('/') }
            .filter { it != folderName }
        Result.success(files)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }

    suspend fun listFiles(folderName: String): Result<List<String>> = try {
        val response = client.get("$lambdaUrl?action=listFilesInFolder&foldername=$folderName") {
            contentType(ContentType.Application.Json)
        }
        val content = response.bodyAsText()

        println("List files $folderName: $content")
        val files = Json
            .decodeFromString<List<String>>(content)
            .map { it.trim('/') }
            .map { it.substringAfterLast('/') }
            .filter { it != folderName }
        Result.success(files)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }

    fun close() {
        client.close()
    }
}
