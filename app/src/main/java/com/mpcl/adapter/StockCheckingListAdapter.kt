package com.mpcl.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mpcl.R
import com.mpcl.custom.RegularTextView
import com.mpcl.custom.SemiBoldTextView
import com.mpcl.database.StockCheckingDB

class StockCheckingListAdapter : RecyclerView.Adapter<StockCheckingListAdapter.MyViewHolder>() {
    var itemClick: ((StockCheckingDB) -> Unit)? = null
    private var stockList = listOf<StockCheckingDB>()
    fun setItems(stockList: List<StockCheckingDB>) {
        this.stockList = stockList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.stock_checking_list_item, parent, false)
        ).apply {
            itemClick = { i ->
                this@StockCheckingListAdapter.itemClick?.invoke(i)
            }
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val viewHolder = holder
        viewHolder.bindView(stockList.get(position),position)
    }

    override fun getItemCount(): Int {
        return stockList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemClick: ((StockCheckingDB) -> Unit)? = null
        private lateinit var slNo: SemiBoldTextView
        private lateinit var scanCode: RegularTextView
        fun bindView(stock: StockCheckingDB, i:Int) {
            slNo = itemView.findViewById(R.id.sl_no)
            scanCode = itemView.findViewById(R.id.scan_code)
            slNo.text = (i+1).toString()
            scanCode.text = stock.bar_code//.toString()

            itemView.setOnClickListener{
                itemClick?.invoke(stock)
            }
        }
    }
}