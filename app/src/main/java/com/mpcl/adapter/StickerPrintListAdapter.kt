package com.mpcl.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mpcl.R
import com.mpcl.custom.BoldTextView
import com.mpcl.custom.RegularTextView
import com.mpcl.model.StickerDataResponseModel

class StickerPrintListAdapter: RecyclerView.Adapter<StickerPrintListAdapter.MyViewHolder>() {
    var itemClick: ((StickerDataResponseModel) -> Unit)? = null
    private var stockList = listOf<StickerDataResponseModel>()
    private lateinit var context :Context
    fun setItems(stockList: List<StickerDataResponseModel>,context:Context) {
        this.context = context
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
        viewHolder.bindView(stockList.get(position),position,context)
    }

    override fun getItemCount(): Int {
        return stockList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemClick: ((StickerDataResponseModel) -> Unit)? = null
        private lateinit var cNoteNumber: BoldTextView
        private lateinit var cartonNo: BoldTextView
        private lateinit var constLayout: ConstraintLayout
        @SuppressLint("ResourceAsColor")
        fun bindView(model: StickerDataResponseModel, i: Int, context: Context) {
             cNoteNumber = itemView.findViewById(R.id.tv_c_note_number)
            cartonNo = itemView.findViewById(R.id.tv_carton_no)
            constLayout = itemView.findViewById(R.id.const_top)
            cNoteNumber.text = model.CNoteNo
            cartonNo.text = model.BarCodeNo
            if(model.printDone){
                constLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.green_light))
            }else{
                constLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.red_light))
            }

            itemView.setOnClickListener{
                itemClick?.invoke(model)
            }
        }
    }
}