package com.sadsha.dnsvpn.ui.components.sdv.controls

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.sadsha.dnsvpn.R
import com.sadsha.dnsvpn.ui.theme.SdvColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SdvTopAppBar(
    title: String,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SdvColor.Primary
            )
        },
        navigationIcon = { navigationIcon?.invoke() },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SdvColor.Background,
            titleContentColor = SdvColor.Primary,
            actionIconContentColor = SdvColor.PrimaryContainer,
            navigationIconContentColor = SdvColor.PrimaryContainer
        )
    )
}

@Composable
fun SdvBackTopAppBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    SdvTopAppBar(
        title = title,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
            }
        },
        actions = actions
    )
}
