package com.example.fleetmanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fleetmanager.data.AttendanceStat
import com.example.fleetmanager.data.BorrowStat
import com.example.fleetmanager.data.TaskStat
import com.example.fleetmanager.ui.components.*
import com.example.fleetmanager.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController, vm: AppViewModel) {
    val user by vm.currentUser.collectAsState()
    val taskStat by vm.taskStat.collectAsState(TaskStat(0, 0, 0, 0))
    val attStat by vm.attendanceStat.collectAsState(AttendanceStat(0, 0, 0, 0))
    val borrowStat by vm.borrowStat.collectAsState(BorrowStat(0, 0, 0))
    val deptStats by vm.deptStats.collectAsState(emptyList())
    val announcements by vm.announcements.collectAsState(emptyList())
    val members by vm.members.collectAsState(emptyList())
    val items by vm.items.collectAsState(emptyList())

    val completion = if (taskStat.total > 0) taskStat.completed.toFloat() / taskStat.total else 0f
    val attendanceRate = if (attStat.total > 0) attStat.present.toFloat() / attStat.total else 0f

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("首页 / 仪表盘") },
            actions = {
                IconButton(onClick = { nav.navigate("profile") }) {
                    Icon(Icons.Default.AccountCircle, "个人中心")
                }
            }
        )
    }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Text("你好，${user?.role?.let { if (it == "admin") "管理员" else "队员" } ?: ""}",
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("任务完成率", "${(completion * 100).toInt()}%", color = Color(0xFF1565C0),
                        modifier = Modifier.weight(1f))
                    StatCard("出勤率", "${(attendanceRate * 100).toInt()}%", color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f))
                    StatCard("在借/逾期", "${borrowStat.onLoan}/${borrowStat.overdue}", color = Color(0xFFEF6C00),
                        modifier = Modifier.weight(1f))
                }
            }
            item {
                SectionCard("统计概览") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        DonutChart(completion, "任务完成率", Color(0xFF1565C0))
                        DonutChart(attendanceRate, "出勤率", Color(0xFF2E7D32))
                    }
                    Divider(Modifier.padding(vertical = 8.dp))
                    Text("各部门队员数", style = MaterialTheme.typography.labelLarge)
                    BarChart(deptStats.map { it.deptName to it.memberCount.toFloat() },
                        color = Color(0xFF7B1FA2))
                }
            }
            item {
                SectionCard("快捷入口") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuickEntry("队员管理", Icons.Default.Groups, Modifier.weight(1f)) { nav.navigate("members") }
                        QuickEntry("任务管理", Icons.Default.Assignment, Modifier.weight(1f)) { nav.navigate("tasks") }
                        QuickEntry("活动签到", Icons.Default.FactCheck, Modifier.weight(1f)) { nav.navigate("activities") }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuickEntry("公告", Icons.Default.Campaign, Modifier.weight(1f)) { nav.navigate("announcements") }
                        QuickEntry("物资借还", Icons.Default.Inventory2, Modifier.weight(1f)) { nav.navigate("items") }
                        QuickEntry("赛程日历", Icons.Default.CalendarMonth, Modifier.weight(1f)) { nav.navigate("calendar") }
                    }
                }
            }
            item {
                SectionCard(
                    title = "置顶 / 最新公告",
                    actions = { TextButton(onClick = { nav.navigate("announcements") }) { Text("更多") } }
                ) {
                    if (announcements.isEmpty()) EmptyState("暂无公告")
                    else announcements.take(3).forEach { a ->
                        Row(
                            Modifier.fillMaxWidth().clickable { nav.navigate("announcement_detail/${a.id}") }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (a.pinned) StatusChip("置顶", Color(0xFFC62828))
                            Spacer(Modifier.width(8.dp))
                            Text(a.title, Modifier.weight(1f), maxLines = 1)
                            Icon(Icons.Default.ChevronRight, null)
                        }
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(14.dp)) {
                        Text("数据一览", style = MaterialTheme.typography.titleSmall)
                        InfoRow("队员总数", "${members.size} 人")
                        InfoRow("任务总数", "${taskStat.total} 项（完成 ${taskStat.completed}）")
                        InfoRow("物资种类", "${items.size} 种")
                        InfoRow("待办任务", "${taskStat.todo} 项")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickEntry(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
                       modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}
