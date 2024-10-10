package com.rafael.inclusimap.feature.settings.presentation

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.twotone.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rafael.inclusimap.core.settings.domain.model.SettingsState

@Composable
fun ProfileSettingsDialog(
    onDismiss: () -> Unit,
    onAddUpdatePicture: (ImageBitmap) -> Unit,
    onRemovePicture: () -> Unit,
    userName: String,
    allowOtherUsersToSeeProfilePicture: Boolean,
    onEditUserName: (String) -> Unit,
    onAllowPictureOptedIn: (Boolean) -> Unit,
    state: SettingsState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var profilePicture by remember { mutableStateOf(state.profilePicture) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let {
            profilePicture =
                context.contentResolver.openInputStream(it)?.use { image ->
                    BitmapFactory.decodeStream(image).asImageBitmap()
                } ?: return@let
        }
    }
    var allowOtherUsersToSeeProfilePictureOptedId by remember {
        mutableStateOf(
            allowOtherUsersToSeeProfilePicture,
        )
    }
    var isPictureEdited by remember { mutableStateOf(false) }
    var isPictureRemoved by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(userName) }
    var isUserNameEdited by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp),
            ),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                profilePicture?.let { image ->
                    Image(
                        bitmap = image,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .clickable {
                                launcher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly,
                                    ),
                                )
                                isPictureRemoved = false
                                isPictureEdited = true
                            },
                    )
                }
                if (profilePicture == null) {
                    IconButton(
                        onClick = {
                            launcher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly,
                                ),
                            )
                            isPictureRemoved = false
                            isPictureEdited = true
                        },
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape),
                    ) {
                        Icon(
                            imageVector = Icons.TwoTone.AddAPhoto,
                            contentDescription = "No profile picture",
                            modifier = Modifier.size(80.dp),
                        )
                    }
                }
                if (profilePicture != null)
                    IconButton(
                        onClick = {
                            profilePicture = null
                            isPictureRemoved = true
                            isPictureEdited = false
                        },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor =  MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Remove",
                            modifier = Modifier.size(30.dp),
                        )
                    }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                        .padding(8.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Permitir que outros usuários vejam sua foto de perfil?",
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                    )
                    Checkbox(
                        checked = allowOtherUsersToSeeProfilePictureOptedId,
                        onCheckedChange = {
                            allowOtherUsersToSeeProfilePictureOptedId = it
                        },
                        modifier = Modifier.size(24.dp),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        8.dp,
                        Alignment.CenterHorizontally,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
                        .padding(8.dp)
                        .padding(horizontal = 4.dp),
                ) {
                    TextField(
                        value = newName,
                        onValueChange = {
                            if (it.length <= 30) {
                                newName = it
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isUserNameEdited,
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = true,
                            capitalization = KeyboardCapitalization.Words,
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                2.dp,
                            ),
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                2.dp,
                            ),
                        ),
                        label = {
                            Text(
                                text = "Nome",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                            )
                        },
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(16.dp),
                        trailingIcon = {
                            Row{
                                Text(
                                    text = newName.length.toString(),
                                    fontSize = 10.sp,
                                    color = if (newName.length < 3 && newName.isNotEmpty()) MaterialTheme.colorScheme.error else LocalContentColor.current,
                                )
                                Text(
                                    text = "/30",
                                    fontSize = 10.sp,
                                )
                            }
                        }
                    )
                    IconButton(
                        onClick = {
                            isUserNameEdited = !isUserNameEdited
                            if (!isUserNameEdited) {
                                newName = userName
                            }
                        },
                        modifier = Modifier.size(35.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                        },
                    ) {
                        Text(
                            text = "Cancelar",
                        )
                    }
                    Button(
                        onClick = {
                            if (isPictureEdited) {
                                onAddUpdatePicture(profilePicture!!)
                            }
                            if (isPictureRemoved) {
                                onRemovePicture()
                            }
                            if (newName != userName) {
                                if (newName.length < 3) {
                                    Toast.makeText(
                                        context,
                                        "O nome deve ter no mínimo 3 caracteres",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                                onEditUserName(newName)
                            }
                            if (allowOtherUsersToSeeProfilePictureOptedId != allowOtherUsersToSeeProfilePicture) {
                                onAllowPictureOptedIn(allowOtherUsersToSeeProfilePictureOptedId)
                            }
                            onDismiss()
                        },
                    ) {
                        Text(
                            text = "Salvar",
                        )
                    }
                }
            }
        }
    }
}
