package com.rafael.inclusimap.feature.auth.presentation.dialogs

import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafael.inclusimap.core.domain.network.InternetConnectionState
import com.rafael.inclusimap.core.domain.util.rotateImage
import com.rafael.inclusimap.feature.auth.domain.model.LoginEvent
import com.rafael.inclusimap.feature.auth.domain.model.LoginState

@Composable
fun ProfileSettingsDialog(
    loginState: LoginState,
    onEvent: (LoginEvent) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val orientation = LocalConfiguration.current.orientation
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
    var profilePicture by remember { mutableStateOf(loginState.userProfilePicture) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let {
            profilePicture = context.contentResolver.openInputStream(it)?.use { image ->
                val bitmap = BitmapFactory.decodeStream(image)
                val imageOrientation =
                    context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                        ExifInterface(fd.fileDescriptor).getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL,
                        )
                    } ?: ExifInterface.ORIENTATION_NORMAL

                rotateImage(bitmap, imageOrientation).asImageBitmap()
            } ?: return@let
        }
    }
    var allowOtherUsersToSeeProfilePictureOptedIn by remember {
        mutableStateOf(
            loginState.user?.showProfilePictureOptedIn ?: false,
        )
    }
    var newName by remember { mutableStateOf(loginState.user?.name ?: "") }
    var shouldDismissDialog by remember { mutableStateOf(false) }
    val internetState = remember { InternetConnectionState(context) }
    val isInternetAvailable by internetState.state.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    var editUserName by remember { mutableStateOf(false) }
    val isErrorUpdatingUserInfos by remember(loginState) {
        derivedStateOf {
            loginState.isErrorUpdatingUserName && loginState.isErrorUpdatingProfilePicture && loginState.isErrorRemovingProfilePicture && loginState.isErrorAllowingPictureOptedIn
        }
    }
    val isSuccessfulUpdatingUserInfos by remember(loginState) {
        derivedStateOf {
            loginState.isPictureOptedInSuccessfullyChanged && loginState.isUserNameUpdated && loginState.isProfilePictureUpdated && loginState.isProfilePictureRemoved
        }
    }
    Dialog(
        onDismissRequest = {
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        Card(
            modifier = modifier
                .statusBarsPadding()
                .navigationBarsPadding()
                .fillMaxWidth(if (isLandscape) 0.5f else 0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
            ),
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    Text(
                        text = "Configurações do perfil",
                        fontSize = 22.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Start,
                        softWrap = true,

                    )
                }
                profilePicture?.let { image ->
                    item {
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
                                },
                        )
                    }
                }
                if (profilePicture == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                                .clickable {
                                    launcher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly,
                                        ),
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddAPhoto,
                                contentDescription = "No profile picture",
                                modifier = Modifier.size(50.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                if (profilePicture != null) {
                    item {
                        IconButton(
                            onClick = {
                                profilePicture = null
                            },
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Remove",
                                modifier = Modifier.size(30.dp),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
                item {
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
                            checked = allowOtherUsersToSeeProfilePictureOptedIn,
                            onCheckedChange = {
                                allowOtherUsersToSeeProfilePictureOptedIn = it
                            },
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterHorizontally,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                            .padding(8.dp),
                    ) {
                        TextField(
                            value = newName,
                            onValueChange = {
                                if (it.length <= 30) {
                                    newName = it
                                }
                            },
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .weight(1f),
                            enabled = editUserName,
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
                                disabledIndicatorColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
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
                                Row {
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
                            },
                            isError = newName.length < 3 && newName.isNotEmpty() && editUserName,
                        )
                        IconButton(
                            onClick = {
                                if (!loginState.isUpdatingUserName) {
                                    editUserName = !editUserName
                                    return@IconButton
                                }
                                if (!editUserName) {
                                    newName = loginState.user?.name.orEmpty()
                                    focusRequester.freeFocus()
                                } else {
                                    focusRequester.requestFocus()
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
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                            .padding(8.dp)
                            .padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = "Conta",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 14.sp,
                        )
                        Text(
                            text = loginState.user?.email ?: "",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        )
                    }
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (isSuccessfulUpdatingUserInfos) {
                            OutlinedButton(
                                onClick = {
                                    onDismiss()
                                },
                                contentPadding = PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 8.dp,
                                ),
                            ) {
                                Text(
                                    text = "Cancelar",
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.height(45.dp),
                            ) {
                                Text(
                                    text = "Atualizando\ninformações...",
                                    maxLines = 2,
                                    lineHeight = 14.sp,
                                    fontSize = 12.sp,
                                )
                                CircularProgressIndicator(
                                    modifier = Modifier.size(30.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeCap = StrokeCap.Round,
                                )
                            }
                        }
                        if (profilePicture != loginState.userProfilePicture || loginState.user?.name != newName || loginState.user.showProfilePictureOptedIn != allowOtherUsersToSeeProfilePictureOptedIn) {
                            Button(
                                onClick = {
                                    if (profilePicture != loginState.userProfilePicture) {
                                        profilePicture?.let {
                                            onEvent(LoginEvent.OnAddEditUserProfilePicture(it))
                                        }
                                    }
                                    if (profilePicture == null) {
                                        onEvent(LoginEvent.OnRemoveUserProfilePicture)
                                    }
                                    if (newName != loginState.user?.name) {
                                        if (newName.length < 3) {
                                            Toast.makeText(
                                                context,
                                                "O nome deve ter no mínimo 3 caracteres",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                            return@Button
                                        }
                                        editUserName = false
                                        onEvent(LoginEvent.UpdateUserName(newName))
                                    }
                                    if (allowOtherUsersToSeeProfilePictureOptedIn != loginState.user?.showProfilePictureOptedIn) {
                                        onEvent(LoginEvent.OnAllowPictureOptedIn(allowOtherUsersToSeeProfilePictureOptedIn))
                                    }
                                    shouldDismissDialog = true
                                },
                                enabled = isSuccessfulUpdatingUserInfos && isInternetAvailable,
                            ) {
                                Text(
                                    text = "Atualizar",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (isErrorUpdatingUserInfos && !isSuccessfulUpdatingUserInfos) {
        Toast.makeText(
            context,
            "Erro ao atualizar informações",
            Toast.LENGTH_SHORT,
        ).show()
        shouldDismissDialog = false
    }
    if (isSuccessfulUpdatingUserInfos && shouldDismissDialog) {
        Toast.makeText(
            context,
            "Informações atualizadas!",
            Toast.LENGTH_SHORT,
        ).show()
        onDismiss()
    }
}
