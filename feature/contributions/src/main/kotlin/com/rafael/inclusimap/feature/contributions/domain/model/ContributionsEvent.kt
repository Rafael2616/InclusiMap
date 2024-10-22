package com.rafael.inclusimap.feature.contributions.domain.model

sealed interface ContributionsEvent {
    data object LoadUserContributions : ContributionsEvent
}
