package com.rafael.inclusimap.feature.report.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.core.util.map.Constants.INCLUSIMAP_REPORT_FOLDER_ID
import com.rafael.inclusimap.feature.auth.domain.model.User
import com.rafael.inclusimap.feature.auth.domain.repository.LoginRepository
import com.rafael.inclusimap.feature.report.domain.model.Report
import com.rafael.inclusimap.feature.report.domain.model.ReportState
import com.rafael.inclusimap.feature.report.domain.model.ReportType
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class ReportViewModel(
    private val loginRepository: LoginRepository,
    private val driveService: GoogleDriveService,
) : ViewModel() {
    private val _state = MutableStateFlow(ReportState())
    val state = _state.asStateFlow()

    @OptIn(ExperimentalTime::class)
    fun onReport(report: Report) {
        viewModelScope
            .launch(Dispatchers.IO) {
                _state.update {
                    it.copy(
                        isReported = false,
                        isReporting = true,
                        isError = false,
                    )
                }
                val loginData = loginRepository.getLoginInfo(1)!!
                val user =
                    User(
                        name = loginData.userName!!,
                        email = loginData.userEmail!!,
                        password = loginData.userPassword!!,
                        id = loginData.userId!!,
                        showProfilePictureOptedIn = loginData.showProfilePictureOptedIn,
                        isBanned = loginData.isBanned,
                        isAdmin = loginData.isAdmin,
                        showFirstTimeAnimation = loginData.showFirstTimeAnimation,
                    )
                val json =
                    Json {
                        prettyPrint = true
                        encodeDefaults = true
                    }
                val completedReport =
                    report.copy(
                        user = user,
                        reportedLocal =
                        report.reportedLocal.copy(
                            comments =
                            if (report.type == ReportType.COMMENT ||
                                report.type == ReportType.OTHER
                            ) {
                                report.reportedLocal.comments
                            } else {
                                emptyList()
                            },
                        ),
                    )
                val jsonReport = json.encodeToString<Report>(completedReport)

                async {
                    val reportId =
                        driveService.createFile(
                            "Report_${System.now().toEpochMilliseconds()}_${user.email}.txt",
                            jsonReport,
                            INCLUSIMAP_REPORT_FOLDER_ID,
                        )
                    if (reportId == null) {
                        _state.update {
                            it.copy(
                                isReported = false,
                                isError = true,
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(isReported = true)
                        }
                    }
                }.await()
            }.invokeOnCompletion {
                _state.update {
                    it.copy(
                        isReporting = false,
                    )
                }
            }
    }
}
