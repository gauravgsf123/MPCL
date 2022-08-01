package com.mpcl.activity.pickup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.mpcl.R
import com.mpcl.databinding.ActivityPickupUpdateBinding
import java.util.*

class PickupUpdateActivity : AppCompatActivity() {
    private lateinit var binding :ActivityPickupUpdateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickupUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var pickupStatusOption = arrayListOf("Select","Pickup Done","Pickup Not Done")
        val pickupStatusOptionAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, pickupStatusOption)
        binding.type.setAdapter(pickupStatusOptionAdapter)
        var pickupNotDoneReason = arrayListOf("Select","Consignment Not Ready","Give to Compititor","Refuse by Consignor","Document Not Ready","Delay by Branch/PDA","Other")
        val pickupNotDoneReasonAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, pickupNotDoneReason)
        binding.reason.setAdapter(pickupNotDoneReasonAdapter)
    }
}