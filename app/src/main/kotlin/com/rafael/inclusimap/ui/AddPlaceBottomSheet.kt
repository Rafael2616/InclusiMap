package com.rafael.inclusimap.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerState
import com.rafael.inclusimap.domain.AccessibleLocalMarker
import com.rafael.inclusimap.domain.LoginState
import java.util.Date
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalUuidApi::class)
@Composable
fun AddPlaceBottomSheet(
    latLng: LatLng,
    loginState: LoginState,
    bottomSheetScaffoldState: SheetState,
    onAddNewPlace: (AccessibleLocalMarker) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var placeName by remember { mutableStateOf("") }
    var placeCategory by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    ModalBottomSheet(
        sheetState = bottomSheetScaffoldState,
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imeNestedScroll()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Adicionar um novo local:",
                fontSize = 24.sp,
            )
            TextField(
                value = placeName,
                onValueChange = {
                    placeName = it
                },
                label = {
                    Text(text = "Digite o nome do local")
                },
                maxLines = 1,
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )
            TextField(
                value = placeCategory,
                onValueChange = {
                    placeCategory = it
                },
                label = {
                    Text(text = "Qual a categoria desse lugar?")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        onAddNewPlace(
                            AccessibleLocalMarker(
                                title = placeName,
                                category = placeCategory,
                                position = latLng.latitude to latLng.longitude,
                                author = loginState.user!!.name,
                                time = Date().toInstant().toString(),
                                id = Uuid.random().toString()
                            )
                        )
                        onDismiss()
                    }
                ) {
                    Text(text = "Adicionar")
                }
            }
        }
    }
}