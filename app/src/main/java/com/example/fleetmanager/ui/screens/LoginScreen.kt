package com.example.fleetmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.fleetmanager.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(vm: AppViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.DirectionsCar, "车队", Modifier.padding(24.dp).size(48.dp),
                    tint = Color.White)
            }
            Spacer(Modifier.height(16.dp))
            Text("车队管理系统", style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary)
            Text("课程演示版 · 本地登录", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(36.dp))

            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("账号") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("密码") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        message = "请填写账号和密码"
                    } else {
                        vm.login(username, password) { ok, msg ->
                            message = msg
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("登录")
            }
            if (message.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(message, color = if (message == "登录成功") Color(0xFF2E7D32)
                else MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(32.dp))
            Card(colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp)) {
                    Text("演示账号", style = MaterialTheme.typography.titleSmall)
                    Text("管理员：admin / 123456（全部权限）")
                    Text("队员：20230002 / 123456（只读为主）")
                }
            }
        }
    }
}
