package io.github.fast_startup.startup.demo.testinterfaceimp

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.UiThread
import io.github.fast_startup.FastStartup
import io.github.fast_startup.IStartup
import io.github.fast_startup.config.StartupConfig
import io.github.fast_startup.startup.demo.TestBase
import io.github.fast_startup.startup.demo.TestTask
import io.github.fast_startup.exception.StartupException
import io.github.fast_startup.listener.AllStartupCompleteListener

/**
 * Author: xuweiyu
 * Date: 2021/12/4
 * Email: wizz.xu@outlook.com
 * Description:
 */
class TestInterfaceImp : TestBase {
    override fun test() {
        val startups = mutableListOf<IStartup<*>>()
        startups.add(C())
        startups.add(A())
        startups.add(B())

        try {
            FastStartup.free()
            FastStartup.init(
                StartupConfig.Builder().setIsDebug(true).setLogLevel(Log.DEBUG)
                    .setAllStartupCompleteListener(object : AllStartupCompleteListener {
                        override fun startupComplete() {
                            Log.e("场景测试", "测试依赖接口方式-->通过")
                            Handler(Looper.getMainLooper()).postDelayed({ TestTask.startNext() }, 1000)
                        }
                    }).build()
            )
            FastStartup.start(startups)
            TestTask.startNext()
        } catch (e: StartupException) {
            e.printStackTrace()
            Log.e("场景测试", "测试依赖接口方式-->不通过")
            TestTask.startNext()
        }
    }
}

interface IA<T> : IStartup<T>
class A : IA<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        return null
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return null
    }
}

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