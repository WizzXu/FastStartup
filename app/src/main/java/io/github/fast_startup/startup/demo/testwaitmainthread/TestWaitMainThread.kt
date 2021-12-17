package io.github.fast_startup.startup.demo.testwaitmainthread

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.github.fast_startup.FastStartup
import io.github.fast_startup.IStartup
import io.github.fast_startup.config.StartupConfig
import io.github.fast_startup.startup.demo.TestBase
import io.github.fast_startup.listener.AllStartupCompleteListener
import io.github.fast_startup.listener.UIStartupCompleteListener
import io.github.fast_startup.log.SLog
import io.github.fast_startup.startup.demo.TestTask

/**
 * Author: xuweiyu
 * Date: 2021/12/4
 * Email: wizz.xu@outlook.com
 * Description:
 */
class TestWaitMainThread : TestBase {
    override fun test() {
        var isUIThreadWaiting = true

        FastStartup.free()
        FastStartup.init(
            StartupConfig.Builder().setIsDebug(true).setLogLevel(Log.DEBUG).build()
        )

        val startups = mutableListOf<IStartup<*>>()
        startups.add(C {
            if (isUIThreadWaiting) {
                Log.e("场景测试", "子线程执行主线程等待-->通过")
            } else {
                Log.e("场景测试", "子线程执行主线程等待-->不通过")
            }
        })
        startups.add(A())
        startups.add(B())
        startups.add(D())
        startups.add(E())

        FastStartup.registerUIStartupCompleteListener(object : UIStartupCompleteListener {
            override fun startupComplete() {
                SLog.e("ui thread startup done")
                isUIThreadWaiting = false
            }
        })

        FastStartup.registerAllStartupCompleteListener(object : AllStartupCompleteListener {
            override fun startupComplete() {
                SLog.e("all startup done")
                Handler(Looper.getMainLooper()).postDelayed({ TestTask.startNext() }, 1000)
            }
        })

        FastStartup.start(startups)
    }
}

class A : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        Thread.sleep(3 * 1000)
        return null
    }
}

class B : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        Thread.sleep(3 * 1000)
        return null
    }

    override fun runOnUIThread(): Boolean {
        return true
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return listOf(A::class.java)
    }
}

class C(private val runnable: Runnable) : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        Thread.sleep(3 * 1000)
        return null
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return listOf(B::class.java)
    }

    override fun needUIThreadWait(): Boolean {
        return true
    }

    override fun onStartCompleted() {
        super.onStartCompleted()
        runnable.run()
    }

}

class D : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        Thread.sleep(3 * 1000)
        return null
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return listOf(E::class.java)
    }
}

class E : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        Thread.sleep(3 * 1000)
        return null
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return listOf(C::class.java)
    }
}