package com.rafael.inclusimap.feature.auth.domain.model

enum class DeleteProcess {
    NO_OP,
    DELETING_USER_INFO,
    DELETING_USER_IMAGES,
    DELETING_USER_COMMENTS,
    DELETING_USER_LOCAL_MARKERS,
    SUCCESS,
    ERROR,
}
