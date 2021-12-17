package io.github.fast_startup.startup.demo.testaop

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.github.fast_startup.FastStartup
import io.github.fast_startup.IStartup
import io.github.fast_startup.annocation.AFastStartup
import io.github.fast_startup.config.StartupConfig
import io.github.fast_startup.startup.demo.TestBase
import io.github.fast_startup.startup.demo.TestTask
import io.github.fast_startup.listener.AllStartupCompleteListener
import io.github.fast_startup.listener.StartupCompleteListener

/**
 * Author: xuweiyu
 * Date: 2021/12/10
 * Email: wizz.xu@outlook.com
 * Description:
 */
class TestAop : TestBase {
    val aopStartups: MutableList<IStartup<*>> = mutableListOf()

    override fun test() {
        FastStartup.free()
        FastStartup.init(
            StartupConfig.Builder().setIsDebug(true).setLogLevel(Log.DEBUG).build()
        )
        FastStartup.registerStartupCompleteListener(object : StartupCompleteListener {
            override fun startupComplete(startup: IStartup<*>) {
                aopStartups.add(startup)
            }
        })

        FastStartup.registerAllStartupCompleteListener(object : AllStartupCompleteListener {
            override fun startupComplete() {
                if (aopStartups.size == 0) {
                    Log.e("场景测试", "测试依赖接口方式-->不通过")
                } else {
                    Log.e("AOP", aopStartups.toString())
                    Log.e("场景测试", "测试依赖接口方式-->通过")
                }
                Handler(Looper.getMainLooper()).postDelayed({ TestTask.startNext() }, 1000)
            }
        })

        FastStartup.start()
    }
}

interface IA<T> : IStartup<T>

@AFastStartup
class A : IA<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        return null
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return null
    }
}

@AFastStartup
class B : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        return null
    }

    override fun runOnUIThread(): Boolean {
        return true
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return listOf(IA::class.java)
    }
}

@AFastStartup
class C : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        return null
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return listOf(A::class.java)
    }

    override fun needUIThreadWait(): Boolean {
        return true
    }
}