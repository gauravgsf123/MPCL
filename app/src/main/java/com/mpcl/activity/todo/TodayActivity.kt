package com.mpcl.activity.todo


import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.mpcl.R
import com.mpcl.activity.todo.database.TaskTable
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.databinding.ActivityTodayBinding
import com.mpcl.databinding.ViewAddTaskBinding
import com.mpcl.receiver.AlertReceiver
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class TodayActivity : BaseActivity(),OnClickListener {
    private lateinit var binding: ActivityTodayBinding
    private lateinit var taskViewModelFactory: TaskViewModelFactory
    private lateinit var viewModel: TaskViewModel
    private lateinit var alarmManager: AlarmManager
    private lateinit var adapter: TaskAdapter
    private lateinit var list: List<TaskTable>
    private lateinit var activityType :String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        taskViewModelFactory = TaskViewModelFactory(this)
        adapter = TaskAdapter(listOf())
        binding.rvTaskList.adapter = adapter
        binding.fbAdd.setOnClickListener(this)
        binding.fbCalender.setOnClickListener(this)
        viewModel = ViewModelProvider(this,taskViewModelFactory)[TaskViewModel::class.java]
        binding.topBar.ivHome.setImageResource(R.drawable.ic_arrow_back)
        binding.topBar.ivHome.setOnClickListener {
            onBackPressed()
        }
        activityType = intent.getStringExtra(Constant.INTENT_TYPE)!!

        binding.etSearch.doOnTextChanged { text, start, before, count ->
            filter(text.toString())
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            adapter.addItem(list)
            adapter.notifyDataSetChanged()
        }

        when(activityType){
            Constant.ActivityType.TODAY->{
                binding.fbCalender.visibility = View.GONE
                val sdf = SimpleDateFormat("d/M/yyyy")
                val currentDate = sdf.format(Date())
                filterTaskByDate(currentDate)
            }
            Constant.ActivityType.SCHEDULED->{}
            Constant.ActivityType.ALL->{
                viewModel.allTask.observe(this, androidx.lifecycle.Observer {
                    list = it.reversed()
                    adapter.addItem(list)
                })
            }
            Constant.ActivityType.COMPLETED->{
                val sdf = SimpleDateFormat("d/M/yyyy")
                val currentDate = sdf.format(Date())
                viewModel.getCompletedTask(currentDate)
                viewModel.completedTaskList.observe(this) {
                    list = it.reversed()
                    adapter.addItem(list)
                }
            }
        }

    }

    override fun onClick(view: View?) {
        when(view?.id){
            binding.fbAdd.id->addTaskDialog()
            binding.fbCalender.id->selectDate()
        }
    }

    private fun addTaskDialog(){
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val alertDialog: AlertDialog = dialogBuilder.create()
        alertDialog.show()
        alertDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)

        //alertDialog.window?.setLayout(800, 1400)
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.view_add_task, null)
        val binding = ViewAddTaskBinding.bind(dialogView)
        alertDialog.window?.setContentView(binding.root)

        binding.ivClose.setOnClickListener {
            alertDialog.dismiss()
        }
        binding.tvDate.setOnClickListener {
            selectDate(binding.tvDate)
        }
        getTime(binding.tvTime, this)
        binding.submit.setOnClickListener {
            var task = TaskTable(
                0,binding.etTask.text.toString(),
                binding.tvDate.text.toString(),
                binding.tvTime.text.toString(),
                false,
                sharedPreference.getValueString(Constant.COMPANY_ID)!!,
                sharedPreference.getValueString(Constant.BID)!!,
                sharedPreference.getValueString(Constant.EMP_NO)!!,
                sharedPreference.getValueString(Constant.MOBILE)!!
            )
            var str = Gson().toJson(task)
            //Log.d("all_task",""+str)
            var lastId = viewModel.addTask(task)
            //viewModel.getAllTask()
            alertDialog.dismiss()

            val str_date = "${binding.tvDate.text.toString()} ${binding.tvTime.text.toString()}"
            val formatter: DateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")
            val date = formatter.parse(str_date) as Date
            println("Today is " + date.time)

            var timeInMillis = date.time
            viewModel.lastId.observe(this, androidx.lifecycle.Observer {
                println("lastId is $it")
                startAlarmManager(timeInMillis,it,binding.etTask.text.toString())
            })
            /*val calendar = Calendar.getInstance(Locale.ENGLISH)
            calendar.timeInMillis = date.time * 1000L

            println("Today timeInMillis " + getDate(date.time))*/

               // addTask(task)
        }

    }

    private fun startAlarmManager(time: Long, id: Long, title: String){
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent= Intent(this, AlertReceiver::class.java)
        intent.putExtra("id",id.toString())
        intent.putExtra("title",title)
        intent.putExtra("time",time.toString())
        Log.e("id of lecture",id.toString())
        val pendingIntent= PendingIntent.getBroadcast(this, id.toInt(),intent,0)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,time,pendingIntent)
    }

    private fun selectDate(textView: TextView) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            textView.text = "$dayOfMonth/${monthOfYear+1}/$year"

        }, year, month, day)

        dpd.show()
    }

    private fun selectDate() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            var date = "$dayOfMonth/${monthOfYear+1}/$year"
            Log.d("selectDate", "" + date)
            filterTaskByDate(date)
        }, year, month, day)

        dpd.show()
    }

    private fun filterTaskByDate(date:String){
        viewModel.getTaskByDate(date)
        viewModel.taskList.observe(this) {
            list = it.reversed()
            adapter.addItem(it.reversed())
            val str = Gson().toJson(it)
            Log.d("filter_task", "" + str)
        }
    }

    private fun getTime(textView: TextView, context: Context){

        val cal = Calendar.getInstance()

        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)

            textView.text = SimpleDateFormat("HH:mm").format(cal.time)
        }

        textView.setOnClickListener {
            TimePickerDialog(context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }
    }

    private fun filter(text: String) {
        // creating a new array list to filter our data.
        val filteredlist: ArrayList<TaskTable> = ArrayList()

        // running a for loop to compare elements.
        for (item in list) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.title.toLowerCase().contains(text.toLowerCase())) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            adapter.addItem(filteredlist)
        }
    }
}