package io.github.fast_startup.sort

import io.github.fast_startup.IStartup
import io.github.fast_startup.exception.StartupException
import io.github.fast_startup.exception.StartupExceptionMsg
import io.github.fast_startup.executor.ExecutorManager
import io.github.fast_startup.extensions.getUniqueKey
import io.github.fast_startup.module.StartupSortStore
import io.github.fast_startup.utils.DependenciesListCheckUtil
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * Author: xuweiyu
 * Date: 2021/12/1
 * Email: wizz.xu@outlook.com
 * Description:
 */
internal object SortUtil {

    /**
     * 排序的方法
     */
    fun sort(startupList: List<IStartup<*>>, isDebug: Boolean?): StartupSortStore {
        // startup的集合 key为uniqueKey value为startup对象
        val startupMap: ConcurrentHashMap<String, IStartup<*>> = ConcurrentHashMap(startupList.size)

        // 工作在ui线程的startup的队列
        val uiZeroDeque: ConcurrentLinkedDeque<IStartup<*>> = ConcurrentLinkedDeque()

        // 工作在io线程的startup的队列
        val ioZeroDeque: ConcurrentLinkedDeque<IStartup<*>> = ConcurrentLinkedDeque()

        val startupInterfaceMap = mutableMapOf<Class<*>, Class<out IStartup<*>>>()

        // 维护入度不为0的startup的依赖表
        // 当一个startup(A)完成后，其他依赖该startup(A)的依赖表需要删除该startup(A)并进行重新计算
        val startupChildrenMap: ConcurrentHashMap<String, MutableSet<String>> =
            ConcurrentHashMap()

        // 工作在ui线程的startup的数量
        var uiThreadTaskSize = 0

        // 需要ui线程等待的startup的数量
        var needUIThreadWaitTaskSize = 0

        // 构建startupInterfaceMap依赖关系表
        startupList.forEach { startup ->
            startup::class.java.interfaces.forEach {
                if (it.interfaces.contains(IStartup::class.java)) {
                    startupInterfaceMap[it] = startup.javaClass
                }
            }
        }
        // 初次计算运行的主线程和IO线程的入度为0的startup，分别存储在uiZeroDeque和ioZeroDeque
        // 入度不为0的startup和他依赖的startup的依赖关系保存在startupChildrenMap中
        // 计算所有需要运行在主线程的startup的数量 和 需要主线程等待的startup的数量
        startupList.forEach {
            val uniqueKey = it::class.java.getUniqueKey()
            if (startupMap.containsKey(uniqueKey)) {
                throw StartupException("$uniqueKey ${StartupExceptionMsg.MULTI_ADD}")
            } else {
                // 保存所有startup
                startupMap[uniqueKey] = it
                // 计算统计入度， 入度为0则保存到0度列表
                // 入度不为0，存放到startupChildrenMap并保存需要依赖的startup
                if (it.dependencies().isNullOrEmpty()) {
                    if (it.runOnUIThread()) {
                        uiZeroDeque.offer(it)
                    } else {
                        ioZeroDeque.offer(it)
                    }
                } else {
                    val dependency = mutableSetOf<String>()
                    it.dependencies()?.forEach { dep ->
                        if (dep.isInterface) {
                            val depModule = startupInterfaceMap[dep]
                            if (depModule != null) {
                                dependency.add(depModule.getUniqueKey())
                            } else {
                                throw StartupException("not fond ${dep.getUniqueKey()} implement")
                            }
                        } else {
                            dependency.add(dep.getUniqueKey())
                        }
                    }
                    startupChildrenMap[uniqueKey] = dependency
                }

                if (it.runOnUIThread()) {
                    uiThreadTaskSize++
                }

                if (it.needUIThreadWait()) {
                    needUIThreadWaitTaskSize++
                }
            }
        }
        // 下面开始进行环检测和依赖丢失情况的检测
        if (isDebug == true) {
            DependenciesListCheckUtil.dependenciesListCheck(startupList, startupInterfaceMap)
        } else {
            ExecutorManager.instance.execute {
                DependenciesListCheckUtil.dependenciesListCheck(startupList, startupInterfaceMap)
            }
        }

        return StartupSortStore(
            uiZeroDeque, ioZeroDeque, startupMap, startupInterfaceMap, ConcurrentHashMap(),
            startupChildrenMap, uiThreadTaskSize, needUIThreadWaitTaskSize
        )
    }
}