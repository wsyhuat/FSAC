package com.example.fleetmanager.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 分区卡片 */
@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                Row(content = actions)
            }
            Spacer(Modifier.height(12.dp))
            Column(content = content)
        }
    }
}

/** 首页统计卡片 */
@Composable
fun StatCard(title: String, value: String, unit: String = "", color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            Modifier.padding(14.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(title + if (unit.isNotEmpty()) "（$unit）" else "",
                style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        }
    }
}

/** 简易环形图（完成率 / 出勤率），无需第三方图表库 */
@Composable
fun DonutChart(
    percent: Float,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val safe = percent.coerceIn(0f, 1f)
    Box(modifier.size(120.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 16.dp.toPx()
            val inset = stroke / 2
            drawArc(
                color = trackColor, startAngle = 0f, sweepAngle = 360f,
                useCenter = false, topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke), style = Stroke(width = stroke)
            )
            drawArc(
                color = color, startAngle = -90f, sweepAngle = 360f * safe,
                useCenter = false, topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke), style = Stroke(width = stroke)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${(safe * 100).toInt()}%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

/** 简易条形图，用于各部门人数 / 各活动出勤 */
@Composable
fun BarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    maxValue: Float = data.maxOfOrNull { it.second }?.coerceAtLeast(1f) ?: 1f
) {
    if (data.isEmpty()) {
        Text("暂无数据", Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Row(
        modifier.fillMaxWidth().height(150.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (name, value) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text("${value.toInt()}", style = MaterialTheme.typography.labelSmall)
                Box(
                    Modifier
                        .width(24.dp)
                        .height((100f * value / maxValue).coerceAtLeast(2f).dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(color)
                )
                Text(name, style = MaterialTheme.typography.labelSmall,
                    maxLines = 1, textAlign = TextAlign.Center)
            }
        }
    }
}

/** 通用筛选 Chip（供各列表页复用） */
@Composable
fun FilterChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    androidx.compose.material3.FilterChip(
        selected = selected, onClick = onClick, label = { Text(label, maxLines = 1) },
        modifier = modifier
    )
}

/** 键值对信息行 */
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(label, Modifier.width(76.dp), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
    }
}

/** 空状态 */
@Composable
fun EmptyState(text: String = "暂无数据") {
    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** 删除确认对话框 */
@Composable
fun ConfirmDialog(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = { onConfirm(); onDismiss() }) {
                Text("删除", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

/** 带删除图标的图标按钮 */
@Composable
fun DeleteAction(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error)
    }
}

/** 状态标签 */
@Composable
fun StatusChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f),
        contentColor = color
    ) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall)
    }
}

fun statusColor(status: String): Color = when (status) {
    "完成", "出勤", "已还", "在队", "可用" -> Color(0xFF2E7D32)
    "进行中", "在借" -> Color(0xFF1565C0)
    "待办", "迟到", "维修" -> Color(0xFFEF6C00)
    "请假", "逾期", "离队", "报废" -> Color(0xFFC62828)
    else -> Color(0xFF616161)
}

fun priorityColor(priority: String): Color = when (priority) {
    "高" -> Color(0xFFC62828)
    "中" -> Color(0xFFEF6C00)
    else -> Color(0xFF2E7D32)
}
