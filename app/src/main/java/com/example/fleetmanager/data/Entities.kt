package com.example.fleetmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.security.MessageDigest

/** 把明文密码转 SHA-256，课程版本地校验用 */
fun hashPassword(plain: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(plain.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val role: String = "member",          // admin / member
    val memberId: Long? = null
)

@Entity(tableName = "department")
data class DepartmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val leaderId: Long? = null
)

@Entity(tableName = "member")
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentNo: String = "",
    val name: String,
    val departmentId: Long? = null,
    val role: String = "队员",             // 队长 / 队员
    val phone: String = "",
    val email: String = "",
    val joinDate: String = "",
    val status: String = "在队"            // 在队 / 离队
)

@Entity(tableName = "task")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val assigneeId: Long? = null,
    val creatorId: Long? = null,
    val deadline: String = "",
    val priority: String = "中",           // 高 / 中 / 低
    val status: String = "待办",           // 待办 / 进行中 / 完成
    val createdAt: String = "",
    val completedAt: String? = null
)

@Entity(tableName = "activity")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: String,
    val location: String = "",
    val type: String = "训练",             // 训练 / 比赛 / 会议 / 其他
    val createdBy: Long? = null
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityId: Long,
    val memberId: Long,
    val status: String = "出勤",           // 出勤 / 迟到 / 请假 / 缺勤
    val checkTime: String = "",
    val remark: String = ""
)

@Entity(tableName = "announcement")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val publisherId: Long? = null,
    val createdAt: String = "",
    val pinned: Boolean = false
)

@Entity(tableName = "item")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "",
    val totalQty: Int = 0,
    val availableQty: Int = 0,
    val location: String = "",
    val status: String = "可用"            // 可用 / 维修 / 报废
)

@Entity(tableName = "borrow_record")
data class BorrowRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val memberId: Long,
    val qty: Int = 1,
    val borrowTime: String = "",
    val dueTime: String = "",
    val returnTime: String? = null,
    val status: String = "在借",           // 在借 / 已还 / 逾期
    val remark: String = ""
)
