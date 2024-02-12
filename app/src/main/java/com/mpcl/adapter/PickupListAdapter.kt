package com.mpcl.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import com.mpcl.R
import com.mpcl.activity.pickup.PickupUpdateActivity
import com.mpcl.databinding.ItemPickupListBinding
import com.mpcl.model.PickupResponseModel

class PickupListAdapter(var list:ArrayList<PickupResponseModel>):RecyclerView.Adapter<PickupListAdapter.MyViewModel>() {
    class MyViewModel(var binding: ItemPickupListBinding, var context: Context):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewModel {
        var binding = ItemPickupListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewModel(binding,parent.context)
    }

    override fun onBindViewHolder(holder: MyViewModel, position: Int) {
        var pickupResponseModel = list[position]
        holder.apply {
            binding.tvRequestNo.text = "${holder.context.getString(R.string.request_no)} ${pickupResponseModel.RequestNo}"
            binding.tvPickupDate.text = "${holder.context.getString(R.string.pickup_date)} ${pickupResponseModel.PickupDate}"
            binding.tvCustomerName.setText(pickupResponseModel.CustName)
            binding.tvPickup.setText(pickupResponseModel.PickPincode)
            binding.tvContactPerson.setText(pickupResponseModel.ContactPerson)
            binding.tvContactNo.setText(pickupResponseModel.ContactNo)
            binding.tvPcs.setText(pickupResponseModel.TotalPcs)
            binding.tvInstruction.setText(pickupResponseModel.Instruction)

            binding.ivDropDown.setOnClickListener {
                toggle(binding.ivDropDown,binding.group)
            }
            binding.update.setOnClickListener {
                var intent = Intent(holder.context,PickupUpdateActivity::class.java)
                intent.putExtra("RequestNo",pickupResponseModel.RequestNo)
                holder.context.startActivity(intent)
            }
        }

    }

    override fun getItemCount() = list.size

    private fun toggle(ivDropDown: ImageView, group: Group,) {
        if (ivDropDown.rotation == 0f) {
            ivDropDown.rotation = 180f
            group.visibility = View.VISIBLE
        } else {
            ivDropDown.rotation = 0f
            group.visibility = View.GONE
        }
    }
}