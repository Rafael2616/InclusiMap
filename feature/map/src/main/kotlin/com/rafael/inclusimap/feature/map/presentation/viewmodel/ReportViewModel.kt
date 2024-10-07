package com.rafael.inclusimap.feature.map.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafael.inclusimap.core.domain.util.Constants.INCLUSIMAP_REPORT_FOLDER_ID
import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.feature.auth.domain.model.User
import com.rafael.inclusimap.feature.auth.domain.repository.LoginRepository
import com.rafael.inclusimap.feature.map.domain.Report
import com.rafael.inclusimap.feature.map.domain.ReportType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ReportViewModel(
    private val loginRepository: LoginRepository,
    private val driveService: GoogleDriveService,
) : ViewModel() {

    fun onReport(report: Report) {
        viewModelScope.launch(Dispatchers.IO) {
            val loginData = loginRepository.getLoginInfo(1)!!
            val user = User(
                name = loginData.userName!!,
                email = loginData.userEmail!!,
                password = loginData.userPassword!!,
                id = loginData.userId!!,
            )
            val json = Json {
                prettyPrint = true
                encodeDefaults = true
            }
            val completedReport = report.copy(
                user = user,
                reportedLocal = report.reportedLocal.copy(
                    comments = if (report.type == ReportType.COMMENT) report.reportedLocal.comments else emptyList()
                )
            )
            val jsonReport = json.encodeToString<Report>(completedReport)

            async {
                driveService.createFile(
                    "Report_${System.currentTimeMillis()}_${user.name}.txt",
                    jsonReport,
                    INCLUSIMAP_REPORT_FOLDER_ID,
                )
            }.await()
        }.invokeOnCompletion {
            if (it != null) {
                println("Error sending report: $it")
            } else {
                println("Report sent!")
            }
        }
    }
}
