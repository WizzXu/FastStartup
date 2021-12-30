package io.github.fast_startup.startup.demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.github.fast_startup.FastStartup
import io.github.fast_startup.IStartup
import io.github.fast_startup.listener.AllStartupCompleteListener
import io.github.fast_startup.listener.StartupCompleteListener
import io.github.fast_startup.listener.UIStartupCompleteListener
import io.github.fast_startup.log.SLog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun click(v: View){
        SLog.e("click")
        FastStartup.setPrivacyAgree(true)
            .registerAllStartupCompleteListener(object :
            AllStartupCompleteListener {
            override fun startupComplete() {
                SLog.e("registerAllStartupCompleteListener")
                //FastStartup.setPrivacyAgree(true)
                //FastStartup.reStart()
            }
        }).registerUIStartupCompleteListener(object : UIStartupCompleteListener {
            override fun startupComplete() {
                SLog.e("registerUIStartupCompleteListener")

            }
        }).registerStartupCompleteListener(object : StartupCompleteListener {
            override fun startupComplete(startup: IStartup<*>) {
                SLog.e("registerStartupCompleteListener:${startup.javaClass.simpleName}")
            }

        })
        FastStartup.reStart()
    }
}

