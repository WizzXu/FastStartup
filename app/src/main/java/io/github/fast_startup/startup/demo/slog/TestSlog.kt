package io.github.fast_startup.startup.demo.slog

import android.util.Log
import io.github.fast_startup.log.SLog

/**
 * Author: xuweiyu
 * Date: 2021/12/8
 * Email: wizz.xu@outlook.com
 * Description:
 */
class TestSlog {
    fun test(){
        val o = Object()
        SLog.init(Log.DEBUG)
        SLog.d(1)

        SLog.d(mutableListOf(1,2,3))
        SLog.d(mapOf("1" to 1, 2 to "456"))
        SLog.d("string")
    }

}