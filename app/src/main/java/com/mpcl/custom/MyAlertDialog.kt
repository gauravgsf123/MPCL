package com.mpcl.custom

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.mpcl.databinding.AlertDialogMessageBinding

class MyAlertDialog(context: Context, var title: String, var text: String,var confirmText: String,var cancelText:String, var cb: ClickListener) : AlertDialog(context) {
    private lateinit var binding: AlertDialogMessageBinding
    private var i: ClickListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AlertDialogMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.i = cb
        binding.titleText.text = title
        binding.contentText.text = text
        binding.confirmButton.text = confirmText
        binding.cancelButton.text = cancelText
        binding.confirmButton.setOnClickListener {
            i?.let {
                i!!.onOk()
            }
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            i?.let {
                i!!.onCancel()
            }
            dismiss()
        }
    }

    interface ClickListener{
        fun onOk()
        fun onCancel()
    }
}