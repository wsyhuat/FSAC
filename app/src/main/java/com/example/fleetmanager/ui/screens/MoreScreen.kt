package com.example.fleetmanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fleetmanager.ui.components.InfoRow
import com.example.fleetmanager.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(nav: NavController, vm: AppViewModel) {
    Scaffold(topBar = { TopAppBar(title = { Text("更多功能") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            MoreItem("活动 / 签到", Icons.Default.FactCheck, "组织活动、手动签到") { nav.navigate("activities") }
            MoreItem("考勤统计", Icons.Default.BarChart, "出勤率与签到明细") { nav.navigate("attendance") }
            MoreItem("公告管理", Icons.Default.Campaign, "发布、列表、详情、置顶") { nav.navigate("announcements") }
            MoreItem("物资管理", Icons.Default.Inventory2, "物品借出、归还、逾期") { nav.navigate("items") }
            MoreItem("借还记录", Icons.Default.History, "查看借还与归还状态") { nav.navigate("borrows") }
            MoreItem("赛程日历", Icons.Default.CalendarMonth, "月历 + 日程列表") { nav.navigate("calendar") }
            MoreItem("个人中心", Icons.Default.Person, "账号信息与退出登录") { nav.navigate("profile") }
            Spacer(Modifier.height(12.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(14.dp)) {
                    Text("课程版说明", style = MaterialTheme.typography.titleSmall)
                    InfoRow("数据", "本地 Room（SQLite）")
                    InfoRow("权限", "本地角色判断（管理员/队员）")
                    InfoRow("图表", "Compose 自绘，无外部服务依赖")
                }
            }
        }
    }
}

@Composable
private fun MoreItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
                     desc: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(desc, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null)
        }
    }
}
