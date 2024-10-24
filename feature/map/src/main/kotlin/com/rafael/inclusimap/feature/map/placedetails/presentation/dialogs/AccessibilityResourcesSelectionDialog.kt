package com.rafael.inclusimap.feature.map.placedetails.presentation.dialogs

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rafael.inclusimap.core.domain.model.Resource
import com.rafael.inclusimap.core.domain.model.toResourceIcon
import com.rafael.inclusimap.feature.map.placedetails.domain.model.PlaceDetailsState

@Composable
fun AccessibilityResourcesSelectionDialog(
    state: PlaceDetailsState,
    onDismiss: () -> Unit,
    onUpdateAccessibilityResources: (List<Resource>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var placeAccessibilityResources by remember { mutableStateOf(state.currentPlace.resources.map { it.resource }) }
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TabRow(
                    selectedTabIndex = 0,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                ) {
                    Tab(
                        selected = true,
                        onClick = { },
                        text = {
                            Text(
                                text = "Recursos",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                    )
                }
                val missingResources = Resource.entries
                    .filter { it != Resource.NOT_APPLICABLE }
                    .filter {
                        !placeAccessibilityResources
                            .contains(it)
                    }
                val resources = Resource.entries
                    .filter { it != Resource.NOT_APPLICABLE }
                    .filter {
                        placeAccessibilityResources
                            .contains(it)
                    }

                (0..1).forEach {
                    val res = if (it == 0) resources else missingResources
                    Text(
                        text = if (it == 0) "Esse lugar possui:" else "Falta nesse lugar:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    )
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(150.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        res.forEach { resource ->
                            item {
                                AssistChip(
                                    label = {
                                        Text(text = resource.displayName)
                                    },
                                    onClick = {
                                        placeAccessibilityResources = if (it == 0) {
                                            placeAccessibilityResources - resource
                                        } else {
                                            placeAccessibilityResources + resource
                                        }
                                    },
                                    modifier = Modifier
                                        .height(55.dp)
                                        .animateItem(),
                                    enabled = true,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = resource.displayName.toResourceIcon(),
                                            contentDescription = null,
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = if (it == 0) Icons.Filled.Close else Icons.Outlined.ArrowUpward,
                                            contentDescription = null,
                                            tint = if (it == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                            if (resource !in state.currentPlace.resources.map { it.resource }) 4.dp else 24.dp,
                                        ),
                                    ),
                                )
                            }
                        }
                    }
                }
                if (placeAccessibilityResources != state.currentPlace.resources.map { it.resource }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        val changes = state.currentPlace.resources.map { it.resource }
                            .filter { it !in placeAccessibilityResources }.size
                        Text(
                            text = changes.toString() + if (changes == 1) " Alteração" else " Alterações",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                    ) {
                        Text(text = "Cancelar")
                    }
                    if (placeAccessibilityResources != state.currentPlace.resources.map { it.resource }) {
                        Button(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Atualizando...",
                                    Toast.LENGTH_SHORT,
                                ).show()
                                onUpdateAccessibilityResources(placeAccessibilityResources)
                                onDismiss()
                            },
                        ) {
                            Text(text = "Atualizar")
                        }
                    }
                }
            }
        }
    }
}
