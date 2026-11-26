package com.example.fleetmanager.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class TaskStat(val total: Int, val completed: Int, val doing: Int, val todo: Int)
data class AttendanceStat(val total: Int, val present: Int, val late: Int, val leave: Int)
data class BorrowStat(val onLoan: Int, val overdue: Int, val returned: Int)
data class DeptStat(val deptName: String, val memberCount: Int)

/** 统计直接用 SQL 计算（方案要求） */
@Dao
interface StatsDao {
    @Query("""
        SELECT COUNT(*) AS total,
               COALESCE(SUM(CASE WHEN status='完成' THEN 1 ELSE 0 END), 0) AS completed,
               COALESCE(SUM(CASE WHEN status='进行中' THEN 1 ELSE 0 END), 0) AS doing,
               COALESCE(SUM(CASE WHEN status='待办' THEN 1 ELSE 0 END), 0) AS todo
        FROM task
    """)
    fun taskStat(): Flow<TaskStat>

    @Query("""
        SELECT COUNT(*) AS total,
               COALESCE(SUM(CASE WHEN status='出勤' THEN 1 ELSE 0 END), 0) AS present,
               COALESCE(SUM(CASE WHEN status='迟到' THEN 1 ELSE 0 END), 0) AS late,
               COALESCE(SUM(CASE WHEN status='请假' THEN 1 ELSE 0 END), 0) AS leave
        FROM attendance
    """)
    fun attendanceStat(): Flow<AttendanceStat>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN status='在借' THEN 1 ELSE 0 END), 0) AS onLoan,
               COALESCE(SUM(CASE WHEN status='逾期' THEN 1 ELSE 0 END), 0) AS overdue,
               COALESCE(SUM(CASE WHEN status='已还' THEN 1 ELSE 0 END), 0) AS returned
        FROM borrow_record
    """)
    fun borrowStat(): Flow<BorrowStat>

    @Query("""
        SELECT d.name AS deptName, COUNT(m.id) AS memberCount
        FROM department d LEFT JOIN member m ON m.departmentId = d.id
        GROUP BY d.id ORDER BY d.id
    """)
    fun deptStats(): Flow<List<DeptStat>>

    @Query("""
        SELECT a.title AS deptName, COUNT(b.id) AS memberCount
        FROM activity a LEFT JOIN attendance b ON b.activityId = a.id
        GROUP BY a.id ORDER BY a.date DESC LIMIT 4
    """)
    fun perActivityStats(): Flow<List<DeptStat>>
}
