package com.mpcl.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Utils {

    fun getDateTime(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        return sdf.format(Date());
    }

    fun milliseconds(date: String?): Long {
        //String date_ = date;
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        try {
            val mDate = sdf.parse(date)
            val timeInMilliseconds = mDate.time
            println("Date in milli :: $timeInMilliseconds")
            return timeInMilliseconds
        } catch (e: ParseException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        return 0
    }

    fun getCurrentDate(format: String?): String? {
        val sdf = SimpleDateFormat(format)
        return sdf.format(Calendar.getInstance().time)
    }

}