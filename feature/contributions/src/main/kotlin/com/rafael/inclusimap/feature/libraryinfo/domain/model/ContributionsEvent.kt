package com.rafael.inclusimap.feature.libraryinfo.domain.model

sealed interface ContributionsEvent {
    data object LoadUserContributions : ContributionsEvent
}
