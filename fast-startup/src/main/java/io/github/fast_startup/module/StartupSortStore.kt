package io.github.fast_startup.module

import io.github.fast_startup.IStartup
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * Author: xuweiyu
 * Date: 2021/12/1
 * Email: wizz.xu@outlook.com
 * Description:
 */
internal data class StartupSortStore(

    // 入度为0工作在ui线程的startup的队列
    val uiZeroDeque: ConcurrentLinkedDeque<IStartup<*>>,

    // 入度为0工作在io线程的startup的队列
    val ioZeroDeque: ConcurrentLinkedDeque<IStartup<*>>,

    // 维护入度不为0的startup的依赖表
    // 当一个startup(A)完成后，其他依赖该startup(A)的依赖表需要删除该startup(A)并进行重新计算
    val startupChildrenMap: ConcurrentHashMap<String, MutableSet<String>>,

    // 工作在ui线程的startup的数量
    var uiThreadTaskSize: Int,

    // 需要ui线程等待的startup的数量
    var needUIThreadWaitTaskSize: Int,

    // 所有任务的数量
    var allTaskSize: Int,
) {
    fun free() {
        startupChildrenMap.clear()
    }
}