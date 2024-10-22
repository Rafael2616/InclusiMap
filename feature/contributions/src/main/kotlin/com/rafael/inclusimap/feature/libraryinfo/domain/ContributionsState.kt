package com.rafael.inclusimap.feature.libraryinfo.domain

import com.rafael.inclusimap.feature.libraryinfo.domain.model.Contributions
import com.rafael.inclusimap.feature.libraryinfo.domain.model.ContributionsSize

data class ContributionsState(
    val isLoadingContributions: Boolean = false,
    val contributionsSize: ContributionsSize = ContributionsSize(),
    val userContributions: Contributions = Contributions(),
    val shouldRefresh: Boolean = false,
    var allCommentsContributionsLoaded: Boolean = false,
    val allPlacesContributionsLoaded: Boolean = false,
    val allImagesContributionsLoaded: Boolean = false,
)
