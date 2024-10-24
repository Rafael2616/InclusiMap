package com.rafael.inclusimap.feature.contributions.domain

import com.rafael.inclusimap.feature.contributions.domain.model.Contributions
import com.rafael.inclusimap.feature.contributions.domain.model.ContributionsSize

data class ContributionsState(
    val isLoadingContributions: Boolean = false,
    val contributionsSize: ContributionsSize = ContributionsSize(),
    val userContributions: Contributions = Contributions(),
    val shouldRefresh: Boolean = false,
    var allCommentsContributionsLoaded: Boolean = false,
    val allPlacesContributionsLoaded: Boolean = false,
    val allImagesContributionsLoaded: Boolean = false,
    val allResourcesContributionsLoaded: Boolean = false
)
