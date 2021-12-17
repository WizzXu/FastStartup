package io.github.fast_startup.startup.demo.testcircle

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
class TestCircle : TestBase {

    override fun test() {
        testCircle()
    }

    fun testCircle() {
        val startups = mutableListOf<IStartup<*>>()
        startups.add(C())
        startups.add(A())
        startups.add(B())
        startups.add(D())
        startups.add(E())

        try {
            FastStartup.free()
            FastStartup.init(StartupConfig(null, true).apply { logLevel = Log.DEBUG }).start(startups)
            Log.e("场景测试", "环检测-->不通过")
            TestTask.startNext()
        } catch (e: StartupException) {
            e.printStackTrace()
            if (e.stackTraceToString().contains(StartupExceptionMsg.DEP_CIRCLE_MISSING)) {
                Log.e("场景测试", "环检测-->通过")
            } else {
                Log.e("场景测试", "环检测-->其他异常")
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

class D : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        return null
    }

    override fun runOnUIThread(): Boolean {
        return true
    }

    override fun dependencies(): List<Class<out IStartup<*>>> {
        return listOf(E::class.java)
    }
}

class E : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        return null
    }

    override fun runOnUIThread(): Boolean {
        return true
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return listOf(D::class.java)
    }
}