package com.mpcl.activity.todo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mpcl.R
import com.mpcl.activity.todo.database.TaskTable
import com.mpcl.databinding.ItemTaskListBinding

class TaskAdapter(var list: List<TaskTable>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    var taskList: List<TaskTable> = listOf()
    class TaskViewHolder(var binding: ItemTaskListBinding): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        var binding = ItemTaskListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TaskViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addItem(list: List<TaskTable>){
        this.list = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        var item = list[position]
        holder.binding.tvTaskDate.text = item.date
        holder.binding.tvTaskTime.text = item.time
        holder.binding.tvTaskTitle.text = item.title
        if(position%2==0)
            holder.binding.clMain.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.light_orange))
        else holder.binding.clMain.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.light_gray))
    }

}