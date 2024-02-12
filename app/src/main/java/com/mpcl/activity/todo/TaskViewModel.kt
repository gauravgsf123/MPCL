package com.mpcl.activity.todo

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mpcl.activity.todo.database.TaskTable
import kotlinx.coroutines.launch

class TaskViewModel(val context: Context) :ViewModel() {


    private val taskRepository = TaskRepository(context)
    var lastId=MutableLiveData<Long>()
    var allTask : LiveData<List<TaskTable>> = taskRepository.getAllTask()
    var taskList = MutableLiveData<List<TaskTable>>()
    var completedTaskList = MutableLiveData<List<TaskTable>>()
    fun addTask(taskTable: TaskTable){
        viewModelScope.launch {
            lastId.value = taskRepository.addTask(taskTable)
        }
        //return lastId
    }

    fun getTaskByDate(date:String) {
        viewModelScope.launch {
            taskList.value = taskRepository.getTaskByDate(date)
        }
    }

    fun getCompletedTask(date:String) {
        viewModelScope.launch {
            completedTaskList.value = taskRepository.getCompletedTask(date)
            var str = Gson().toJson(taskList.value)
            Log.d("filter_task : ",""+str)
            // allTask.value = response.value
        }
    }
}