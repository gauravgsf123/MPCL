package com.mpcl.activity.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mpcl.R
import com.mpcl.activity.OptionActivity
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.custom.BoldTextView
import com.mpcl.custom.RegularButton
import com.mpcl.custom.RegularTextView
import com.mpcl.databinding.ActivityOtpVerifyBinding

class OTPVerifyActivity : BaseActivity() {
    private lateinit var binding:ActivityOtpVerifyBinding
    private lateinit var otp:String
    lateinit var dialog: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        otp = intent.getStringExtra(Constant.OTP)!!

        binding.btnVerify.setOnClickListener{
            if(otp==binding.pinview.value){
                showPopupMessage(getString(R.string.success),getString(R.string.your_registration_completed))
            }
        }

    }

    private fun showPopupMessage(titleText: String, messageText: String){
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this, R.style.PauseDialog)
        val customAlertView = LayoutInflater.from(this).inflate(
            R.layout.dialog_no_internet,
            null,
            false
        )
        val title: BoldTextView = customAlertView.findViewById(R.id.title)
        val message: RegularTextView = customAlertView.findViewById(R.id.message)
        val ok: RegularButton = customAlertView.findViewById(R.id.ok)
        materialAlertDialogBuilder.setView(customAlertView)
        materialAlertDialogBuilder.background = getDrawable(R.drawable.card_view)
        title.text = titleText
        message.text = messageText
        ok.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            sharedPreference.save(Constant.IS_LOGIN,true)
            val intent = Intent(this, OptionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        })



        dialog = materialAlertDialogBuilder.show()
    }
}