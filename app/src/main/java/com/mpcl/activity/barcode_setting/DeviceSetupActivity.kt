package com.mpcl.activity.barcode_setting

import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.tscdll.TSCActivity
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.databinding.ActivityDeviceSetupBinding
import java.lang.Exception

class DeviceSetupActivity : BaseActivity() {
    var TscDll = TSCActivity()
    private lateinit var binding:ActivityDeviceSetupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topBar.ivHome.setOnClickListener {
            onBackPressed()
        }
        binding.tvLastConnectedDevice.text = sharedPreference.getValueString(Constant.MAC_ADDRESS)
        binding.btnNewAddDevice.setOnClickListener {
            binding.btnAddDevice.visibility = View.VISIBLE
            binding.tvMacId.visibility = View.VISIBLE
        }
        binding.btnAddDevice.setOnClickListener {
            sharedPreference.save(Constant.MAC_ADDRESS,binding.tvMacId.text.toString())
            binding.tvLastConnectedDevice.text = sharedPreference.getValueString(Constant.MAC_ADDRESS)!!
        }
        binding.btnTestDevice.setOnClickListener {
            printBarCode()
        }

    }

    private fun printBarCode() {
        var macAdd = sharedPreference.getValueString(Constant.MAC_ADDRESS)
        //showToast(macAdd.toString())
        Log.d("mac_address",macAdd.toString())
        try {
            TscDll.openport(sharedPreference.getValueString(Constant.MAC_ADDRESS)) //BT
            TscDll.sendcommand("SIZE 76 mm, 50 mm\r\n")
            TscDll.sendcommand("SPEED 6\r\n")
            TscDll.sendcommand("DENSITY 12\r\n")
            TscDll.sendcommand("CODEPAGE UTF-8\r\n")
            TscDll.sendcommand("SET TEAR ON\r\n")
            TscDll.clearbuffer()
            TscDll.sendcommand("BOX 0,0,866,866,5")
            TscDll.sendcommand("TEXT 100,300,\"ROMAN.TTF\",0,12,12,@1\r\n")
            TscDll.printerfont(15, 160, "5", 270, 1, 1, "TEST")
            TscDll.barcode(120, 50, "128", 100, 1, 0, 4, 5, "1111111111")
            TscDll.printerfont(
                10,
                190,
                "2",
                0,
                1,
                1,
                "---------------------------------------------"
            )
            TscDll.printerfont(50, 210, "4", 0, 1, 1, "0000000000")
            TscDll.printerfont(390, 210, "4", 0, 1, 1, "9999999999")
            TscDll.printerfont(
                10,
                240,
                "2",
                0,
                1,
                1,
                "---------------------------------------------"
            )
            TscDll.printerfont(30, 260, "4", 0, 1, 1, "00000")
            TscDll.printerfont(350, 260, "4", 0, 1, 1, "XXX-XXX")
            TscDll.printerfont(10, 310, "3", 0, 1, 1, "Testing Print")
            TscDll.printerfont(10, 360, "3", 0, 1, 1, "Testing Print")
            TscDll.printerfont(350, 360, "4", 0, 1, 1, "TEST")
            TscDll.printlabel(1, 1)
            TscDll.closeport(5000)
        } catch (ex: Exception) {
        }

    }
}