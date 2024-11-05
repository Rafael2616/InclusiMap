package com.rafael.inclusimap.feature.contributions.data.repository

import com.rafael.inclusimap.core.domain.network.onSuccess
import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.feature.auth.domain.repository.LoginRepository
import com.rafael.inclusimap.feature.contributions.domain.model.Contribution
import com.rafael.inclusimap.feature.contributions.domain.model.ContributionType
import com.rafael.inclusimap.feature.contributions.domain.repository.ContributionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ContributionsRepositoryImpl(
    private val loginRepository: LoginRepository,
    private val driveService: GoogleDriveService,
) : ContributionsRepository {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override suspend fun addNewContributions(contributions: List<Contribution>, attempt: Int) {
        withContext(Dispatchers.IO) {
            val userPathId = loginRepository.getLoginInfo(1)?.userPathID ?: return@withContext
            driveService.listFiles(userPathId).onSuccess { userFiles ->
                val userContributionsFile = userFiles.find { it.name == "contributions.json" }
                    ?.also { contributionsFile ->
                        val userContributions =
                            driveService.getFileContent(contributionsFile.id)
                        val file = json.decodeFromString<List<Contribution>>(
                            userContributions?.decodeToString() ?: return@withContext,
                        )
                        val updatedContributions = file + contributions
                        val updatedContributionsString =
                            json.encodeToString(updatedContributions)
                        driveService.updateFile(
                            contributionsFile.id,
                            "contributions.json",
                            updatedContributionsString.byteInputStream(),
                        )
                        println("Contribution added successfully: $contributions")
                    }
                if (userContributionsFile == null && attempt < 3) {
                    driveService.createFile(
                        "contributions.json",
                        "[]",
                        userPathId,
                    )
                    println("Contribution file was created now, attempting to add contribution again")
                    addNewContributions(contributions, attempt + 1)
                }
            }
        }
    }

    override suspend fun addNewContribution(contribution: Contribution, attempt: Int) {
        withContext(Dispatchers.IO) {
            val userPathId = loginRepository.getLoginInfo(1)?.userPathID ?: return@withContext
            driveService.listFiles(userPathId).onSuccess { userFiles ->
                val userContributionsFile = userFiles.find { it.name == "contributions.json" }
                    ?.also { contributionsFile ->
                        val contributions =
                            driveService.getFileContent(contributionsFile.id)
                        val file = json.decodeFromString<List<Contribution>>(
                            contributions?.decodeToString() ?: return@withContext,
                        )
                        if (file.any { it == contribution }) return@withContext
                        val updatedContributions = file + contribution
                        val updatedContributionsString =
                            json.encodeToString(updatedContributions)
                        driveService.updateFile(
                            contributionsFile.id,
                            "contributions.json",
                            updatedContributionsString.byteInputStream(),
                        )
                        println("Contribution added successfully" + contribution.fileId)
                    }
                if (userContributionsFile == null && attempt < 3) {
                    driveService.createFile(
                        "contributions.json",
                        "[]",
                        userPathId,
                    )
                    println("Contribution file was created now, attempting to add contribution again")
                    addNewContribution(contribution, attempt + 1)
                }
            }
        }
    }

    override suspend fun removeContribution(contribution: Contribution) {
        withContext(Dispatchers.IO) {
            val userPathId = loginRepository.getLoginInfo(1)?.userPathID ?: return@withContext
            driveService.listFiles(userPathId).onSuccess { userFiles ->
                userFiles.find { it.name == "contributions.json" }
                    ?.also { contributionsFile ->
                        val contributions =
                            driveService.getFileContent(contributionsFile.id)
                        val file = json.decodeFromString<List<Contribution>>(
                            contributions?.decodeToString() ?: return@withContext,
                        )
                        val updatedContributions =
                            file.filter { if (contribution.type == ContributionType.PLACE) it.fileId != contribution.fileId else it != contribution }

                        val updatedContributionsString =
                            json.encodeToString(updatedContributions)
                        driveService.updateFile(
                            contributionsFile.id,
                            "contributions.json",
                            updatedContributionsString.byteInputStream(),
                        )
                        println("Contribution removed successfully" + contribution.fileId)
                    }
            }
        }
    }

    override suspend fun removeContributions(contributions: List<Contribution>) {
        withContext(Dispatchers.IO) {
            val userPathId =
                loginRepository.getLoginInfo(1)?.userPathID ?: return@withContext

            driveService.listFiles(userPathId).onSuccess { userFiles ->
                userFiles.find { it.name == "contributions.json" }
                    ?.also { contributionsFile ->
                        val userContributions =
                            driveService.getFileContent(contributionsFile.id)
                        val file = json.decodeFromString<List<Contribution>>(
                            userContributions?.decodeToString() ?: return@withContext,
                        )
                        val updatedContributions = file.filter { it !in contributions }
                        val updatedContributionsString =
                            json.encodeToString(updatedContributions)
                        driveService.updateFile(
                            contributionsFile.id,
                            "contributions.json",
                            updatedContributionsString.byteInputStream(),
                        )
                        println("Contribution removed successfully: $contributions")
                    }
            }
        }
    }
}
