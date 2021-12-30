package io.github.fast_startup.run

import io.github.fast_startup.IStartup
import io.github.fast_startup.config.StartupConfig
import io.github.fast_startup.dispatcher.IDispatcher
import io.github.fast_startup.extensions.getUniqueKey
import io.github.fast_startup.log.SLog
import io.github.fast_startup.module.StartupInfoStore
import io.github.fast_startup.module.StartupSortStore
import io.github.fast_startup.utils.StartupCostTimesUtil

/**
 * Author: xuweiyu
 * Date: 2021/12/3
 * Email: wizz.xu@outlook.com
 * Description:
 */
internal class StartupRunnable(
    private val startup: IStartup<*>, private val startupConfig: StartupConfig?,
    private val startupSortStore: StartupSortStore,
    private val startupInfoStore: StartupInfoStore,
    private val dispatcher: IDispatcher,
    private val startupCostTimesUtils: StartupCostTimesUtil? = null
) : Runnable {
    override fun run() {
        startup.onStartPrepare()
        SLog.d("开始执行：${startup.javaClass.getUniqueKey()} Thread:${Thread.currentThread().name}")
        startupCostTimesUtils?.recordStart(startup)
        startup.start(startupConfig?.application, startupConfig?.isDebug, startupConfig?.params)
            ?.let {
                startupInfoStore.startupResultMap[startup.javaClass.getUniqueKey()] = it
            }
        startupCostTimesUtils?.recordEnd(startup)
        SLog.d("结束执行：${startup.javaClass.getUniqueKey()} Thread:${Thread.currentThread().name}")
        startup.onStartCompleted()
        dispatcher.dispatch(startup, startupSortStore)
    }
}