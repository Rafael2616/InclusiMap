package com.rafael.inclusimap.feature.intro.presentation.dialogs

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings

@Suppress("ktlint:compose:modifier-not-used-at-root")
@Composable
fun TermsAndConditionsDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val orientation = LocalConfiguration.current.orientation
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current
    val termsAndServicesFile = remember {
        context.assets.open("TermsAndConditions.txt")
            .readBytes()
            .decodeToString()
    }
    val state = rememberLazyListState()
    val titles = remember {
        listOf(
            "Termos de serviço:",
            "Condições:",
        )
    }
    val keywords = remember {
        listOf(
            "1- Que informações coletamos?",
            "2- O que é feito com seus dados?",
            "3- Como garantimos sua privacidade?",
            "4- O que acontece quando você exclui sua conta?",
            "1- Ao entrar nesse serviço, você concorda que:",
            "2- Ao entrar nesse serviço, você se compromente a:",
        )
    }
    val termsAndServicesString = remember {
        buildAnnotatedString {
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
    }

    Dialog(
        onDismissRequest = {
            onDismissRequest()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Card(
            modifier = modifier
                .navigationBarsPadding()
                .statusBarsPadding()
                .height(560.dp)
                .fillMaxWidth(if (isLandscape) 0.5f else 0.85f)
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(560.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Termos e condições",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(top = 16.dp),
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(530.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                ) {
                    item {
                        LazyColumnScrollbar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(440.dp)
                                .padding(top = 10.dp),
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
                                    .height(440.dp)
                                    .padding(horizontal = 24.dp)
                                    .padding(top = 28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
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
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp, bottom = 16.dp),
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
        }
    }
}
