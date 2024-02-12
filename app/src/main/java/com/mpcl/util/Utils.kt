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

    fun getDate(time:Long):String {
        var date:Date = Date(time); // *1000 is to convert seconds to milliseconds
        var sdf:SimpleDateFormat  = SimpleDateFormat("dd/MM/yyyy HH:mm"); // the format of your date
        return sdf.format(date);
    }

    fun getDate(format: String): String {
        val sdf = SimpleDateFormat(format)
        return sdf.format(Date());
    }

    fun checkDate(date:String,calenderDate:String):Boolean{
        var enteredDate: Date? = null
        var currentDate: Date? = null
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            enteredDate = sdf.parse(date)
            currentDate = sdf.parse(calenderDate)
        } catch (ex: Exception) {
            // enteredDate will be null if date="287686";
        }

        return enteredDate?.before(currentDate) == true //|| enteredDate?.equals(currentDate)== true
    }

}