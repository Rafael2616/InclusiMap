package com.rafael.tictactoe.feature.libraryinfo.presentation.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun annotatedLink(textLink: String): AnnotatedString = buildAnnotatedString {
    append(textLink)

    val urlPattern = Regex("(http|https)://\\S+")
    val matches = urlPattern.findAll(textLink)

    matches.forEach { matchResult ->
        val start = matchResult.range.first
        val end = matchResult.range.last + 1
        val url = matchResult.value

        addStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
            ),
            start = start,
            end = end,
        )
        addStringAnnotation(
            tag = "URL",
            annotation = url,
            start = start,
            end = end,
        )
    }
}
