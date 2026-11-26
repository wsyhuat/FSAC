package com.example.fleetmanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user ORDER BY id")
    fun getAll(): Flow<List<UserEntity>>

    @Query("SELECT * FROM user WHERE username = :name LIMIT 1")
    suspend fun findByName(name: String): UserEntity?

    @Query("SELECT * FROM user WHERE id = :id")
    suspend fun findById(id: Long): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Delete
    suspend fun delete(user: UserEntity)
}

@Dao
interface DepartmentDao {
    @Query("SELECT * FROM department ORDER BY id")
    fun getAll(): Flow<List<DepartmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dept: DepartmentEntity): Long

    @Query("DELETE FROM department WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface MemberDao {
    @Query("SELECT * FROM member ORDER BY id")
    fun getAll(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM member WHERE id = :id")
    suspend fun findById(id: Long): MemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: MemberEntity): Long

    @Update
    suspend fun update(member: MemberEntity)

    @Delete
    suspend fun delete(member: MemberEntity)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM task ORDER BY id DESC")
    fun getAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task WHERE id = :id")
    suspend fun findById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity ORDER BY date DESC, id DESC")
    fun getAll(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activity WHERE id = :id")
    suspend fun findById(id: Long): ActivityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityEntity): Long

    @Update
    suspend fun update(activity: ActivityEntity)

    @Delete
    suspend fun delete(activity: ActivityEntity)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY id DESC")
    fun getAll(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE activityId = :activityId")
    fun getByActivity(activityId: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE activityId = :activityId AND memberId = :memberId LIMIT 1")
    suspend fun findOne(activityId: Long, memberId: Long): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AttendanceEntity): Long

    @Update
    suspend fun update(record: AttendanceEntity)

    @Delete
    suspend fun delete(record: AttendanceEntity)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcement ORDER BY pinned DESC, createdAt DESC, id DESC")
    fun getAll(): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcement WHERE id = :id")
    suspend fun findById(id: Long): AnnouncementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: AnnouncementEntity): Long

    @Update
    suspend fun update(item: AnnouncementEntity)

    @Delete
    suspend fun delete(item: AnnouncementEntity)
}

@Dao
interface ItemDao {
    @Query("SELECT * FROM item ORDER BY id DESC")
    fun getAll(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM item WHERE id = :id")
    suspend fun findById(id: Long): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemEntity): Long

    @Update
    suspend fun update(item: ItemEntity)

    @Delete
    suspend fun delete(item: ItemEntity)
}

@Dao
interface BorrowRecordDao {
    @Query("SELECT * FROM borrow_record ORDER BY id DESC")
    fun getAll(): Flow<List<BorrowRecordEntity>>

    @Query("SELECT * FROM borrow_record")
    suspend fun getAllOnce(): List<BorrowRecordEntity>

    @Query("SELECT * FROM borrow_record WHERE id = :id")
    suspend fun findById(id: Long): BorrowRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: BorrowRecordEntity): Long

    @Update
    suspend fun update(record: BorrowRecordEntity)

    @Delete
    suspend fun delete(record: BorrowRecordEntity)
}
