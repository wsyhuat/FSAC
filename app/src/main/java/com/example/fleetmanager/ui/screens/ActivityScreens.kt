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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fleetmanager.data.ActivityEntity
import com.example.fleetmanager.data.AttendanceStat
import com.example.fleetmanager.ui.components.*
import com.example.fleetmanager.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityListScreen(nav: NavController, vm: AppViewModel) {
    val activities by vm.activities.collectAsState(emptyList())
    val attendances by vm.attendances.collectAsState(emptyList())
    var showAdd by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("训练") }
    var toDelete by remember { mutableStateOf<ActivityEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("活动 / 签到") }, actions = {
                IconButton(onClick = { nav.navigate("attendance") }) {
                    Icon(Icons.Default.BarChart, "考勤统计")
                }
            })
        },
        floatingActionButton = {
            if (vm.isAdmin) FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, "新增活动")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            InfoRow("说明", "活动被考勤签到与赛程日历共用；签到采用手动方式，不做 GPS/人脸")
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(activities) { a ->
                    val count = attendances.count { it.activityId == a.id }
                    Card(Modifier.fillMaxWidth().clickable { nav.navigate("checkin/${a.id}") }) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer) {
                                Icon(Icons.Default.Event, null, Modifier.padding(8.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(a.title, style = MaterialTheme.typography.titleMedium)
                                Text("${a.date} · ${a.location} · ${a.type} · 已签 $count 人",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(onClick = { nav.navigate("checkin/${a.id}") }) { Text("签到") }
                            if (vm.isAdmin) DeleteAction { toDelete = a }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("新增活动") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it },
                        label = { Text("活动名称") }, singleLine = true)
                    OutlinedTextField(value = date, onValueChange = { date = it },
                        label = { Text("日期") }, singleLine = true, placeholder = { Text("yyyy-MM-dd") })
                    OutlinedTextField(value = location, onValueChange = { location = it },
                        label = { Text("地点") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("训练", "比赛", "会议", "其他").forEach { t ->
                            FilterChip(t, type == t, Modifier.weight(1f)) { type = t }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        vm.saveActivity(ActivityEntity(title = title.trim(), date = date.trim(),
                            location = location.trim(), type = type, createdBy = null))
                        title = ""; date = ""; location = ""; showAdd = false
                    }
                }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("取消") } }
        )
    }
    toDelete?.let {
        ConfirmDialog("删除活动", "确定删除活动「${it.title}」及相关签到记录？",
            onConfirm = { vm.deleteActivity(it) }, onDismiss = { toDelete = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(nav: NavController, vm: AppViewModel, activityId: Long) {
    val activities by vm.activities.collectAsState(emptyList())
    val members by vm.members.collectAsState(emptyList())
    val records by vm.attendances.collectAsState(emptyList())
    val activity = activities.firstOrNull { it.id == activityId }
    var memberId by remember { mutableStateOf(0L) }
    var status by remember { mutableStateOf("出勤") }
    var remark by remember { mutableStateOf("") }
    var toast by remember { mutableStateOf("") }

    LaunchedEffect(members) {
        if (memberId == 0L) memberId = members.firstOrNull()?.id ?: 0L
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("签到：${activity?.title ?: "活动"}") },
            navigationIcon = {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, "返回") }
            })
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoRow("活动", activity?.title ?: "-")
                    InfoRow("时间", activity?.date ?: "-")
                    InfoRow("地点", activity?.location ?: "-")
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = members.firstOrNull { it.id == memberId }?.name ?: "请选择队员",
                            onValueChange = {}, readOnly = true, label = { Text("签到队员") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            members.forEach { m ->
                                DropdownMenuItem(text = { Text("${m.name}（${m.studentNo}）") }, onClick = {
                                    memberId = m.id; expanded = false
                                })
                            }
                        }
                    }
                    Text("出勤状态", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("出勤", "迟到", "请假", "缺勤").forEach { s ->
                            FilterChip(s, status == s, Modifier.weight(1f)) { status = s }
                        }
                    }
                    OutlinedTextField(value = remark, onValueChange = { remark = it },
                        label = { Text("备注（可选）") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Button(onClick = {
                        if (memberId != 0L) {
                            vm.checkIn(activityId, memberId, status, remark) { _ ->
                                toast = "签到成功：$status"
                            }
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("提交签到") }
                    if (toast.isNotEmpty()) {
                        Text(toast, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            SectionCard("已签到名单（${records.count { it.activityId == activityId }}）") {
                val list = records.filter { it.activityId == activityId }
                if (list.isEmpty()) EmptyState("暂无签到记录")
                else list.forEach { r ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(members.firstOrNull { it.id == r.memberId }?.name ?: "未知",
                            Modifier.weight(1f))
                        StatusChip(r.status, statusColor(r.status))
                        Text(r.checkTime, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        DeleteAction { vm.deleteAttendance(r) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(nav: NavController, vm: AppViewModel) {
    val stat by vm.attendanceStat.collectAsState(AttendanceStat(0, 0, 0, 0))
    val actStats by vm.perActivityStats.collectAsState(emptyList())
    val members by vm.members.collectAsState(emptyList())

    Scaffold(topBar = {
        TopAppBar(title = { Text("考勤统计") },
            navigationIcon = {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, "返回") }
            })
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionCard("总体出勤率") {
                val rate = if (stat.total > 0) stat.present.toFloat() / stat.total else 0f
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DonutChart(rate, "出勤率", MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        InfoRow("应到人次", "${stat.total}")
                        InfoRow("出勤", "${stat.present}")
                        InfoRow("迟到", "${stat.late}")
                        InfoRow("请假", "${stat.leave}")
                    }
                }
            }
            SectionCard("各活动签到人数") {
                BarChart(actStats.map { it.deptName to it.memberCount.toFloat() },
                    color = MaterialTheme.colorScheme.primary)
            }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(14.dp)) {
                    Text("统计说明", style = MaterialTheme.typography.titleSmall)
                    Text("出勤率 = 出勤人数 / 应到人数；统计由 SQL 聚合查询得出。")
                    Text("目前登记队员 ${members.size} 人。")
                }
            }
        }
    }
}
