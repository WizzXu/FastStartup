package io.github.fast_startup.startup.demo.testmissing

import android.content.Context
import android.util.Log
import io.github.fast_startup.FastStartup
import io.github.fast_startup.IStartup
import io.github.fast_startup.config.StartupConfig
import io.github.fast_startup.startup.demo.TestBase
import io.github.fast_startup.startup.demo.TestTask
import io.github.fast_startup.exception.StartupException
import io.github.fast_startup.exception.StartupExceptionMsg

/**
 * Author: xuweiyu
 * Date: 2021/12/4
 * Email: wizz.xu@outlook.com
 * Description:
 */
class TestMissing : TestBase {
    override fun test() {
        val startups = mutableListOf<IStartup<*>>()
        startups.add(C())
        startups.add(A())

        try {
            FastStartup.free()
            FastStartup.init(StartupConfig.Builder().setIsDebug(true).setLogLevel(Log.DEBUG).build())
            FastStartup.start(startups)
            Log.e("场景测试", "依赖缺失检测-->不通过")
            TestTask.startNext()
        } catch (e: StartupException) {
            e.printStackTrace()
            if (e.stackTraceToString().contains(StartupExceptionMsg.DEP_CIRCLE_MISSING)) {
                Log.e("场景测试", "依赖缺失检测-->通过")
            } else {
                Log.e("场景测试", "依赖缺失检测-->其他异常")
            }
            TestTask.startNext()
        }
    }
}

class A : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {

        return null
    }

    override fun runOnUIThread(): Boolean {
        return true
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

class C : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {

        return null
    }

    override fun runOnUIThread(): Boolean {
        return true
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return listOf(B::class.java)
    }
}