package com.mpcl.app

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.mpcl.model.RegistrationResponseModel

class SharedPreference(val context: Context) {

    val sharedPref: SharedPreferences = context.getSharedPreferences(Constant.PREFS_NAME, Context.MODE_PRIVATE)

    fun getUser(): RegistrationResponseModel? {
        val data: String? = sharedPref.getString(Constant.USER_LOGIN, null)
        return Gson().fromJson(data, RegistrationResponseModel::class.java)
    }

    fun save(KEY_NAME: String, text: String) {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putString(KEY_NAME, text)

        editor!!.commit()
    }

    fun save(KEY_NAME: String, value: Int) {
        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putInt(KEY_NAME, value)

        editor.commit()
    }

    fun save(KEY_NAME: String, status: Boolean) {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putBoolean(KEY_NAME, status!!)

        editor.commit()
    }

    fun getValueString(KEY_NAME: String): String? {

        return sharedPref.getString(KEY_NAME, "")

    }

    fun getValueInt(KEY_NAME: String): Int {

        return sharedPref.getInt(KEY_NAME, 0)
    }

    fun getValueBoolean(KEY_NAME: String, defaultValue: Boolean): Boolean {

        return sharedPref.getBoolean(KEY_NAME, defaultValue)

    }

    fun clearSharedPreference() {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        //sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        editor.clear()
        editor.commit()
    }

    fun removeValue(KEY_NAME: String) {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.remove(KEY_NAME)
        editor.commit()
    }
}