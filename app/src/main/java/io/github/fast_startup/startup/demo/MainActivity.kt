package io.github.fast_startup.startup.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.fast_startup.log.SLog
import io.github.fast_startup.startup.demo.testaop.TestAop
import io.github.fast_startup.startup.demo.testinterfaceimp.TestInterfaceImp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}

