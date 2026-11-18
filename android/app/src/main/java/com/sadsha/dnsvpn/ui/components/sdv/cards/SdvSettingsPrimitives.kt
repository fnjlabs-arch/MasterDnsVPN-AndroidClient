package com.sadsha.dnsvpn.ui.components.sdv.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.sadsha.dnsvpn.R
import com.sadsha.dnsvpn.ui.theme.SdvColor
import com.sadsha.dnsvpn.ui.theme.SdvSpace

@Composable
fun SdvSectionCard(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onToggle,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SdvColor.SurfaceHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SdvSpace.S3, vertical = SdvSpace.S2),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = SdvColor.PrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (expanded) {
                    stringResource(R.string.action_hide)
                } else {
                    stringResource(R.string.action_show)
                },
                color = SdvColor.Primary
            )
        }
    }
}

@Composable
fun SdvSettingFieldCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SdvColor.SurfaceHigh)
    ) {
        Column(modifier = Modifier.padding(SdvSpace.S3)) {
            content()
            HorizontalDivider(
                modifier = Modifier.padding(top = SdvSpace.S2),
                color = SdvColor.OutlineVariant.copy(alpha = 0.3f)
            )
        }
    }
}
