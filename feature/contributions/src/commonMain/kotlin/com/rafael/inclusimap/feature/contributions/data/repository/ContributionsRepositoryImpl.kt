package com.rafael.inclusimap.feature.contributions.data.repository

import com.rafael.inclusimap.core.services.AwsFileApiService
import com.rafael.inclusimap.feature.auth.domain.repository.LoginRepository
import com.rafael.inclusimap.feature.contributions.domain.model.Contribution
import com.rafael.inclusimap.feature.contributions.domain.model.ContributionType
import com.rafael.inclusimap.feature.contributions.domain.repository.ContributionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class ContributionsRepositoryImpl(
    private val loginRepository: LoginRepository,
    private val awsService: AwsFileApiService,
) : ContributionsRepository {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override suspend fun addNewContributions(contributions: List<Contribution>) {
        withContext(Dispatchers.IO) {
            val userPathId = loginRepository.getLoginInfo(1)?.userPathID ?: return@withContext
            val contributionsFile =
                awsService.downloadFile("$userPathId/contributions.json").getOrNull()

            val file = json.decodeFromString<List<Contribution>>(
                contributionsFile?.decodeToString() ?: return@withContext,
            )
            val updatedContributions = file + contributions
            awsService.uploadFile(
                "$userPathId/contributions.json",
                json.encodeToString(updatedContributions),
            ).getOrNull()
            println("Contribution added successfully: $contributions")
        }
    }

    override suspend fun removeContribution(contribution: Contribution) {
        withContext(Dispatchers.IO) {
            val userPathId = loginRepository.getLoginInfo(1)?.userPathID ?: return@withContext
            val contributionsFile =
                awsService.downloadFile("$userPathId/contributions.json").getOrNull()
            val file = json.decodeFromString<List<Contribution>>(
                contributionsFile?.decodeToString() ?: return@withContext,
            )
            val updatedContributions = file.filter {
                if (contribution.type == ContributionType.PLACE) {
                    it.fileId != contribution.fileId
                } else it != contribution
            }
            awsService.uploadFile(
                "$userPathId/contributions.json",
                json.encodeToString(updatedContributions),
            )
            println("Contribution removed successfully" + contribution.fileId)
        }
    }

    override suspend fun removeContributions(contributions: List<Contribution>) {
        withContext(Dispatchers.IO) {
            val userPathId =
                loginRepository.getLoginInfo(1)?.userPathID ?: return@withContext
            val contributionsFile =
                awsService.downloadFile("$userPathId/contributions.json").getOrNull()

            val file = json.decodeFromString<List<Contribution>>(
                contributionsFile?.decodeToString() ?: return@withContext,
            )
            val updatedContributions = file.filter { it !in contributions }
            awsService.uploadFile(
                "$userPathId/contributions.json",
                json.encodeToString(updatedContributions),
            )
            println("Contribution removed successfully: $contributions")
        }
    }
}
