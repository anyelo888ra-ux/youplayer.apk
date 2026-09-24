package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkDivider
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import com.example.util.LogEntry
import com.example.util.LogLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugLogSheet(
    logs: List<LogEntry>,
    onDismiss: () -> Unit,
    onClearLogs: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Diagnostics & Event Logs",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${logs.size} recorded events in ring buffer",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                TextButton(
                    onClick = onClearLogs,
                    colors = ButtonDefaults.textButtonColors(contentColor = YouTubeRed),
                    modifier = Modifier.testTag("clear_logs_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear", fontSize = 12.sp)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(DarkDivider)
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No logs recorded yet.", color = TextSecondary, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(logs) { log ->
                        LogItemView(log)
                    }
                }
            }
        }
    }
}

@Composable
fun LogItemView(log: LogEntry) {
    val levelColor = when (log.level) {
        LogLevel.DEBUG -> Color(0xFF38BDF8)
        LogLevel.INFO -> Color(0xFF4ADE80)
        LogLevel.WARN -> Color(0xFFFBBF24)
        LogLevel.ERROR -> Color(0xFFF87171)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceVariant, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = log.level.name,
                        color = levelColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "[${log.tag}]",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = log.formattedTime,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = log.message,
                color = TextPrimary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            if (log.throwable != null) {
                Text(
                    text = log.throwable.stackTraceToString().take(300),
                    color = Color(0xFFF87171),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
