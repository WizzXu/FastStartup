package io.github.fast_startup.startup.demo

import android.app.Application
import io.github.fast_startup.log.SLog
import io.github.fast_startup.startup.demo.testaop.TestAop
import io.github.fast_startup.startup.demo.testcircle.TestCircle
import io.github.fast_startup.startup.demo.testgetresult.TestGetResult
import io.github.fast_startup.startup.demo.testinterfaceimp.TestInterfaceImp
import io.github.fast_startup.startup.demo.testmissing.TestMissing
import io.github.fast_startup.startup.demo.testmultiadd.TestMultiAdd
import io.github.fast_startup.startup.demo.testprivacy.TestPrivacy
import io.github.fast_startup.startup.demo.testwaitmainthread.TestWaitMainThread

/**
 * Author: xuweiyu
 * Date: 2021/12/15
 * Email: wizz.xu@outlook.com
 * Description:
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
//        FastStartup.init(
//            //null,
//            /*StartupConfig(application = application, isDebug = true, logLevel = Log.DEBUG,
//                enableTimeStatistics = true,
//
//                allStartupCompleteListener =
//                object : AllStartupCompleteListener {
//                    override fun startupComplete() {
//                        SLog.e("all task complete")
//                    }
//                },
//                startupCompleteListener = object : StartupCompleteListener {
//                    override fun startupComplete(startup: IStartup<*>) {
//                        SLog.e(startup.javaClass.simpleName + "complete")
//                    }
//                }, uiStartupCompleteListener = object : UIStartupCompleteListener{
//                    override fun startupComplete() {
//                        TODO("Not yet implemented")
//                    }
//
//                },)*/
//            StartupConfig.Builder().setApplication(this)
//                .setLogLevel(0)
//                .setIsDebug(true)
//                .setEnableTimeStatistics(true)
//                .build()
//        )
        /*FastStartup.init(StartupConfig(application = this, BuildConfig.DEBUG)).start()
        FastStartup.init(
            StartupConfig.Builder()
                .setApplication(this)                    //application
                .setIsDebug(BuildConfig.DEBUG)           //是否是debug
                .setParams(mapOf("key" to "value"))      //通用配置参数
                .setEnableTimeStatistics(true)           //是否打印每一个startup启动耗时(需要日志级别Log.ERROR以下)
                .setLogLevel(Log.DEBUG)                  //组件内打印的日志级别
                .setStartupCompleteListener(object : StartupCompleteListener {
                    override fun startupComplete(startup: IStartup<*>) {
                        SLog.d("FastStartup", "目前完成的startup为:" + startup::class.java.simpleName)
                    }

                })
                .setUIStartupCompleteListener(object : UIStartupCompleteListener {
                    override fun startupComplete() {
                        SLog.d("FastStartup", "所有运行在UI线程和需要UI线程等待的Startup已经全部完成")
                    }
                })
                .setAllStartupCompleteListener(object : AllStartupCompleteListener {
                    override fun startupComplete() {
                        SLog.d("FastStartup", "所有Startup已经全部完成")
                    }
                })
                .build()
        ).start()*/

        // 测试环
        //TestTask.addTask(TestCircle())
//
//        //测试依缺失
        //TestTask.addTask(TestMissing())
//
//        //测试依赖接口方式
        //TestTask.addTask(TestInterfaceImp())
//
//        //测试多次添加
        //TestTask.addTask(TestMultiAdd())

        //TestTask.addTask(TestGetResult())

        //TestTask.addTask(TestAop())

        TestTask.addTask(TestPrivacy())

        //测试主线程需要等待子线程完成
        //TestTask.addTask(TestWaitMainThread())

        TestTask.startNext()

        SLog.e("StartupRunnable", "all is ok")
    }
}