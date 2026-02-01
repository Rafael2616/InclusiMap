package com.rafael.inclusimap.feature.report.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafael.inclusimap.core.services.AwsFileApiService
import com.rafael.inclusimap.core.util.map.Constants.INCLUSIMAP_REPORT_FOLDER_ID
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
    private val awsService: AwsFileApiService,
) : ViewModel() {
    private val _state = MutableStateFlow(ReportState())
    val state = _state.asStateFlow()

    @OptIn(ExperimentalTime::class)
    fun onReport(report: Report) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(
                    isReported = false,
                    isReporting = true,
                    isError = false,
                )
            }
            val json =
                Json {
                    prettyPrint = true
                    encodeDefaults = true
                }
            val completedReport =
                report.copy(
                    reportedLocal =
                        report.reportedLocal.copy(
                            comments =
                                if (report.type == ReportType.COMMENT ||
                                    report.type == ReportType.OTHER
                                ) {
                                    report.reportedLocal.comments
                                } else emptyList(),
                        ),
                )
            val jsonReport = json.encodeToString<Report>(completedReport)

            async {
                val reportId =
                    awsService.uploadFile(
                        "$INCLUSIMAP_REPORT_FOLDER_ID/Report_${
                            System.now().toEpochMilliseconds()
                        }_${report.userEmail}.json",
                        jsonReport,
                    ).getOrNull()
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
                it.copy(isReporting = false)
            }
        }
    }
}
