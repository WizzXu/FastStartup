package io.github.fast_startup.executor

import io.github.fast_startup.run.StartupRunnable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Author: xuweiyu
 * Date: 2021/12/3
 * Email: wizz.xu@outlook.com
 * Description:
 */
internal class ExecutorManager {

    private val defaultExecutor: ExecutorService =
        Executors.newCachedThreadPool(Executors.defaultThreadFactory())

    companion object {
        val instance by lazy { ExecutorManager() }
    }

    fun execute(runnable: StartupRunnable) {
        defaultExecutor.execute(runnable)
    }

    fun execute(runnable: Runnable) {
        defaultExecutor.execute(runnable)
    }

}