package com.rafael.tictactoe.feature.libraryinfo.presentation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafael.tictactoe.feature.libraryinfo.domain.model.OssLibrary
import com.rafael.tictactoe.feature.libraryinfo.presentation.utils.annotatedLink

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LibraryScreen(
    libraries: List<OssLibrary>,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var isGoBackClicked by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text("Liçenças de código aberto")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!isGoBackClicked) {
                                isGoBackClicked = true
                                popBackStack()
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back",
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(innerPadding),
        ) {
            itemsIndexed(libraries) { index, library ->
                val licenseLink = (library.spdxLicenses ?: library.unknownLicenses)?.firstOrNull()?.url.orEmpty()
                val annotatedLicenseLink = annotatedLink(licenseLink)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = library.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "v" + library.version,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = annotatedLicenseLink,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                        ),
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectTapGestures { pos ->
                                    layoutResult.value?.let { layoutResult ->
                                        val position = layoutResult.getOffsetForPosition(pos)
                                        val aLicenseLink = annotatedLicenseLink.getStringAnnotations(position, position).firstOrNull()
                                        if (aLicenseLink?.tag == "URL") {
                                            uriHandler.openUri(aLicenseLink.item)
                                        }
                                    }
                                }
                            },
                        onTextLayout = {
                            layoutResult.value = it
                        },
                    )
                }
                if (index < (libraries.size - 1)) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = Color.Gray,
                    )
                }
            }
        }
    }
}
