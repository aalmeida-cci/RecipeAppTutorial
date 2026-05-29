package com.adrian.recipeapp.features.profile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adrian.recipeapp.features.language.ui.LanguageBottomSheet
import com.adrian.recipeapp.features.language.ui.LanguageUiState
import com.adrian.recipeapp.features.language.ui.LanguageViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import recipeapp.composeapp.generated.resources.Res
import recipeapp.composeapp.generated.resources.connect_bluetooth
import recipeapp.composeapp.generated.resources.language
import recipeapp.composeapp.generated.resources.logout
import recipeapp.composeapp.generated.resources.notification
import recipeapp.composeapp.generated.resources.profile_dummy
import recipeapp.composeapp.generated.resources.profile_title
import recipeapp.composeapp.generated.resources.qr_code_generation
import recipeapp.composeapp.generated.resources.qr_code_scan

@Composable
fun ProfileRoute(modifier: Modifier = Modifier, viewModel: ProfileViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val langViewModel: LanguageViewModel = koinViewModel()
    val langState by langViewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(
        uiState = uiState,
        langState = langState,
        onLanguageRowTap = langViewModel::onLanguageRowTapped,
        modifier = modifier
    )

    if (langState.isBottomSheetVisible) {
        LanguageBottomSheet(
            uiState = langState,
            onLanguageSelected = langViewModel::onLanguageSelected,
            onApply = langViewModel::onApply,
            onDismiss = langViewModel::onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    langState: LanguageUiState,
    onLanguageRowTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                colors =
                TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = { Text(stringResource(Res.string.profile_title)) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier =
            Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
        ) {
            HorizontalDivider(
                thickness = 0.3.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )

            Spacer(Modifier.height(24.dp))

            ProfileAvatar(modifier = Modifier.align(Alignment.CenterHorizontally))

            Text(
                text = uiState.email,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp)
            )

            Spacer(Modifier.height(24.dp))

            SettingsGroup(
                onLanguageRowTap = onLanguageRowTap
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileAvatar(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(Res.drawable.profile_dummy),
            contentDescription = null,
            modifier =
            Modifier.size(120.dp).clip(CircleShape).border(
                0.3.dp,
                MaterialTheme.colorScheme.outline.copy(
                    alpha = 0.5f
                ),
                CircleShape
            ).background(MaterialTheme.colorScheme.outline),
            contentScale = ContentScale.Crop
        )
        SmallFloatingActionButton(
            onClick = {},
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = null
            )
        }
    }
}

private data class SettingsRowItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit = {}
)

@Composable
private fun SettingsGroup(onLanguageRowTap: () -> Unit, modifier: Modifier = Modifier) {
    val rows = listOf(
        SettingsRowItem(Icons.Default.QrCodeScanner, stringResource(Res.string.qr_code_scan)),
        SettingsRowItem(Icons.Default.QrCode2, stringResource(Res.string.qr_code_generation)),
        SettingsRowItem(Icons.Default.Bluetooth, stringResource(Res.string.connect_bluetooth)),
        SettingsRowItem(Icons.Default.Notifications, stringResource(Res.string.notification)),
        SettingsRowItem(
            Icons.Default.Language,
            stringResource(Res.string.language),
            onLanguageRowTap
        ),
        SettingsRowItem(Icons.AutoMirrored.Filled.Logout, stringResource(Res.string.logout))
    )
    Surface(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column {
            rows.forEachIndexed { index, item ->
                SettingsRow(item = item)
                if (index < rows.lastIndex) {
                    HorizontalDivider(
                        thickness = 0.3.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(item: SettingsRowItem, modifier: Modifier = Modifier) {
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}
