package com.example.fleetmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fleetmanager.data.BorrowRecordEntity
import com.example.fleetmanager.data.BorrowStat
import com.example.fleetmanager.data.ItemEntity
import com.example.fleetmanager.ui.components.*
import com.example.fleetmanager.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListScreen(nav: NavController, vm: AppViewModel) {
    val items by vm.items.collectAsState(emptyList())
    val borrowStat by vm.borrowStat.collectAsState(BorrowStat(0, 0, 0))
    var showAdd by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var total by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var borrowItem by remember { mutableStateOf<ItemEntity?>(null) }
    var toDelete by remember { mutableStateOf<ItemEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("物资管理") }, actions = {
                IconButton(onClick = { nav.navigate("borrows") }) {
                    Icon(Icons.Default.History, "借还记录")
                }
            })
        },
        floatingActionButton = {
            if (vm.isAdmin) FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, "新增物资")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("在借", "${borrowStat.onLoan}", color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f))
                StatCard("逾期", "${borrowStat.overdue}", color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f))
                StatCard("已还", "${borrowStat.returned}", color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items) { item ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Text(item.name, Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleMedium)
                                StatusChip(item.status, statusColor(item.status))
                                if (vm.isAdmin) DeleteAction { toDelete = item }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("类别：${item.category.ifEmpty { "-" }} · 存放：${item.location.ifEmpty { "-" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("总量 ${item.totalQty} · 可用 ${item.availableQty}",
                                style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { borrowItem = item },
                                enabled = item.availableQty > 0, modifier = Modifier.fillMaxWidth()) {
                                Text("借出")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("新增物资") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it },
                        label = { Text("物品名称") }, singleLine = true)
                    OutlinedTextField(value = category, onValueChange = { category = it },
                        label = { Text("类别") }, singleLine = true)
                    OutlinedTextField(value = total, onValueChange = { total = it },
                        label = { Text("数量") }, singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = location, onValueChange = { location = it },
                        label = { Text("存放位置") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val qty = total.toIntOrNull() ?: 0
                    if (name.isNotBlank() && qty > 0) {
                        vm.saveItem(ItemEntity(name = name.trim(), category = category.trim(),
                            totalQty = qty, availableQty = qty, location = location.trim()))
                        name = ""; category = ""; total = ""; location = ""; showAdd = false
                    }
                }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("取消") } }
        )
    }
    borrowItem?.let { item ->
        BorrowDialog(item = item, vm = vm, onDismiss = { borrowItem = null })
    }
    toDelete?.let {
        ConfirmDialog("删除物资", "确定删除物资「${it.name}」？",
            onConfirm = { vm.deleteItem(it) }, onDismiss = { toDelete = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BorrowDialog(item: ItemEntity, vm: AppViewModel, onDismiss: () -> Unit) {
    val members by vm.members.collectAsState(emptyList())
    var memberId by remember { mutableStateOf(members.firstOrNull()?.id ?: 0L) }
    var qty by remember { mutableStateOf("1") }
    var due by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("借出：${item.name}（可用 ${item.availableQty}）") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = members.firstOrNull { it.id == memberId }?.name ?: "请选择队员",
                        onValueChange = {}, readOnly = true, label = { Text("借用人") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        members.forEach { m ->
                            DropdownMenuItem(text = { Text(m.name) }, onClick = {
                                memberId = m.id; expanded = false
                            })
                        }
                    }
                }
                OutlinedTextField(value = qty, onValueChange = { qty = it },
                    label = { Text("数量") }, singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number))
                OutlinedTextField(value = due, onValueChange = { due = it },
                    label = { Text("应还日期") }, singleLine = true, placeholder = { Text("yyyy-MM-dd") })
                OutlinedTextField(value = remark, onValueChange = { remark = it },
                    label = { Text("备注") }, singleLine = true)
                if (result.isNotEmpty()) Text(result,
                    color = if (result == "借出成功") MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                vm.borrow(item.id, memberId, qty.toIntOrNull() ?: 1, due, remark) { ok, msg ->
                    result = msg
                    if (ok) onDismiss()
                }
            }) { Text("确认借出") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowScreen(nav: NavController, vm: AppViewModel) {
    val records by vm.borrowRecords.collectAsState(emptyList())
    val items by vm.items.collectAsState(emptyList())
    val members by vm.members.collectAsState(emptyList())
    var toDelete by remember { mutableStateOf<BorrowRecordEntity?>(null) }
    val itemName = { id: Long -> items.firstOrNull { it.id == id }?.name ?: "未知物品" }
    val memberName = { id: Long -> members.firstOrNull { it.id == id }?.name ?: "未知队员" }

    Scaffold(topBar = {
        TopAppBar(title = { Text("借还记录（${records.size}）") },
            navigationIcon = {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, "返回") }
            })
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            LaunchedEffect(Unit) { vm.refreshOverdue() }
            if (records.isEmpty()) EmptyState("暂无借还记录")
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(records) { r ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Text("${itemName(r.itemId)} ×${r.qty}", Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleMedium)
                                StatusChip(r.status, statusColor(r.status))
                            }
                            InfoRow("借用人", memberName(r.memberId))
                            InfoRow("借出时间", r.borrowTime)
                            InfoRow("应还时间", r.dueTime)
                            InfoRow("归还时间", r.returnTime ?: "-")
                            if (r.remark.isNotBlank()) InfoRow("备注", r.remark)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (r.status == "在借" || r.status == "逾期") {
                                    Button(onClick = { vm.returnItem(r) { _, _ -> } },
                                        modifier = Modifier.weight(1f)) { Text("归还") }
                                }
                                if (vm.isAdmin) {
                                    OutlinedButton(onClick = { toDelete = r },
                                        modifier = Modifier.weight(1f)) { Text("删除记录") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    toDelete?.let {
        ConfirmDialog("删除记录", "确定删除该借还记录？",
            onConfirm = { vm.deleteBorrowRecord(it) }, onDismiss = { toDelete = null })
    }
}
