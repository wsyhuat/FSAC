package com.example.fleetmanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fleetmanager.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)

    init {
        // 首次启动时写入演示数据（原 onCreate 回调因 INSTANCE 未赋值而失效）
        viewModelScope.launch { AppDatabase.seedIfEmpty(db) }
    }

    // ===== 各表数据流 =====
    val users = db.userDao().getAll()
    val departments = db.departmentDao().getAll()
    val members = db.memberDao().getAll()
    val tasks = db.taskDao().getAll()
    val activities = db.activityDao().getAll()
    val attendances = db.attendanceDao().getAll()
    val announcements = db.announcementDao().getAll()
    val items = db.itemDao().getAll()
    val borrowRecords = db.borrowRecordDao().getAll()

    // ===== 统计 =====
    val taskStat = db.statsDao().taskStat()
    val attendanceStat = db.statsDao().attendanceStat()
    val borrowStat = db.statsDao().borrowStat()
    val deptStats = db.statsDao().deptStats()
    val perActivityStats = db.statsDao().perActivityStats()

    // ===== 登录状态 =====
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()
    val isAdmin: Boolean get() = _currentUser.value?.role == "admin"

    fun login(username: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = db.userDao().findByName(username.trim())
            when {
                user == null -> onResult(false, "账号不存在")
                user.passwordHash != hashPassword(password) -> onResult(false, "密码错误")
                else -> {
                    _currentUser.value = user
                    onResult(true, "登录成功")
                }
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    // ===== 队员 =====
    fun saveMember(m: MemberEntity) = viewModelScope.launch {
        if (m.id == 0L) db.memberDao().insert(m) else db.memberDao().update(m)
    }

    fun deleteMember(m: MemberEntity) = viewModelScope.launch { db.memberDao().delete(m) }

    // ===== 任务 =====
    fun saveTask(t: TaskEntity) = viewModelScope.launch {
        if (t.id == 0L) db.taskDao().insert(t) else db.taskDao().update(t)
    }

    fun deleteTask(t: TaskEntity) = viewModelScope.launch { db.taskDao().delete(t) }

    // ===== 活动 =====
    fun saveActivity(a: ActivityEntity) = viewModelScope.launch {
        if (a.id == 0L) db.activityDao().insert(a) else db.activityDao().update(a)
    }

    fun deleteActivity(a: ActivityEntity) = viewModelScope.launch { db.activityDao().delete(a) }

    // ===== 考勤 / 签到 =====
    fun checkIn(activityId: Long, memberId: Long, status: String, remark: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val existing = db.attendanceDao().findOne(activityId, memberId)
            val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date())
            val record = AttendanceEntity(
                id = existing?.id ?: 0,
                activityId = activityId,
                memberId = memberId,
                status = status,
                checkTime = now,
                remark = remark
            )
            if (existing == null) db.attendanceDao().insert(record) else db.attendanceDao().update(record)
            onDone(true)
        }
    }

    fun deleteAttendance(a: AttendanceEntity) = viewModelScope.launch { db.attendanceDao().delete(a) }

    // ===== 公告 =====
    fun saveAnnouncement(a: AnnouncementEntity) = viewModelScope.launch {
        if (a.id == 0L) db.announcementDao().insert(a) else db.announcementDao().update(a)
    }

    fun deleteAnnouncement(a: AnnouncementEntity) = viewModelScope.launch { db.announcementDao().delete(a) }

    // ===== 物资 =====
    fun saveItem(i: ItemEntity) = viewModelScope.launch {
        if (i.id == 0L) db.itemDao().insert(i) else db.itemDao().update(i)
    }

    fun deleteItem(i: ItemEntity) = viewModelScope.launch { db.itemDao().delete(i) }

    // ===== 借还 =====
    fun borrow(itemId: Long, memberId: Long, qty: Int, dueTime: String, remark: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val item = db.itemDao().findById(itemId)
            when {
                item == null -> onResult(false, "物品不存在")
                item.availableQty < qty -> onResult(false, "库存不足，当前可用 ${item.availableQty}")
                else -> {
                    val now = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .format(java.util.Date())
                    db.borrowRecordDao().insert(
                        BorrowRecordEntity(
                            itemId = itemId, memberId = memberId, qty = qty,
                            borrowTime = now, dueTime = dueTime, status = "在借", remark = remark
                        )
                    )
                    db.itemDao().update(item.copy(availableQty = item.availableQty - qty))
                    onResult(true, "借出成功")
                }
            }
        }
    }

    fun returnItem(record: BorrowRecordEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val item = db.itemDao().findById(record.itemId)
            val now = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            db.borrowRecordDao().update(record.copy(returnTime = now, status = "已还"))
            if (item != null) {
                db.itemDao().update(item.copy(availableQty = item.availableQty + record.qty))
            }
            onResult(true, "归还成功")
        }
    }

    fun deleteBorrowRecord(r: BorrowRecordEntity) = viewModelScope.launch { db.borrowRecordDao().delete(r) }

    // ===== 同步辅助：把逾期记录状态刷新为「逾期」 =====
    fun refreshOverdue() = viewModelScope.launch {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        val list = db.borrowRecordDao().getAllOnce()
        list.filter { it.status == "在借" && it.dueTime < today }.forEach {
            db.borrowRecordDao().update(it.copy(status = "逾期"))
        }
    }
}
