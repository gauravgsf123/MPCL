package com.mpcl.app

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mpcl.custom.MyAlertDialog


class ManagePermissions(val activity: Activity, val list: List<String>, val code:Int) {

    // Check permissions at runtime
    fun checkPermissions(): Boolean {
        var b = false
        if (isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
            showAlert()
        } else {
            b = true
        }

        return b
    }


    // Check permissions status
    private fun isPermissionsGranted(): Int {
        // PERMISSION_GRANTED : Constant Value: 0
        // PERMISSION_DENIED : Constant Value: -1
        var counter = 0;
        for (permission in list) {
            counter += ContextCompat.checkSelfPermission(activity, permission)
        }
        return counter
    }


    // Find the first denied permission
    private fun deniedPermission(): String {
        for (permission in list) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_DENIED) return permission
        }
        return ""
    }


    // Show alert dialog to request permissions
    private fun showAlert() {
        MyAlertDialog(activity,
            "Need permission(s)",
            "Some permissions are required to do the task.",
            "Ok",
            "Cancel",
            cb = object : MyAlertDialog.ClickListener {
                override fun onOk() {
                    requestPermissions()
                }

                override fun onCancel() {
                }

            }
        ).show()
    }


    // Request the permissions at run time
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(activity, list.toTypedArray(), code)
        /*val permission = deniedPermission()
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // Show an explanation asynchronously
            Toast.makeText(activity, "Please enable the permissions from setting", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
        } else {
            ActivityCompat.requestPermissions(activity, list.toTypedArray(), code)
        }*/
    }


    // Process permissions result
    fun processPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray): Boolean {
        var result = 0
        if (grantResults.isNotEmpty()) {
            for (item in grantResults) {
                result += item
            }
        }
        if (result == PackageManager.PERMISSION_GRANTED) return true
        return false
    }
}