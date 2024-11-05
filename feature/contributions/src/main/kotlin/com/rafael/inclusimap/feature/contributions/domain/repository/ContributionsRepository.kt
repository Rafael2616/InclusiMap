package com.rafael.inclusimap.feature.contributions.domain.repository

import com.rafael.inclusimap.feature.contributions.domain.model.Contribution

interface ContributionsRepository {
    suspend fun addNewContribution(contribution: Contribution, attempt: Int = 1)
    suspend fun addNewContributions(contributions: List<Contribution>, attempt: Int = 1)
    suspend fun removeContribution(contribution: Contribution)
    suspend fun removeContributions(contributions: List<Contribution>)
}
