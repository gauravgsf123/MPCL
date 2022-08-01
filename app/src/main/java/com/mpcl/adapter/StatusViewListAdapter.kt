package com.mpcl.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mpcl.R
import com.mpcl.database.VehicleListData
import com.mpcl.databinding.ItemStatusViewListBinding
import com.mpcl.model.ScanDocTotalResponseModel

class StatusViewListAdapter : RecyclerView.Adapter<StatusViewListAdapter.MyViewHolder>() {
    var itemClick: ((ScanDocTotalResponseModel) -> Unit)? = null
    private var stockList = listOf<ScanDocTotalResponseModel>()
    private lateinit var context: Context
    fun setItems(stockList: List<ScanDocTotalResponseModel>, context: Context) {
        this.stockList = stockList
        this.context=context
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_status_view_list, parent, false)
        ).apply {
            itemClick = { i ->
                this@StatusViewListAdapter.itemClick?.invoke(i)
            }
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val viewHolder = holder
        viewHolder.bindView(stockList.get(position),position,context)
    }

    override fun getItemCount(): Int {
        return stockList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemClick: ((ScanDocTotalResponseModel) -> Unit)? = null
        private var binding = ItemStatusViewListBinding.bind(itemView)
        fun bindView(data: ScanDocTotalResponseModel, i: Int, context: Context) {

            binding.scanBox.text = data.ScanBox.toString()
            binding.totalBox.text = data.TotalBox.toString()
            binding.cNoteNumber.text = data.CNoteNo.toString()


        }
    }
}