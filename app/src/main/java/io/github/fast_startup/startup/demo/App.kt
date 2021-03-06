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
                .setIsDebug(BuildConfig.DEBUG)           //?????????debug
                .setParams(mapOf("key" to "value"))      //??????????????????
                .setEnableTimeStatistics(true)           //?????????????????????startup????????????(??????????????????Log.ERROR??????)
                .setLogLevel(Log.DEBUG)                  //??????????????????????????????
                .setStartupCompleteListener(object : StartupCompleteListener {
                    override fun startupComplete(startup: IStartup<*>) {
                        SLog.d("FastStartup", "???????????????startup???:" + startup::class.java.simpleName)
                    }

                })
                .setUIStartupCompleteListener(object : UIStartupCompleteListener {
                    override fun startupComplete() {
                        SLog.d("FastStartup", "???????????????UI???????????????UI???????????????Startup??????????????????")
                    }
                })
                .setAllStartupCompleteListener(object : AllStartupCompleteListener {
                    override fun startupComplete() {
                        SLog.d("FastStartup", "??????Startup??????????????????")
                    }
                })
                .build()
        ).start()*/

        // ?????????
        TestTask.addTask(TestCircle())
//
//        //???????????????
        TestTask.addTask(TestMissing())

        //????????????????????????
        TestTask.addTask(TestInterfaceImp())
//
//        //??????????????????
        TestTask.addTask(TestMultiAdd())

        TestTask.addTask(TestGetResult())

        //TestTask.addTask(TestAop())

        TestTask.addTask(TestPrivacy())

        //??????????????????????????????????????????
        //TestTask.addTask(TestWaitMainThread())

        TestTask.startNext()

        SLog.e("StartupRunnable", "all is ok")
    }
}