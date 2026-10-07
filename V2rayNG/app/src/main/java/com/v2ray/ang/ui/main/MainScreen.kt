package com.v2ray.ang.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R
import com.v2ray.ang.ui.compose.LocalDarkTheme
import com.v2ray.ang.ui.compose.colorPing

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onAction: (MainAction) -> Unit,
    @Suppress("UNUSED_PARAMETER") onNavigate: (MainDestination) -> Unit = {},
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val isRunning = uiState.isRunning
    val isDarkTheme = LocalDarkTheme.current
    val displayText = mainViewModel.formatStatus(uiState.status)

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 1.18f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isRunning) 0.35f else 0f,
        targetValue = if (isRunning) 0.05f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val buttonColor by animateColorAsState(
        targetValue = if (isRunning) {
            colorPing
        } else {
            if (isDarkTheme) Color(0xFF23272F) else Color(0xFFEDF2F7)
        },
        animationSpec = tween(durationMillis = 400),
        label = "buttonColor"
    )

    val buttonBorderColor by animateColorAsState(
        targetValue = if (isRunning) {
            Color(0xFF00C853)
        } else {
            if (isDarkTheme) Color(0xFF374151) else Color(0xFFCBD5E1)
        },
        animationSpec = tween(durationMillis = 400),
        label = "buttonBorderColor"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isRunning) {
            Color.White
        } else {
            if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF64748B)
        },
        animationSpec = tween(durationMillis = 400),
        label = "iconTint"
    )

    val statusText = when {
        uiState.isTesting -> stringResource(R.string.connection_test_testing)
        isRunning -> stringResource(R.string.toast_services_success)
        else -> stringResource(R.string.connection_not_connected)
    }

    val actionDesc = stringResource(if (isRunning) R.string.acc_stop else R.string.acc_start)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pulse Ring and Power Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                if (isRunning) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(colorPing.copy(alpha = pulseAlpha))
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(140.dp)
                        .shadow(
                            elevation = if (isRunning) 16.dp else 4.dp,
                            shape = CircleShape,
                            clip = false
                        )
                        .clip(CircleShape)
                        .background(buttonColor)
                        .border(width = 3.dp, color = buttonBorderColor, shape = CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClick = { onAction(MainAction.ToggleService) }
                        )
                        .semantics {
                            contentDescription = actionDesc
                        }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_power_24dp),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Connection Status Label
            Text(
                text = statusText,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = if (isRunning) {
                    if (isDarkTheme) Color(0xFF69F0AE) else Color(0xFF00C853)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle / Latency details (interactive latency ping when connected)
            if (isRunning) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onAction(MainAction.TestCurrentServer) }
                ) {
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
