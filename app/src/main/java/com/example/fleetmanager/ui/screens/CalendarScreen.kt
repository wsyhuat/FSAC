package com.example.fleetmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fleetmanager.ui.components.InfoRow
import com.example.fleetmanager.ui.components.SectionCard
import com.example.fleetmanager.viewmodel.AppViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(nav: NavController, vm: AppViewModel) {
    val activities by vm.activities.collectAsState(emptyList())
    val today = Calendar.getInstance()
    var year by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var month by remember { mutableIntStateOf(today.get(Calendar.MONTH)) } // 0-based
    var selectedDay by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH)) }

    val cal = Calendar.getInstance().apply { set(year, month, 1) }
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    // 表头为「一 二 三 四 五 六 日」，即周一开头：周一偏移 0，周日偏移 6
    val weekOffset = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7

    // 切换月份后，若当前选中日期超出该月天数，自动收敛到当月最后一天
    LaunchedEffect(year, month) {
        if (selectedDay > daysInMonth) selectedDay = daysInMonth
    }

    val dateKey = { d: Int -> "%04d-%02d-%02d".format(year, month + 1, d) }
    val eventsOn = { d: Int -> activities.filter { it.date == dateKey(d) } }

    Scaffold(topBar = {
        TopAppBar(title = { Text("赛程日历") }, navigationIcon = {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, "返回") }
        })
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = {
                    if (month == 0) { month = 11; year-- } else month--
                }) { Icon(Icons.Default.ChevronLeft, "上月") }
                Text("${year} 年 ${month + 1} 月", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = {
                    if (month == 11) { month = 0; year++ } else month++
                }) { Icon(Icons.Default.ChevronRight, "下月") }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                listOf("一", "二", "三", "四", "五", "六", "日").forEach {
                    Text(it, Modifier.weight(1f), textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            val cells = mutableListOf<Int?>().apply {
                repeat(weekOffset) { add(null) }
                for (d in 1..daysInMonth) add(d)
            }
            Column {
                for (rowStart in cells.indices step 7) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        for (col in 0..6) {
                            val idx = rowStart + col
                            val day = cells.getOrNull(idx)
                            Box(Modifier.weight(1f).height(48.dp).padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (day == selectedDay) MaterialTheme.colorScheme.primary
                                    else Color.Transparent)
                                .clickable { if (day != null) selectedDay = day },
                                contentAlignment = Alignment.Center) {
                                if (day != null) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("$day", color = if (day == selectedDay) Color.White
                                        else MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyMedium)
                                        if (eventsOn(day).isNotEmpty()) {
                                            Box(Modifier.size(6.dp).clip(RoundedCornerShape(50))
                                                .background(if (day == selectedDay) Color.White
                                                else MaterialTheme.colorScheme.primary))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            SectionCard("${month + 1} 月 ${selectedDay} 日 日程") {
                val list = eventsOn(selectedDay)
                if (list.isEmpty()) {
                    Text("当天暂无日程安排", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn {
                        items(list) { a ->
                            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(a.title, style = MaterialTheme.typography.titleMedium)
                                    InfoRow("类型", a.type)
                                    InfoRow("地点", a.location.ifEmpty { "-" })
                                    InfoRow("日期", a.date)
                                    TextButton(onClick = { nav.navigate("checkin/${a.id}") }) {
                                        Text("查看签到")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
