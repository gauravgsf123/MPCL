package com.mpcl.activity.finder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.mpcl.R
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.databinding.ActivityFinderBinding
import com.mpcl.viewmodel.registrationViewModel.RegistrationRepositoty
import com.mpcl.viewmodel.registrationViewModel.RegistrationViewModel
import com.mpcl.viewmodel.registrationViewModel.RegistrationViewModelFactory

class FinderActivity : BaseActivity() {
    private lateinit var binding:ActivityFinderBinding
    private lateinit var finderRepository: FinderRepository
    private lateinit var finderViewModelFactory: FinderViewModelFactory
    private lateinit var finderViewModel: FinderViewModel
    private var documentType  = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFinderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topBar.ivHome.setOnClickListener {
            onBackPressed()
        }
        finderRepository = FinderRepository()
        finderViewModelFactory = FinderViewModelFactory(finderRepository)
        finderViewModel = ViewModelProvider(this, finderViewModelFactory).get(
            FinderViewModel::class.java
        )

        val pickupStatusOption = resources.getStringArray(R.array.document_type_option)
        val pickupStatusOptionAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, pickupStatusOption)
        binding.type.setAdapter(pickupStatusOptionAdapter)
        binding.type.setText(binding.type.adapter.getItem(0).toString(), false)

        binding.type.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                if(documentType!=(parent.getItemAtPosition(position).toString()))
                    binding.group.visibility = View.GONE
                documentType = (parent.getItemAtPosition(position).toString())

            }

        binding.ivDownload.setOnClickListener {
            if(binding.etDocumentNo.text.toString().isNullOrEmpty()){
                Toast.makeText(this,getString(R.string.please_enter_document_number),Toast.LENGTH_LONG).show()
            }else if(documentType.isNullOrEmpty()){
                Toast.makeText(this,getString(R.string.please_select_document_type),Toast.LENGTH_LONG).show()
            }else {
                var body = mutableMapOf<String, String>()
                body["CID"] = sharedPreference.getValueString(Constant.COMPANY_ID)!!
                when (documentType) {
                    "CNOTE" -> {
                        body["STYPE"] = "CNOTE"
                        body["CNOTENO"] = binding.etDocumentNo.text.toString()
                    }
                    "BARCODE" -> {
                        body["STYPE"] = "BARCODE"
                        body["CNOTENO"] = binding.etDocumentNo.text.toString()
                    }
                    "INVOICE" -> {
                        body["STYPE"] = "INVOICE"
                        body["CNOTENO"] = binding.etDocumentNo.text.toString()
                    }
                }
                Log.d("body",body.toString())
                finderViewModel.getTrackData(body)
                showDialog()
            }
        }

        setObserver()
    }

    private fun setObserver() {
        finderViewModel.finderResponse.observe(this, androidx.lifecycle.Observer {
            hideDialog()
            if (it.isNotEmpty() && it[0].Response.isNullOrEmpty()) {
                binding.group.visibility = View.VISIBLE
                it[0].run {
                    binding.tvCNoteNo.text = CNOTENO
                    binding.tvCNoteDate.text = CNOTEDATE
                    binding.tvEDD.text = EDDDATE
                    binding.tvAddDate.text = ADDATE
                    binding.tvOrigin.text = ORIGIN
                    binding.tvAddDestination.text = DESTINATION
                    binding.tvConsignor.text = CONSIGNOR
                    binding.tvConsignee.text = CONSIGNOR
                    binding.tvCurrentStatus.text = CURRENTSTATUS
                }

            }
        })
    }
}