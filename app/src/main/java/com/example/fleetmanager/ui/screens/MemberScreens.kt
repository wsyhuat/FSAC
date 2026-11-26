package com.example.fleetmanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fleetmanager.data.DepartmentEntity
import com.example.fleetmanager.data.MemberEntity
import com.example.fleetmanager.ui.components.*
import com.example.fleetmanager.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberListScreen(nav: NavController, vm: AppViewModel) {
    val members by vm.members.collectAsState(emptyList())
    val depts by vm.departments.collectAsState(emptyList())
    var keyword by remember { mutableStateOf("") }
    var deptFilter by remember { mutableStateOf(0L) }
    var toDelete by remember { mutableStateOf<MemberEntity?>(null) }
    val deptName = { id: Long? -> depts.firstOrNull { it.id == id }?.name ?: "未分配" }

    val filtered = members.filter {
        (deptFilter == 0L || it.departmentId == deptFilter) &&
                (keyword.isBlank() || it.name.contains(keyword) || it.studentNo.contains(keyword))
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("队员管理（${filtered.size}）") }) },
        floatingActionButton = {
            if (vm.isAdmin) {
                FloatingActionButton(onClick = { nav.navigate("member_edit/0") }) {
                    Icon(Icons.Default.Add, "新增队员")
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = keyword, onValueChange = { keyword = it },
                label = { Text("搜索姓名 / 学号") }, singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip("全部", deptFilter == 0L, Modifier.weight(1f)) { deptFilter = 0L }
                depts.take(3).forEach { d ->
                    FilterChip(d.name, deptFilter == d.id, Modifier.weight(1f)) { deptFilter = d.id }
                }
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered) { m ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { nav.navigate("member_edit/${m.id}") }) {
                        Row(Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(m.name.take(1), Modifier.padding(10.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(m.name, style = MaterialTheme.typography.titleMedium)
                                Text("${m.studentNo} · ${deptName(m.departmentId)} · ${m.role}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusChip(m.status, statusColor(m.status))
                            if (vm.isAdmin) DeleteAction { toDelete = m }
                        }
                    }
                }
            }
        }
    }
    toDelete?.let {
        ConfirmDialog("删除队员", "确定删除 ${it.name}？此操作不可撤销。",
            onConfirm = { vm.deleteMember(it) }, onDismiss = { toDelete = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberEditScreen(nav: NavController, vm: AppViewModel, id: Long) {
    val members by vm.members.collectAsState(emptyList())
    val depts by vm.departments.collectAsState(emptyList())
    val existing = members.firstOrNull { it.id == id }

    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var studentNo by remember(existing) { mutableStateOf(existing?.studentNo ?: "") }
    var phone by remember(existing) { mutableStateOf(existing?.phone ?: "") }
    var email by remember(existing) { mutableStateOf(existing?.email ?: "") }
    var joinDate by remember(existing) { mutableStateOf(existing?.joinDate ?: "") }
    var role by remember(existing) { mutableStateOf(existing?.role ?: "队员") }
    var status by remember(existing) { mutableStateOf(existing?.status ?: "在队") }
    var deptId by remember(existing) { mutableStateOf(existing?.departmentId ?: depts.firstOrNull()?.id) }
    var expanded by remember { mutableStateOf(false) }

    val editable = vm.isAdmin

    Scaffold(topBar = {
        TopAppBar(title = { Text(if (id == 0L) "新增队员" else "队员详情 / 编辑") },
            navigationIcon = {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, "返回") }
            })
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("姓名") },
                        enabled = editable, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = studentNo, onValueChange = { studentNo = it }, label = { Text("学号") },
                        enabled = editable, singleLine = true, modifier = Modifier.fillMaxWidth())
                    // 部门用字典表选择，不做组织树
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = depts.firstOrNull { it.id == deptId }?.name ?: "未分配",
                            onValueChange = {}, readOnly = true, enabled = editable,
                            label = { Text("部门（字典表）") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            depts.forEach { d ->
                                DropdownMenuItem(text = { Text(d.name) }, onClick = {
                                    deptId = d.id; expanded = false
                                })
                            }
                        }
                    }
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("电话") },
                        enabled = editable, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("邮箱") },
                        enabled = editable, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = joinDate, onValueChange = { joinDate = it }, label = { Text("入队日期") },
                        enabled = editable, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("yyyy-MM-dd") })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("队长", "队员").forEach { r ->
                            FilterChip(r, role == r, Modifier.weight(1f)) { if (editable) role = r }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("在队", "离队").forEach { s ->
                            FilterChip(s, status == s, Modifier.weight(1f)) { if (editable) status = s }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            if (editable) {
                Button(onClick = {
                    if (name.isBlank()) return@Button
                    vm.saveMember(MemberEntity(
                        id = id, name = name.trim(), studentNo = studentNo.trim(),
                        departmentId = deptId, role = role, phone = phone.trim(),
                        email = email.trim(), joinDate = joinDate.trim(), status = status
                    ))
                    nav.popBackStack()
                }, modifier = Modifier.fillMaxWidth()) { Text("保存") }
            } else {
                Text("当前账号为队员，仅管理员可编辑队员信息。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
