package com.mpcl.activity.todo.database

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "taskTable")
data class TaskTable(
    @PrimaryKey(autoGenerate = true)
    @NonNull var id: Int = 0,
    @NonNull var title: String,
    @NonNull var date: String,
    @NonNull var time: String,
    @NonNull var isComplete:Boolean,
    @NonNull var CID: String,
    @NonNull var BID: String,
    @NonNull var EMP: String,
    @NonNull var mobile:String)