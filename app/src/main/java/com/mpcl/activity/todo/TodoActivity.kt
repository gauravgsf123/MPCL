package com.mpcl.activity.todo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.databinding.ActivityOptionBinding
import com.mpcl.databinding.ActivityTodoBinding
import java.text.SimpleDateFormat
import java.util.*

class TodoActivity : BaseActivity(),OnClickListener {
    private lateinit var binding : ActivityTodoBinding
    private lateinit var taskViewModelFactory: TaskViewModelFactory
    private lateinit var viewModel: TaskViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topbar.ivHome.setOnClickListener {
            onBackPressed()
        }
        taskViewModelFactory = TaskViewModelFactory(this)
        viewModel = ViewModelProvider(this,taskViewModelFactory)[TaskViewModel::class.java]
        val sdf = SimpleDateFormat("d/M/yyyy")
        val currentDate = sdf.format(Date())
        viewModel.getTaskByDate(currentDate)
        viewModel.taskList.observe(this) {
            binding.tvTodayCount.text = it.size.toString()
        }
        viewModel.allTask.observe(this, androidx.lifecycle.Observer {
            binding.tvAllCount.text = it.size.toString()
        })
        viewModel.getCompletedTask(currentDate)
        viewModel.completedTaskList.observe(this) {
            binding.tvCompletedCount.text = it.size.toString()
        }
        binding.cvToday.setOnClickListener(this)
        binding.cvScheduled.setOnClickListener(this)
        binding.cvAll.setOnClickListener(this)
        binding.cvCompleted.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            binding.cvToday.id ->{
                val intent = Intent(this,TodayActivity::class.java)
                intent.putExtra(Constant.INTENT_TYPE,Constant.ActivityType.TODAY)
                startActivity(intent)
            }
            binding.cvScheduled.id ->{
                val intent = Intent(this,TodayActivity::class.java)
                intent.putExtra(Constant.INTENT_TYPE,Constant.ActivityType.SCHEDULED)
                startActivity(intent)
            }
            binding.cvAll.id ->{
                val intent = Intent(this,TodayActivity::class.java)
                intent.putExtra(Constant.INTENT_TYPE,Constant.ActivityType.ALL)
                startActivity(intent)
            }
            binding.cvCompleted.id ->{
                val intent = Intent(this,TodayActivity::class.java)
                intent.putExtra(Constant.INTENT_TYPE,Constant.ActivityType.COMPLETED)
                startActivity(intent)
            }
        }
    }
}