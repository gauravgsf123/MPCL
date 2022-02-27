package com.mpcl.database

import android.content.Context
import com.mpcl.database.MyObjectBox
import io.objectbox.BoxStore

object ObjectBox  {
    lateinit var boxStore: BoxStore

    //lateinit var boxStore: BoxStore
        private set

    fun init(context: Context) {
        //if(boxStore==null){
            boxStore = MyObjectBox.builder()
                .androidContext(context.applicationContext)
                .build()
        //}

    }

    /*fun init(context: Context) {
            boxStore = MyObjectBox.builder()
                .androidContext(context.applicationContext)
                .build()


    }
    fun get(): BoxStore? {
        return boxStore
    }*/
    /*fun get(): BoxStore? {
        return boxStore
    }*/

}