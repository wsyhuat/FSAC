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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fleetmanager.data.TaskEntity
import com.example.fleetmanager.ui.components.*
import com.example.fleetmanager.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(nav: NavController, vm: AppViewModel) {
    val tasks by vm.tasks.collectAsState(emptyList())
    val members by vm.members.collectAsState(emptyList())
    var statusFilter by remember { mutableStateOf("全部") }
    var assigneeFilter by remember { mutableStateOf(0L) }
    var toDelete by remember { mutableStateOf<TaskEntity?>(null) }
    val nameOf = { id: Long? -> members.firstOrNull { it.id == id }?.name ?: "未分配" }

    val filtered = tasks.filter {
        (statusFilter == "全部" || it.status == statusFilter) &&
                (assigneeFilter == 0L || it.assigneeId == assigneeFilter)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("任务管理（${filtered.size}）") }) },
        floatingActionButton = {
            if (vm.isAdmin) FloatingActionButton(onClick = { nav.navigate("task_edit/0") }) {
                Icon(Icons.Default.Add, "新增任务")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("全部", "待办", "进行中", "完成").forEach { s ->
                    FilterChip(s, statusFilter == s, Modifier.weight(1f)) { statusFilter = s }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip("全部负责人", assigneeFilter == 0L, Modifier.weight(1f)) { assigneeFilter = 0L }
                members.take(2).forEach { m ->
                    FilterChip(m.name, assigneeFilter == m.id, Modifier.weight(1f)) { assigneeFilter = m.id }
                }
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered) { t ->
                    Card(Modifier.fillMaxWidth().clickable { nav.navigate("task_edit/${t.id}") }) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(t.title, Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleMedium)
                                StatusChip(t.status, statusColor(t.status))
                                if (vm.isAdmin) DeleteAction { toDelete = t }
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatusChip("优先级：${t.priority}", priorityColor(t.priority))
                                Text("负责人：${nameOf(t.assigneeId)}",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("截止：${t.deadline.ifEmpty { "未设置" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
    toDelete?.let {
        ConfirmDialog("删除任务", "确定删除任务「${it.title}」？",
            onConfirm = { vm.deleteTask(it) }, onDismiss = { toDelete = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(nav: NavController, vm: AppViewModel, id: Long) {
    val tasks by vm.tasks.collectAsState(emptyList())
    val members by vm.members.collectAsState(emptyList())
    val existing = tasks.firstOrNull { it.id == id }

    var title by remember(existing) { mutableStateOf(existing?.title ?: "") }
    var desc by remember(existing) { mutableStateOf(existing?.description ?: "") }
    var assignee by remember(existing) { mutableStateOf(existing?.assigneeId ?: members.firstOrNull()?.id) }
    var deadline by remember(existing) { mutableStateOf(existing?.deadline ?: "") }
    var priority by remember(existing) { mutableStateOf(existing?.priority ?: "中") }
    var status by remember(existing) { mutableStateOf(existing?.status ?: "待办") }
    var assigneeExpanded by remember { mutableStateOf(false) }
    val editable = vm.isAdmin

    Scaffold(topBar = {
        TopAppBar(title = { Text(if (id == 0L) "新增任务" else "任务详情 / 编辑") },
            navigationIcon = {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, "返回") }
            })
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("任务标题") },
                        enabled = editable, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("任务描述") },
                        enabled = editable, minLines = 3, modifier = Modifier.fillMaxWidth())
                    ExposedDropdownMenuBox(expanded = assigneeExpanded,
                        onExpandedChange = { if (editable) assigneeExpanded = it }) {
                        OutlinedTextField(
                            value = members.firstOrNull { it.id == assignee }?.name ?: "未分配",
                            onValueChange = {}, readOnly = true, enabled = editable,
                            label = { Text("负责人") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(assigneeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = assigneeExpanded,
                            onDismissRequest = { assigneeExpanded = false }) {
                            members.forEach { m ->
                                DropdownMenuItem(text = { Text(m.name) }, onClick = {
                                    assignee = m.id; assigneeExpanded = false
                                })
                            }
                        }
                    }
                    OutlinedTextField(value = deadline, onValueChange = { deadline = it },
                        label = { Text("截止日期") }, enabled = editable, singleLine = true,
                        placeholder = { Text("yyyy-MM-dd") }, modifier = Modifier.fillMaxWidth())
                    Text("优先级（课程版：高/中/低）", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("高", "中", "低").forEach { p ->
                            FilterChip(p, priority == p, Modifier.weight(1f)) { if (editable) priority = p }
                        }
                    }
                    Text("状态（课程版：待办/进行中/完成）", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("待办", "进行中", "完成").forEach { s ->
                            FilterChip(s, status == s, Modifier.weight(1f)) { if (editable) status = s }
                        }
                    }
                    if (existing != null) {
                        InfoRow("创建时间", existing.createdAt.ifEmpty { "-" })
                        InfoRow("完成时间", existing.completedAt ?: "-")
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            if (editable) {
                Button(onClick = {
                    if (title.isBlank()) return@Button
                    val completedAt = if (status == "完成")
                        (existing?.completedAt ?: java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            .format(java.util.Date())) else null
                    vm.saveTask(TaskEntity(
                        id = id, title = title.trim(), description = desc.trim(),
                        assigneeId = assignee, creatorId = existing?.creatorId,
                        deadline = deadline.trim(), priority = priority, status = status,
                        createdAt = existing?.createdAt ?: java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            .format(java.util.Date()),
                        completedAt = completedAt
                    ))
                    nav.popBackStack()
                }, modifier = Modifier.fillMaxWidth()) { Text("保存") }
            } else {
                Text("当前账号为队员，仅管理员可编辑任务。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
