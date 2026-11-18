package com.sadsha.dnsvpn.ui.components.sdv.controls

import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sadsha.dnsvpn.ui.theme.SdvColor

@Composable
fun SdvFilterChip(
    selected: Boolean,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        modifier = modifier,
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = SdvColor.OutlineVariant,
            selectedBorderColor = SdvColor.Primary,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp
        ),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SdvColor.PrimaryContainer.copy(alpha = 0.22f),
            selectedLabelColor = SdvColor.Primary,
            selectedLeadingIconColor = SdvColor.Primary,
            containerColor = SdvColor.SurfaceHigh,
            labelColor = SdvColor.OnSurfaceVariant,
            iconColor = SdvColor.OnSurfaceVariant
        )
    )
}

