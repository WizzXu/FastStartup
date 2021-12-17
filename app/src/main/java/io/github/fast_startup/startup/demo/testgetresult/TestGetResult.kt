package io.github.fast_startup.startup.demo.testgetresult

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.github.fast_startup.FastStartup
import io.github.fast_startup.IStartup
import io.github.fast_startup.config.StartupConfig
import io.github.fast_startup.startup.demo.TestBase
import io.github.fast_startup.startup.demo.TestTask
import io.github.fast_startup.listener.AllStartupCompleteListener

/**
 * Author: xuweiyu
 * Date: 2021/12/10
 * Email: wizz.xu@outlook.com
 * Description:
 */
class TestGetResult : TestBase {
    override fun test() {
        val startups = mutableListOf<IStartup<*>>()
        startups.add(C())
        startups.add(A())
        startups.add(B())

        FastStartup.free()
        FastStartup.init(
            StartupConfig.Builder().setIsDebug(true).setLogLevel(Log.DEBUG).build()
        )

        FastStartup.registerAllStartupCompleteListener(object : AllStartupCompleteListener {
            override fun startupComplete() {
                val s1:A? = FastStartup.getStartup(A::class.java)
                val s11:IA<*>? = FastStartup.getStartup(A::class.java)
                val s12:IA<*>? = FastStartup.getStartup(IA::class.java)
                Log.e("TestGetResult s1", "${s1?.getStartupName()}")
                val s2 = FastStartup.getStartup(IA::class.java)
                Log.e("TestGetResult s2", "${s2?.getStartupName()}")
                Log.e("TestGetResult", "${FastStartup.getStartupResult(A::class.java)}")
                Log.e("TestGetResult", "${FastStartup.getStartupResult(IA::class.java)}")

                Log.e("TestGetResult", "${FastStartup.getStartupResult(IB::class.java)}")
                Log.e("TestGetResult", "${FastStartup.getStartup(IB::class.java)}")

                if (s1?.getStartupName() == "This is A" &&
                    s2?.getStartupName() == "This is A" &&
                    FastStartup.getStartup(IB::class.java)?.getStartupName() == null &&
                    FastStartup.getStartup(D::class.java) == null
                ) {
                    Log.e("场景测试", "测试通过接口类和实体类获取实例对象-->通过")
                } else {
                    Log.e("场景测试", "测试通过接口类和实体类获取实例对象-->通过")
                }
                if (FastStartup.getStartupResult(A::class.java) == "A start result" &&
                    FastStartup.getStartupResult(IA::class.java) == "A start result" &&
                    FastStartup.getStartupResult(IB::class.java) == null &&
                    FastStartup.getStartupResult(IB::class.java) == null
                ) {
                    Log.e("场景测试", "测试通过接口类和实体类获取start方法结果缓存-->通过")

                } else {
                    Log.e("场景测试", "测试通过接口类和实体类获取start方法结果缓存-->通过")

                }
                Handler(Looper.getMainLooper()).postDelayed({ TestTask.startNext() }, 1000)
            }
        })
        FastStartup.start(startups)
    }
}

interface IA<T> : IStartup<T> {
    fun getStartupName(): String
}

interface IB<T> : IStartup<T> {
    fun getStartupName(): String
}

class A : IA<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        return "A start result"
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return null
    }

    override fun getStartupName(): String {
        return "This is A"
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

class D : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        return null
    }
}