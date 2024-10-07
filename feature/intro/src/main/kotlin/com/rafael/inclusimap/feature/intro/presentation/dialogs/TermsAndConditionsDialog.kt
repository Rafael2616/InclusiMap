package com.rafael.inclusimap.feature.intro.presentation.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings

@Suppress("ktlint:compose:modifier-not-used-at-root")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val termsAndServicesFile = context.assets.open("TermsAndConditions.txt")
        .readBytes()
        .decodeToString()
    val state = rememberLazyListState()
    val titles = listOf(
        "Termos de serviço:",
        "Condições:",
    )
    val keywords = listOf(
        "1- Que informações coletamos?",
        "2- O que é feito com seus dados?",
        "3- Como garantimos sua privacidade?",
        "4- O que acontece quando eu excluo minha conta?",
        "1- Ao entrar nesse serviço, você concorda que:",
        "2- Ao entrar nesse serviço, você se compromente a:",
    )
    val termsAndServicesString = buildAnnotatedString {
        val termsAndServicesFileLowercase = termsAndServicesFile.lowercase()
        append(termsAndServicesFile)
        titles.forEach { title ->
            val titleLowercase = title.lowercase()
            var index = termsAndServicesFileLowercase.indexOf(titleLowercase)
            while (index != -1) {
                addStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                    ),
                    start = index,
                    end = index + title.length,
                )
                index = termsAndServicesFileLowercase.indexOf(titleLowercase, index + 1)
            }
        }
        keywords.forEach { keyword ->
            val keywordLowercase = keyword.lowercase()
            var index = termsAndServicesFileLowercase.indexOf(keywordLowercase)
            while (index != -1) {
                addStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    ),
                    start = index,
                    end = index + keyword.length,
                )
                index = termsAndServicesFileLowercase.indexOf(keywordLowercase, index + 1)
            }
        }
    }
    BasicAlertDialog(
        onDismissRequest = {
            onDismissRequest()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Card(
            modifier = modifier
                .height(560.dp)
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Termos e condições",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),

                )
                LazyColumnScrollbar(
                    modifier = Modifier
                        .padding(top = 35.dp),
                    state = state,
                    settings = ScrollbarSettings(
                        scrollbarPadding = 10.dp,
                        thumbUnselectedColor = MaterialTheme.colorScheme.primary,
                        thumbSelectedColor = MaterialTheme.colorScheme.primary,
                        thumbMinLength = 0.05f,
                        thumbMaxLength = 0.3f,
                        thumbThickness = 6.dp,
                    ),
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .height(500.dp)
                            .padding(horizontal = 24.dp)
                            .padding(top = 24.dp, bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        state = state,
                    ) {
                        item {
                            Text(
                                text = termsAndServicesString,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                textAlign = TextAlign.Justify,
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        onDismissRequest()
                    },
                ) {
                    Text(text = "Entendi")
                }
            }
        }
    }
}
