package io.github.fast_startup.startup.demo.testmultiadd

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.github.fast_startup.FastStartup
import io.github.fast_startup.IStartup
import io.github.fast_startup.config.StartupConfig
import io.github.fast_startup.startup.demo.TestBase
import io.github.fast_startup.startup.demo.TestTask
import io.github.fast_startup.exception.StartupException
import io.github.fast_startup.exception.StartupExceptionMsg
import io.github.fast_startup.listener.AllStartupCompleteListener

/**
 * Author: xuweiyu
 * Date: 2021/12/5
 * Email: wizz.xu@outlook.com
 * Description:
 */
class TestMultiAdd : TestBase {
    override fun test() {
        val startups = mutableListOf<IStartup<*>>()
        startups.add(B())
        startups.add(A())
        startups.add(B())

        try {
            FastStartup.free()
            FastStartup.init(
                StartupConfig.Builder().setIsDebug(true).setLogLevel(Log.DEBUG)
                    .setAllStartupCompleteListener(object : AllStartupCompleteListener {
                        override fun startupComplete() {
                            Log.e("场景测试", "测试多次添加同一个组件-->不通过")
                            Handler(Looper.getMainLooper()).postDelayed({ TestTask.startNext() }, 1000)
                        }
                    }).build()
            )
            FastStartup.start(startups)
        } catch (e: StartupException) {
            e.printStackTrace()
            if (e.stackTraceToString().contains(StartupExceptionMsg.MULTI_ADD)) {
                Log.e("场景测试", "测试多次添加同一个组件-->通过")
            } else {
                Log.e("场景测试", "测试多次添加同一个组件-->其他异常")
            }
            TestTask.startNext()
        }
    }
}

class A : IStartup<String> {
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
        return listOf(A::class.java)
    }
}
