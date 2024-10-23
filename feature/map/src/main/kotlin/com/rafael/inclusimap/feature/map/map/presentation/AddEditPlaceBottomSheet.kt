package com.rafael.inclusimap.feature.map.map.presentation

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.AddRoad
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.PlaceCategory
import com.rafael.inclusimap.core.domain.model.icon
import com.rafael.inclusimap.core.domain.model.toAccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.toCategoryName
import com.rafael.inclusimap.core.domain.model.toPlaceCategory
import com.rafael.inclusimap.feature.map.map.presentation.dialog.AddNewPlaceConfirmationDialog
import com.rafael.inclusimap.feature.map.map.presentation.dialog.DeletePlaceConfirmationDialog
import com.rafael.inclusimap.feature.map.placedetails.domain.model.PlaceDetailsState
import java.util.Date
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalUuidApi::class)
@Composable
fun AddEditPlaceBottomSheet(
    latlng: LatLng,
    userEmail: String,
    placeDetailsState: PlaceDetailsState,
    bottomSheetScaffoldState: SheetState,
    isEditing: Boolean,
    onAddNewPlace: (AccessibleLocalMarker) -> Unit,
    onEditNewPlace: (AccessibleLocalMarker) -> Unit,
    onDeletePlace: (id: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    defaultRoundedShape: Shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
) {
    var placeName by remember { mutableStateOf(if (isEditing) placeDetailsState.currentPlace.title else "") }
    var placeAddress by remember { mutableStateOf(if (isEditing) placeDetailsState.currentPlace.address else "") }
    var placeLocatedIn by remember { mutableStateOf(if (isEditing) placeDetailsState.currentPlace.locatedIn else "") }
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var tryAddUpdate by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val minPlaceNameLength by remember { mutableIntStateOf(3) }
    val maxPlaceNameLength by remember { mutableIntStateOf(50) }
    val minPlaceAddressLength by remember { mutableIntStateOf(6) }
    val maxPlaceAddressLength by remember { mutableIntStateOf(60) }
    val minPlaceLocatedInLength by remember { mutableIntStateOf(6) }
    val maxPlaceLocatedInLength by remember { mutableIntStateOf(30) }
    var selectedPlaceCategory by remember { mutableStateOf(if (isEditing) placeDetailsState.currentPlace.category else null) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        sheetState = bottomSheetScaffoldState,
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imeNestedScroll()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row {
                Text(
                    text = if (isEditing) "Editar local:" else "Adicionar um novo local:",
                    fontSize = 24.sp,
                    modifier = Modifier.weight(1f),
                )
                if (isEditing) {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            showDeleteConfirmationDialog = true
                        },
                        modifier = Modifier.align(Alignment.CenterVertically),
                    ) {
                        Icon(
                            imageVector = Icons.TwoTone.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            TextField(
                value = placeName,
                onValueChange = {
                    if (it.length <= maxPlaceNameLength) {
                        placeName = it
                    }
                    tryAddUpdate = false
                },
                label = {
                    Text(text = "Digite o nome do local")
                },
                maxLines = 1,
                singleLine = true,
                isError = tryAddUpdate && (placeName.isEmpty() || placeName.length < minPlaceNameLength),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    Row {
                        Text(
                            text = "${placeName.length}",
                            fontSize = 14.sp,
                            color = if (placeName.length < minPlaceNameLength && placeName.isNotEmpty()) {
                                MaterialTheme.colorScheme.error
                            } else {
                                LocalContentColor.current
                            },
                        )
                        Text(
                            text = "/$maxPlaceNameLength",
                            fontSize = 14.sp,
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Words,
                ),
                modifier = Modifier
                    .fillMaxWidth(),
                shape = defaultRoundedShape,
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { isCategoryExpanded = true },
                shape = RoundedCornerShape(16.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = if (selectedPlaceCategory == null) Icons.Outlined.Category else selectedPlaceCategory!!.icon(),
                            contentDescription = null,
                        )
                        Text(
                            text = selectedPlaceCategory?.toCategoryName()
                                ?: "Selecione uma categoria",
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically),
                            fontWeight = if (selectedPlaceCategory == null) FontWeight.Normal else FontWeight.Bold,
                        )
                        IconButton(
                            onClick = {
                                isCategoryExpanded = true
                            },
                        ) {
                            Icon(
                                imageVector = Icons.TwoTone.KeyboardArrowUp,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(35.dp),
                            )
                        }
                    }
                }
            }
            TextField(
                value = placeAddress,
                onValueChange = {
                    if (it.length <= maxPlaceAddressLength) {
                        placeAddress = it
                    }
                    tryAddUpdate = false
                },
                label = {
                    Text(text = "Endereço")
                },
                maxLines = 1,
                singleLine = true,
                isError = tryAddUpdate && (placeAddress.isEmpty() || placeAddress.length < minPlaceAddressLength),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.AddRoad,
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    Row {
                        Text(
                            text = "${placeAddress.length}",
                            fontSize = 14.sp,
                            color = if (placeAddress.length < minPlaceAddressLength && placeAddress.isNotEmpty()) {
                                MaterialTheme.colorScheme.error
                            } else {
                                LocalContentColor.current
                            },
                        )
                        Text(
                            text = "/$maxPlaceAddressLength",
                            fontSize = 14.sp,
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Words,
                ),
                modifier = Modifier
                    .fillMaxWidth(),
                shape = defaultRoundedShape,
                placeholder = {
                    Text(text = "Ex: Av. Paulista, Bela Vista")
                },
            )
            TextField(
                value = placeLocatedIn,
                onValueChange = {
                    if (it.length <= maxPlaceLocatedInLength) {
                        placeLocatedIn = it
                    }
                    tryAddUpdate = false
                },
                label = {
                    Text(text = "Localizado em")
                },
                maxLines = 1,
                singleLine = true,
                isError = tryAddUpdate && (placeLocatedIn.isEmpty() || placeLocatedIn.length < minPlaceLocatedInLength),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.LocationCity,
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    Row {
                        Text(
                            text = "${placeLocatedIn.length}",
                            fontSize = 14.sp,
                            color = if (placeLocatedIn.length < minPlaceLocatedInLength && placeLocatedIn.isNotEmpty()) {
                                MaterialTheme.colorScheme.error
                            } else {
                                LocalContentColor.current
                            },
                        )
                        Text(
                            text = "/$maxPlaceLocatedInLength",
                            fontSize = 14.sp,
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Words,
                ),
                modifier = Modifier
                    .fillMaxWidth(),
                shape = defaultRoundedShape,
                placeholder = {
                    Text(text = "Ex: São Paulo, SP")
                },
            )
            Box(
                modifier = Modifier.align(Alignment.End),
            ) {
                DropdownMenu(
                    expanded = isCategoryExpanded,
                    onDismissRequest = { isCategoryExpanded = false },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(400.dp),
                ) {
                    PlaceCategory.entries.sortedBy {
                        it.toCategoryName()
                    }.map { it.toCategoryName().toPlaceCategory() }.forEach { placeCategory ->
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = placeCategory.icon(),
                                    contentDescription = null,
                                )
                            },
                            text = {
                                Text(
                                    text = placeCategory.toCategoryName(),
                                )
                            },
                            onClick = {
                                selectedPlaceCategory = placeCategory
                                isCategoryExpanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        tryAddUpdate = true
                        focusManager.clearFocus()
                        if (placeName.isEmpty() || placeAddress.isEmpty() || placeLocatedIn.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Preencha todos os campos!",
                                Toast.LENGTH_SHORT,
                            )
                                .show()
                            return@Button
                        }
                        if (selectedPlaceCategory == null) {
                            Toast.makeText(context, "Selecione uma categoria!", Toast.LENGTH_SHORT)
                                .show()
                            return@Button
                        }
                        if (placeName.length < minPlaceNameLength) {
                            Toast.makeText(
                                context,
                                "O nome do local deve ter pelo menos ${minPlaceNameLength} caracteres!",
                                Toast.LENGTH_SHORT,
                            ).show()
                            return@Button
                        }
                        if (placeAddress.length < minPlaceAddressLength) {
                            Toast.makeText(
                                context,
                                "O endereço do local deve ter pelo menos ${minPlaceAddressLength} caracteres!",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                        if (placeLocatedIn.length < minPlaceLocatedInLength) {
                            Toast.makeText(
                                context,
                                "A localização deve ter pelo menos ${minPlaceLocatedInLength} caracteres!",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                        if (isEditing) {
                            onEditNewPlace(
                                placeDetailsState.currentPlace.copy(
                                    title = placeName,
                                    category = selectedPlaceCategory,
                                    address = placeAddress,
                                ).toAccessibleLocalMarker(),
                            )
                            Toast.makeText(context, "Atualizando local...", Toast.LENGTH_SHORT)
                                .show()
                            onDismiss()
                        } else {
                            showConfirmationDialog = true
                        }
                    },
                ) {
                    Text(text = if (isEditing) "Atualizar" else "Adicionar")
                }
            }
        }
    }

    AnimatedVisibility(showDeleteConfirmationDialog) {
        DeletePlaceConfirmationDialog(
            onDismiss = {
                showDeleteConfirmationDialog = false
            },
            onDelete = {
                Toast.makeText(
                    context,
                    "Excluindo local...",
                    Toast.LENGTH_SHORT,
                ).show()
                onDeletePlace(placeDetailsState.currentPlace.id!!)
                showDeleteConfirmationDialog = false
                onDismiss()
            },
        )
    }

    AnimatedVisibility(showConfirmationDialog) {
        AddNewPlaceConfirmationDialog(
            onDismiss = {
                showConfirmationDialog = false
            },
            onConfirm = {
                onAddNewPlace(
                    AccessibleLocalMarker(
                        title = placeName,
                        category = selectedPlaceCategory,
                        position = latlng.latitude to latlng.longitude,
                        authorEmail = userEmail,
                        time = Date().toInstant().toString(),
                        id = Uuid.random().toString(),
                    ),
                )
                Toast.makeText(
                    context,
                    "Local adicionado com sucesso!",
                    Toast.LENGTH_SHORT,
                ).show()
                showConfirmationDialog = false
                onDismiss()
            },
        )
    }
}
