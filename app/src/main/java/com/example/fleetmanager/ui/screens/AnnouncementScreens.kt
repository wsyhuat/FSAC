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
import com.example.fleetmanager.data.AnnouncementEntity
import com.example.fleetmanager.ui.components.*
import com.example.fleetmanager.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementListScreen(nav: NavController, vm: AppViewModel) {
    val list by vm.announcements.collectAsState(emptyList())
    var toDelete by remember { mutableStateOf<AnnouncementEntity?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("公告（${list.size}）") }) },
        floatingActionButton = {
            if (vm.isAdmin) FloatingActionButton(onClick = { nav.navigate("announcement_edit/0") }) {
                Icon(Icons.Default.Add, "发布公告")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            InfoRow("说明", "公告为纯文本，不做富文本编辑器；支持置顶")
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list) { a ->
                    Card(Modifier.fillMaxWidth().clickable { nav.navigate("announcement_detail/${a.id}") }) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (a.pinned) StatusChip("置顶", Color(0xFFC62828))
                                Spacer(Modifier.width(8.dp))
                                Text(a.title, Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                if (vm.isAdmin) DeleteAction { toDelete = a }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(a.content, style = MaterialTheme.typography.bodySmall, maxLines = 2,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(a.createdAt, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
    toDelete?.let {
        ConfirmDialog("删除公告", "确定删除公告「${it.title}」？",
            onConfirm = { vm.deleteAnnouncement(it) }, onDismiss = { toDelete = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementDetailScreen(nav: NavController, vm: AppViewModel, id: Long) {
    val list by vm.announcements.collectAsState(emptyList())
    val a = list.firstOrNull { it.id == id }
    Scaffold(topBar = {
        TopAppBar(title = { Text(a?.title ?: "公告详情") },
            navigationIcon = {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, "返回") }
            })
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(a?.title ?: "", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("发布时间：${a?.createdAt ?: "-"}", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Divider(Modifier.padding(vertical = 12.dp))
                    Text(a?.content ?: "", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(Modifier.height(12.dp))
            if (vm.isAdmin) {
                Button(onClick = { nav.navigate("announcement_edit/$id") },
                    modifier = Modifier.fillMaxWidth()) { Text("编辑公告") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementEditScreen(nav: NavController, vm: AppViewModel, id: Long) {
    val list by vm.announcements.collectAsState(emptyList())
    val existing = list.firstOrNull { it.id == id }
    var title by remember(existing) { mutableStateOf(existing?.title ?: "") }
    var content by remember(existing) { mutableStateOf(existing?.content ?: "") }
    var pinned by remember(existing) { mutableStateOf(existing?.pinned ?: false) }

    Scaffold(topBar = {
        TopAppBar(title = { Text(if (id == 0L) "发布公告" else "编辑公告") },
            navigationIcon = {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, "返回") }
            })
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it },
                        label = { Text("标题") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = content, onValueChange = { content = it },
                        label = { Text("正文（纯文本）") }, minLines = 6, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = pinned, onCheckedChange = { pinned = it })
                        Text("置顶公告")
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                if (title.isBlank() || content.isBlank()) return@Button
                val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date())
                vm.saveAnnouncement(AnnouncementEntity(
                    id = id, title = title.trim(), content = content.trim(),
                    publisherId = existing?.publisherId, createdAt = existing?.createdAt ?: now,
                    pinned = pinned
                ))
                nav.popBackStack()
            }, modifier = Modifier.fillMaxWidth()) { Text("保存") }
        }
    }
}
