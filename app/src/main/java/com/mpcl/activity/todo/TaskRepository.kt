package com.mpcl.activity.todo

import android.content.Context
import androidx.lifecycle.LiveData
import com.mpcl.activity.todo.database.AppDatabase
import com.mpcl.activity.todo.database.TaskDao
import com.mpcl.activity.todo.database.TaskTable
import com.mpcl.util.CoroutineUtil
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepository(val context: Context) {

    private var db: AppDatabase = AppDatabase.getInstance(context.applicationContext)
    private var taskDao: TaskDao = db.TaskDao()
    private var tasks: LiveData<List<TaskTable>> = taskDao.getAllTask()
    //private lateinit var lectureCountList: LiveData<List<LectureCount>>

    init {
        //lectures = lectureDao.getLecturesByWeekday(weekday)
    }

    /*fun getLecturesByWeekday(weekday: Weekday): LiveData<List<TimeTable>> {
        return lectures
    }*/

    suspend fun addTask(taskTable: TaskTable): Long = withContext(Dispatchers.IO) {
        taskDao.addTask(taskTable)
    }

    fun updateTask(taskTable: TaskTable) {
        CoroutineUtil.io {
            taskDao.updateTask(taskTable)
        }
    }

    fun deleteTask(id: Int) {
        CoroutineUtil.io {
            taskDao.deleteTask(id)
        }
    }

    suspend fun getTaskByDate(date: String): List<TaskTable>? {
        return taskDao.getTaskByDate(date)
    }

    suspend fun getCompletedTask(date: String): List<TaskTable>? {
        return taskDao.getCompletedTask(date)
    }

    fun getAllTask():LiveData<List<TaskTable>>{
        return tasks
    }

    /*fun getLectureCountPerWeekday(): LiveData<List<LectureCount>> {
        return lectureCountList
    }*/
}