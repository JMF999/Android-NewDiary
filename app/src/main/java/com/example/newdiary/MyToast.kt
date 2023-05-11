package com.example.newdiary

import android.content.Context
import android.widget.Toast

class MyToast {
    companion object{
        private var mToast: Toast? = null
        fun showToast(context: Context, msg: String) {
            mToast = if (mToast == null){
                Toast.makeText(context, msg, Toast.LENGTH_LONG)
            }else{
                mToast?.cancel()
                Toast.makeText(context, msg, Toast.LENGTH_LONG)
            }
            mToast?.show()
        }
    }

}