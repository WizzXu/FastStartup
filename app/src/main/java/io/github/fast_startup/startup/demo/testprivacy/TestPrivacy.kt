package io.github.fast_startup.startup.demo.testprivacy

import android.content.Context
import android.util.Log
import io.github.fast_startup.FastStartup
import io.github.fast_startup.IStartup
import io.github.fast_startup.config.StartupConfig
import io.github.fast_startup.listener.AllStartupCompleteListener
import io.github.fast_startup.listener.StartupCompleteListener
import io.github.fast_startup.listener.UIStartupCompleteListener
import io.github.fast_startup.log.SLog
import io.github.fast_startup.startup.demo.TestBase

/**
 * Author: xuweiyu
 * Date: 2021/12/29
 * Description:
 */
class TestPrivacy : TestBase {
    override fun test() {
        val startups = mutableListOf<IStartup<*>>()
        //startups.add(C())
        startups.add(A())
        startups.add(B())
        startups.add(D())
        startups.add(E())

        FastStartup.free()
        FastStartup.init(
            StartupConfig.Builder().setIsDebug(true).setEnableTimeStatistics(true)
                .setLogLevel(Log.DEBUG)
                .setIsPrintDependencies(true)
                .build()
        ).registerAllStartupCompleteListener(object : AllStartupCompleteListener {
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
            .start(startups)
    }
}

class A : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        //SLog.e("A()")
        return null
    }

    override fun needPrivacyAgree(): Boolean {
        return true
    }
}

class B : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        //SLog.e("B()")

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
        SLog.e("c()")

        return null
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return listOf(A::class.java)
    }

    override fun needPrivacyAgree(): Boolean {
        return true
    }
}

class D : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        //SLog.e("D()")

        return null
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return null
    }

    override fun runOnUIThread(): Boolean {
        return false
    }
}

class E : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        //SLog.e("E()")

        return null
    }

    override fun runOnUIThread(): Boolean {
        return false
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return listOf(D::class.java)
    }
}