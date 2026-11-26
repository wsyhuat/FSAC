package com.example.fleetmanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class, DepartmentEntity::class, MemberEntity::class,
        TaskEntity::class, ActivityEntity::class, AttendanceEntity::class,
        AnnouncementEntity::class, ItemEntity::class, BorrowRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun departmentDao(): DepartmentDao
    abstract fun memberDao(): MemberDao
    abstract fun taskDao(): TaskDao
    abstract fun activityDao(): ActivityDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun itemDao(): ItemDao
    abstract fun borrowRecordDao(): BorrowRecordDao
    abstract fun statsDao(): StatsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "fleet_manager.db"
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // 在 onCreate 里 INSTANCE 尚未赋值，需用 applicationContext 重建一个同库实例来 seed
                        // 注意：不能直接在这里用 DAO，因此延迟到数据库完全初始化后再写入
                    }
                })
                .build().also { INSTANCE = it }
        }

        /** 在数据库首次创建后写入演示数据（由 ViewModel 初始化时调用） */
        suspend fun seedIfEmpty(db: AppDatabase) {
            if (db.userDao().findByName("admin") != null) return
            seed(db)
        }

        private suspend fun seed(db: AppDatabase) {
            // 部门
            val d1 = db.departmentDao().insert(DepartmentEntity(name = "技术部", leaderId = 1))
            val d2 = db.departmentDao().insert(DepartmentEntity(name = "运营部", leaderId = 3))
            val d3 = db.departmentDao().insert(DepartmentEntity(name = "外联部", leaderId = 5))

            // 队员
            val m1 = db.memberDao().insert(MemberEntity(studentNo = "20230001", name = "张伟", departmentId = d1, role = "队长", phone = "13800000001", email = "zhangwei@school.edu", joinDate = "2023-09-01"))
            val m2 = db.memberDao().insert(MemberEntity(studentNo = "20230002", name = "李娜", departmentId = d1, role = "队员", phone = "13800000002", joinDate = "2023-09-01"))
            val m3 = db.memberDao().insert(MemberEntity(studentNo = "20230003", name = "王强", departmentId = d2, role = "队长", phone = "13800000003", joinDate = "2023-09-02"))
            val m4 = db.memberDao().insert(MemberEntity(studentNo = "20230004", name = "刘洋", departmentId = d2, role = "队员", joinDate = "2023-09-05"))
            val m5 = db.memberDao().insert(MemberEntity(studentNo = "20230005", name = "陈静", departmentId = d3, role = "队长", joinDate = "2023-09-10"))
            val m6 = db.memberDao().insert(MemberEntity(studentNo = "20230006", name = "赵敏", departmentId = d3, role = "队员", joinDate = "2024-03-01"))

            // 账号：admin / 123456，队员账号与学号同名
            db.userDao().insert(UserEntity(username = "admin", passwordHash = hashPassword("123456"), role = "admin", memberId = m1))
            db.userDao().insert(UserEntity(username = "20230001", passwordHash = hashPassword("123456"), role = "member", memberId = m1))
            db.userDao().insert(UserEntity(username = "20230002", passwordHash = hashPassword("123456"), role = "member", memberId = m2))
            db.userDao().insert(UserEntity(username = "20230003", passwordHash = hashPassword("123456"), role = "member", memberId = m3))
            db.userDao().insert(UserEntity(username = "20230004", passwordHash = hashPassword("123456"), role = "member", memberId = m4))
            db.userDao().insert(UserEntity(username = "20230005", passwordHash = hashPassword("123456"), role = "member", memberId = m5))
            db.userDao().insert(UserEntity(username = "20230006", passwordHash = hashPassword("123456"), role = "member", memberId = m6))

            // 任务
            db.taskDao().insert(TaskEntity(title = "完成赛季预算表", description = "整理全年参赛预算，提交指导老师", assigneeId = m1, creatorId = m1, deadline = "2026-12-10", priority = "高", status = "进行中", createdAt = "2026-11-01"))
            db.taskDao().insert(TaskEntity(title = "设计新车涂装方案", description = "输出三套涂装设计方案", assigneeId = m2, creatorId = m1, deadline = "2026-12-05", priority = "中", status = "待办", createdAt = "2026-11-02"))
            db.taskDao().insert(TaskEntity(title = "联系赞助商", description = "对接本地企业，落实赞助", assigneeId = m5, creatorId = m1, deadline = "2026-11-30", priority = "高", status = "完成", createdAt = "2026-10-20", completedAt = "2026-11-18"))
            db.taskDao().insert(TaskEntity(title = "采购工具套装", description = "采购维修工具两套", assigneeId = m3, creatorId = m1, deadline = "2026-11-28", priority = "中", status = "进行中", createdAt = "2026-11-05"))
            db.taskDao().insert(TaskEntity(title = "整理训练视频", description = "剪辑并归档训练录像", assigneeId = m4, creatorId = m1, deadline = "2026-12-01", priority = "低", status = "待办", createdAt = "2026-11-08"))
            db.taskDao().insert(TaskEntity(title = "官网内容更新", description = "更新车队介绍与成员照片", assigneeId = m6, creatorId = m1, deadline = "2026-11-20", priority = "低", status = "完成", createdAt = "2026-10-25", completedAt = "2026-11-15"))

            // 活动（考勤与赛程共用）
            val a1 = db.activityDao().insert(ActivityEntity(title = "常规训练", date = "2026-11-20", location = "工程训练中心", type = "训练", createdBy = m1))
            val a2 = db.activityDao().insert(ActivityEntity(title = "车队例会", date = "2026-11-18", location = "教学楼 A301", type = "会议", createdBy = m1))
            db.activityDao().insert(ActivityEntity(title = "校际邀请赛", date = "2026-12-06", location = "市体育中心", type = "比赛", createdBy = m1))
            db.activityDao().insert(ActivityEntity(title = "新车调试", date = "2026-11-25", location = "工程训练中心", type = "训练", createdBy = m2))

            // 考勤
            listOf(m1, m2, m3, m4, m5).forEach { mid ->
                db.attendanceDao().insert(AttendanceEntity(activityId = a1, memberId = mid, status = "出勤", checkTime = "2026-11-20 08:30"))
            }
            db.attendanceDao().insert(AttendanceEntity(activityId = a1, memberId = m6, status = "迟到", checkTime = "2026-11-20 08:45"))
            listOf(m1, m2, m3, m4, m5).forEach { mid ->
                db.attendanceDao().insert(AttendanceEntity(activityId = a2, memberId = mid, status = "出勤", checkTime = "2026-11-18 19:00"))
            }
            db.attendanceDao().insert(AttendanceEntity(activityId = a2, memberId = m6, status = "请假", remark = "课程冲突"))

            // 公告
            db.announcementDao().insert(AnnouncementEntity(title = "关于赛季启动的通知", content = "本学期车队训练正式启动，请全体队员按时参加每周训练。", publisherId = m1, createdAt = "2026-11-15 09:00", pinned = true))
            db.announcementDao().insert(AnnouncementEntity(title = "招新结果公示", content = "经过面试，共有 6 名同学通过招新，欢迎加入车队。", publisherId = m1, createdAt = "2026-11-10 14:30"))
            db.announcementDao().insert(AnnouncementEntity(title = "实验室使用规定", content = "进入实验室请登记，工具使用后请归位，保持卫生。", publisherId = m3, createdAt = "2026-11-05 10:00"))

            // 物资
            val i1 = db.itemDao().insert(ItemEntity(name = "笔记本电脑", category = "电子设备", totalQty = 5, availableQty = 3, location = "办公室"))
            val i2 = db.itemDao().insert(ItemEntity(name = "万用表", category = "工具", totalQty = 4, availableQty = 2, location = "工具间"))
            db.itemDao().insert(ItemEntity(name = "赛车服", category = "装备", totalQty = 10, availableQty = 8, location = "仓库"))
            db.itemDao().insert(ItemEntity(name = "电钻", category = "工具", totalQty = 2, availableQty = 1, location = "工具间"))
            db.itemDao().insert(ItemEntity(name = "投影仪", category = "电子设备", totalQty = 1, availableQty = 1, location = "会议室", status = "维修"))

            // 借还记录
            db.borrowRecordDao().insert(BorrowRecordEntity(itemId = i1, memberId = m2, qty = 1, borrowTime = "2026-11-10", dueTime = "2026-11-17", status = "逾期", remark = "尚未归还"))
            db.borrowRecordDao().insert(BorrowRecordEntity(itemId = i2, memberId = m3, qty = 1, borrowTime = "2026-11-18", dueTime = "2026-11-25", status = "在借"))
            db.borrowRecordDao().insert(BorrowRecordEntity(itemId = i1, memberId = m4, qty = 1, borrowTime = "2026-11-01", dueTime = "2026-11-08", returnTime = "2026-11-07", status = "已还"))
        }
    }
}
