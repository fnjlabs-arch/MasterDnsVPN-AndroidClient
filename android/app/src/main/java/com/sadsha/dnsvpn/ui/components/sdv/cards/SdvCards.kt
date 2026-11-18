package com.sadsha.dnsvpn.ui.components.sdv.cards

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.sadsha.dnsvpn.ui.theme.SdvColor
import com.sadsha.dnsvpn.ui.theme.SdvRadius

@Composable
fun SdvCardLow(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(SdvRadius.Lg),
            colors = CardDefaults.cardColors(containerColor = SdvColor.SurfaceLow),
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(SdvRadius.Lg),
            colors = CardDefaults.cardColors(containerColor = SdvColor.SurfaceLow),
            content = content
        )
    }
}

@Composable
fun SdvCardHigh(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(SdvRadius.Xl),
            colors = CardDefaults.cardColors(containerColor = SdvColor.SurfaceHigh),
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(SdvRadius.Xl),
            colors = CardDefaults.cardColors(containerColor = SdvColor.SurfaceHigh),
            content = content
        )
    }
}
