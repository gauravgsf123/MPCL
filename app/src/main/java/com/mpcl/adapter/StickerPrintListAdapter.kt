package com.mpcl.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mpcl.R
import com.mpcl.custom.RegularTextView
import com.mpcl.model.StickerDataResponseModel

class StickerPrintListAdapter: RecyclerView.Adapter<StickerPrintListAdapter.MyViewHolder>() {
    var itemClick: ((StickerDataResponseModel) -> Unit)? = null
    private var stockList = listOf<StickerDataResponseModel>()
    fun setItems(stockList: List<StickerDataResponseModel>) {
        this.stockList = stockList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_sticker_list, parent, false)
        ).apply {
            itemClick = { i ->
                this@StickerPrintListAdapter.itemClick?.invoke(i)
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
        var itemClick: ((StickerDataResponseModel) -> Unit)? = null
        private lateinit var cNoteNumber: RegularTextView
        private lateinit var cartonNo: RegularTextView
        @SuppressLint("ResourceAsColor")
        fun bindView(model: StickerDataResponseModel, i:Int) {
             cNoteNumber = itemView.findViewById(R.id.tv_c_note_number)
            cartonNo = itemView.findViewById(R.id.tv_carton_no)
            cNoteNumber.text = model.CNoteNo
            cartonNo.text = model.BarCodeNo
            if(model.printDone){
                cNoteNumber.setTextColor(R.color.green)
            }else{
                cNoteNumber.setTextColor(R.color.red)
            }

            itemView.setOnClickListener{
                itemClick?.invoke(model)
            }
        }
    }
}