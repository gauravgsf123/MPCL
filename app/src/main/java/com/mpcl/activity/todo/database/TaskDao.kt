package com.mpcl.activity.todo.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTask(lec: TaskTable):Long

    @Update
    suspend fun updateTask(lec: TaskTable)

    @Query("SELECT * FROM taskTable")
    fun getAllTask(): LiveData<List<TaskTable>>

    @Query("DELETE FROM taskTable WHERE id = :id")
    suspend fun deleteTask(id: Int)

    @Query("select * from taskTable where date= :date ORDER BY id")
    suspend fun getTaskByDate(date: String) : List<TaskTable>

    @Query("select * from taskTable where date!= :date ORDER BY id")
    suspend fun getCompletedTask(date: String) : List<TaskTable>

    /*@Query("SELECT date as dow, count(lec) as lecNo FROM timeTable GROUP BY dow")
    fun getTaskCountPerWeekday(): LiveData<List<TaskTable>>*/
}