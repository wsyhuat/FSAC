package com.example.fleetmanager.ui.screens

import androidx.compose.foundation.layout.*
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
fun ProfileScreen(nav: NavController, vm: AppViewModel) {
    val user = vm.currentUser.value
    Scaffold(topBar = {
        TopAppBar(title = { Text("个人中心") }, navigationIcon = {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, "返回") }
        })
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AccountCircle, null, Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(user?.username ?: "-", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    InfoRow("账号", user?.username ?: "-")
                    InfoRow("角色", if (user?.role == "admin") "管理员" else "队员")
                    InfoRow("关联队员ID", user?.memberId?.toString() ?: "-")
                    InfoRow("权限说明", if (vm.isAdmin) "可增删改查全部数据" else "以查看和签到为主")
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = {
                // 先清空导航栈（回到起始 home），再清除登录状态，避免下次登录残留旧页面
                nav.navigate("home") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
                vm.logout()
            }, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("退出登录")
            }
        }
    }
}
