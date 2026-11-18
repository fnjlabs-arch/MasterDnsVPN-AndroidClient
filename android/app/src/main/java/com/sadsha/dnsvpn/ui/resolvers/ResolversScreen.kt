package com.sadsha.dnsvpn.ui.resolvers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sadsha.dnsvpn.R
import com.sadsha.dnsvpn.ui.components.sdv.cards.SdvCardLow
import com.sadsha.dnsvpn.ui.components.sdv.controls.SdvTopAppBar
import com.sadsha.dnsvpn.ui.theme.ConnectedGreen
import com.sadsha.dnsvpn.ui.theme.SdvColor
import com.sadsha.dnsvpn.ui.theme.SdvSpace
import com.sadsha.dnsvpn.util.VpnManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResolversScreen() {
    val activeResolvers by VpnManager.activeResolvers.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SdvColor.Background)
    ) {
        SdvTopAppBar(
            title = stringResource(R.string.title_resolvers),
            actions = {
                IconButton(onClick = {
                    val textToCopy = activeResolvers.joinToString("\n")
                    if (textToCopy.isNotBlank()) {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(context.getString(R.string.title_resolvers), textToCopy)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, context.getString(R.string.resolvers_copied, activeResolvers.size), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, context.getString(R.string.resolvers_none), Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.resolvers_copy_desc), tint = SdvColor.PrimaryContainer)
                }
                IconButton(onClick = {
                    val textToShare = activeResolvers.joinToString("\n")
                    if (textToShare.isNotBlank()) {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, textToShare)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    } else {
                        Toast.makeText(context, context.getString(R.string.resolvers_none_to_share), Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.resolvers_share_desc), tint = SdvColor.PrimaryContainer)
                }
            }
        )

        if (activeResolvers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.resolvers_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = SdvColor.OnSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = SdvSpace.S4),
                contentPadding = PaddingValues(bottom = SdvSpace.S6),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activeResolvers, key = { it }, contentType = { "resolver" }) { resolver ->
                    ResolverItem(resolver)
                }
            }
        }
    }
}

@Composable
fun ResolverItem(resolver: String) {
    SdvCardLow(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(ConnectedGreen, androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = resolver,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = SdvColor.OnSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
