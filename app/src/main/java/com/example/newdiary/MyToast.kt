package com.example.newdiary

import android.content.Context
import android.widget.Toast

class MyToast {
    companion object {
        private var mToast: Toast? = null

        // todo 代码可以精简
        fun showToast(context: Context, msg: String, isLong: Boolean) {
            mToast = if (mToast == null) {
                if (isLong) {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG)
                } else {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT)
                }
            } else {
                mToast?.cancel()
                if (isLong) {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG)
                } else {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT)
                }
            }
            mToast?.show()
        }
    }

}