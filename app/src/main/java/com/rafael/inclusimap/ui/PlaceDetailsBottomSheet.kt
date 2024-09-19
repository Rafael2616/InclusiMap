package com.rafael.inclusimap.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafael.inclusimap.R
import com.rafael.inclusimap.domain.AccessibleLocalMarker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetainsBottomSheet(
    localMarker: AccessibleLocalMarker,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottomSheetScaffoldState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        sheetState = bottomSheetScaffoldState,
        onDismissRequest = {
            scope.launch {
                bottomSheetScaffoldState.hide()
            }
            onDismiss()
        },
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = true,
        )
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = localMarker.title,
                fontSize = 22.sp,
            )
            Text(
                text = localMarker.description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )

            repeat(2) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth(),

                    ) {
                    items(3) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier
                                .height(160.dp)
                                .padding(6.dp)
                                .clip(RoundedCornerShape(24.dp))

                        )
                    }
                }
            }
        }
    }
}