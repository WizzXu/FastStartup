package io.github.fast_startup.dispatcher

import io.github.fast_startup.IStartup
import io.github.fast_startup.config.StartupConfig
import io.github.fast_startup.executor.ExecutorManager
import io.github.fast_startup.extensions.getUniqueKey
import io.github.fast_startup.listener.AllStartupCompleteListener
import io.github.fast_startup.listener.StartupCompleteListener
import io.github.fast_startup.listener.UIStartupCompleteListener
import io.github.fast_startup.module.StartupInfoStore
import io.github.fast_startup.module.StartupSortStore
import io.github.fast_startup.run.StartupRunnable
import io.github.fast_startup.utils.StartupCostTimesUtil
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

/**
 * Author: xuweiyu
 * Date: 2021/12/3
 * Email: wizz.xu@outlook.com
 * Description:
 */
internal class DefaultDispatcher(
    private val startupConfig: StartupConfig?,
    private val startupInfoStore: StartupInfoStore
) : IDispatcher {

    private lateinit var startupSortStore: StartupSortStore

    // 工作在ui线程的startup的数量
    private val uiThreadTaskSize = AtomicInteger()

    // 所有startup的数量
    private val allTaskSize = AtomicInteger()

    // 需要ui线程等待的startup的数量
    private val needUIThreadWaitTaskSize = AtomicInteger()

    // ui线程startup执行完毕的回调
    var uiStartupCompleteListener: UIStartupCompleteListener? = null

    // 所有的startup执行完毕的回调
    var allStartupCompleteListener: AllStartupCompleteListener? = null

    // 每一个startup执行完毕的回调
    var startupCompleteListener: StartupCompleteListener? = null

    private var countDownLatch: CountDownLatch? = null

    private var startupCostTimesUtils: StartupCostTimesUtil? = null


    fun start(startupSortStore: StartupSortStore, startupCostTimesUtils: StartupCostTimesUtil?) {
        this.startupCostTimesUtils = startupCostTimesUtils

        uiThreadTaskSize.addAndGet(startupSortStore.uiThreadTaskSize)
        allTaskSize.addAndGet(startupSortStore.allTaskSize)
        needUIThreadWaitTaskSize.addAndGet(startupSortStore.needUIThreadWaitTaskSize)
        this.startupSortStore = startupSortStore

        dispatchOnIO()
        dispatchOnMain()
        uiStartupCompleteListener?.startupComplete()
    }

    private fun dispatchOnIO() {
        var startup = startupSortStore.ioZeroDeque.removeFirstOrNull()
        while (startup != null) {
            ExecutorManager.instance.execute(
                StartupRunnable(
                    startup, startupConfig, startupSortStore, startupInfoStore, this,
                    startupCostTimesUtils
                )
            )
            startup = startupSortStore.ioZeroDeque.removeFirstOrNull()
        }
    }

    private fun dispatchOnMain() {
        var startup: IStartup<*>? = startupSortStore.uiZeroDeque.removeFirstOrNull()
        while (uiThreadTaskSize.get() > 0 || needUIThreadWaitTaskSize.get() > 0) {
            if (startup != null) {
                StartupRunnable(
                    startup, startupConfig, startupSortStore, startupInfoStore, this,
                    startupCostTimesUtils
                ).run()
            } else {
                countDownLatch = CountDownLatch(1)
                countDownLatch?.await()
            }
            countDownLatch = null
            startup = startupSortStore.uiZeroDeque.removeFirstOrNull()
        }
        this.startupCostTimesUtils?.recordUIThreadStartupsEnd()
    }

    @Synchronized
    override fun dispatch(completeStartup: IStartup<*>, startupSortStore: StartupSortStore) {
        val uniqueKey = completeStartup.javaClass.getUniqueKey()
        val its = startupSortStore.startupChildrenMap.iterator()
        if (completeStartup.runOnUIThread()) {
            uiThreadTaskSize.decrementAndGet()
        }

        startupCompleteListener?.startupComplete(completeStartup)
        // 移除所有需要依赖该startup的 依赖关联
        startupInfoStore.allStartupDependenciesList.values.forEach {
            it.remove(uniqueKey)
        }

        while (its.hasNext()) {
            val entry = its.next()
            entry.value.remove(uniqueKey)
            if (entry.value.isEmpty()) {
                its.remove()
                startupInfoStore.startupMap[entry.key]?.let { startup ->
                    if (startup.runOnUIThread()) {
                        startupSortStore.uiZeroDeque.add(startup)
                    } else {
                        startupSortStore.ioZeroDeque.add(startup)
                    }
                }
            }
        }
        // 如果UI线程队列有任务，唤醒UI线程
        if (startupSortStore.uiZeroDeque.isNotEmpty()) {
            countDownLatch?.countDown()
        } else {
            this.startupCostTimesUtils?.recordUIThreadStartupsEnd()
        }

        if (completeStartup.needUIThreadWait()) {
            if (needUIThreadWaitTaskSize.decrementAndGet() < 1) {
                this.startupCostTimesUtils?.recordUIThreadStartupsEnd()
                countDownLatch?.countDown()
            }
        }

        if (allTaskSize.decrementAndGet() > 0) {
            dispatchOnIO()
        } else {
            startupCostTimesUtils?.recordAllStartupsEnd()
            allStartupCompleteListener?.startupComplete()
            startupCostTimesUtils?.printAll()
        }
    }
}