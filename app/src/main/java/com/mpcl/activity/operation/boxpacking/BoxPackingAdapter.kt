package com.mpcl.activity.operation.boxpacking

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.mpcl.R
import com.mpcl.custom.RegularTextView
import com.mpcl.custom.SemiBoldTextView
import com.mpcl.database.VehicleListData

class BoxPackingAdapter : RecyclerView.Adapter<BoxPackingAdapter.MyViewHolder>() {
    var itemClick: ((VehicleListData) -> Unit)? = null
    private var stockList = listOf<String>()
    private lateinit var context: Context
    fun setItems(stockList: ArrayList<String>, context: Context) {
        this.stockList = stockList
        this.context=context
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_box_packing_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val viewHolder = holder
        viewHolder.bindView(stockList.get(position),position,context)
    }

    override fun getItemCount(): Int {
        return stockList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemClick: ((String) -> Unit)? = null
        private lateinit var tvSlNo: SemiBoldTextView
        private lateinit var tvSKUBarcode: RegularTextView
        private lateinit var constraintLayout: ConstraintLayout
        fun bindView(stock: String, i: Int, context: Context) {
            tvSlNo = itemView.findViewById(R.id.tvSlNo)
            tvSKUBarcode = itemView.findViewById(R.id.tvSKUBarcode)
            constraintLayout = itemView.findViewById(R.id.constraintLayout)
            tvSlNo.text = (i+1).toString()
            tvSKUBarcode.text = stock

            /*if(i%2==0){
                constraintLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.green_light))
            }else{
                constraintLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.red_light))
            }*/

        }
    }
}