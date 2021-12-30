package io.github.fast_startup.sort

import io.github.fast_startup.IStartup
import io.github.fast_startup.config.StartupConfig
import io.github.fast_startup.exception.StartupException
import io.github.fast_startup.exception.StartupExceptionMsg
import io.github.fast_startup.extensions.getUniqueKey
import io.github.fast_startup.module.StartupInfoStore
import io.github.fast_startup.module.StartupSortStore
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.collections.set

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
    fun sort(
        startupList: ArrayDeque<IStartup<*>>, startupConfig: StartupConfig?,
        startupInfoStore: StartupInfoStore
    ): StartupSortStore {

        // 工作在ui线程的startup的队列
        val uiZeroDeque: ConcurrentLinkedDeque<IStartup<*>> = ConcurrentLinkedDeque()

        // 工作在io线程的startup的队列
        val ioZeroDeque: ConcurrentLinkedDeque<IStartup<*>> = ConcurrentLinkedDeque()

        // 维护入度不为0的startup的依赖表
        // 当一个startup(A)完成后，其他依赖该startup(A)的依赖表需要删除该startup(A)并进行重新计算
        val startupChildrenMap: ConcurrentHashMap<String, MutableSet<String>> =
            ConcurrentHashMap()

        // 工作在ui线程的startup的数量
        var uiThreadTaskSize = 0

        // 需要ui线程等待的startup的数量
        var needUIThreadWaitTaskSize = 0

        // 所有任务的数量
        val allTaskSize = startupList.size

        // 初次计算运行的主线程和IO线程的入度为0的startup，分别存储在uiZeroDeque和ioZeroDeque
        // 入度不为0的startup和他依赖的startup的依赖关系保存在startupChildrenMap中
        // 计算所有需要运行在主线程的startup的数量 和 需要主线程等待的startup的数量
        for (i in 1..allTaskSize) {
            startupList.removeFirstOrNull()?.let {
                val uniqueKey = it::class.java.getUniqueKey()
                // 符合隐私检测
                if (!it.needPrivacyAgree() || it.needPrivacyAgree() == startupConfig?.isPrivacyAgree) {
                    // 计算统计入度， 入度为0则保存到0度列表
                    // 入度不为0，存放到startupChildrenMap并保存需要依赖的startup
                    if (it.dependencies()
                            .isNullOrEmpty() || startupInfoStore.allStartupDependenciesList[uniqueKey].isNullOrEmpty()
                    ) {
                        if (it.runOnUIThread()) {
                            uiZeroDeque.offer(it)
                        } else {
                            ioZeroDeque.offer(it)
                        }
                    } else {
                        val dependency = mutableSetOf<String>()
                        it.dependencies()?.forEach { dep ->
                            if (dep.isInterface) {
                                val depModule = startupInfoStore.startupInterfaceMap[dep]
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
        }


        return StartupSortStore(
            uiZeroDeque, ioZeroDeque, startupChildrenMap, uiThreadTaskSize,
            needUIThreadWaitTaskSize, allTaskSize
        )
    }

    /**
     * 排序的方法
     */
    fun getPrivacyCheckStartup(
        startupList: List<IStartup<*>>,
        startupConfig: StartupConfig?
    ): StartupInfoStore {
        // startup的集合 key为uniqueKey value为startup对象
        val startupMap: HashMap<String, IStartup<*>> = HashMap(startupList.size)

        val privacyCheckPassStartupList = ArrayDeque<IStartup<*>>()

        val privacyCheckFailStartupList = ArrayDeque<IStartup<*>>()

        val startupInterfaceMap = mutableMapOf<Class<*>, Class<out IStartup<*>>>()

        val allStartupDependenciesList: MutableMap<String, MutableList<String>?> =
            mutableMapOf()

        // 维护入度不为0的startup的依赖表
        // 当一个startup(A)完成后，其他依赖该startup(A)的依赖表需要删除该startup(A)并进行重新计算
        val startupChildrenMap: MutableMap<String, MutableSet<String>> = mutableMapOf()

        // 构建startupInterfaceMap依赖关系表
        startupList.forEach { startup ->
            startup::class.java.interfaces.forEach {
                if (it.interfaces.contains(IStartup::class.java)) {
                    startupInterfaceMap[it] = startup.javaClass
                }
            }
        }

        val zeroDequeTmp = ArrayDeque<String>()
        startupList.forEach { it ->
            val uniqueKey = it::class.java.getUniqueKey()
            if (startupMap.containsKey(uniqueKey)) {
                throw StartupException("$uniqueKey ${StartupExceptionMsg.MULTI_ADD}")
            }
            // 保存所有startup
            startupMap[uniqueKey] = it
            // 保存所有startup 依赖关系
            allStartupDependenciesList[uniqueKey] = it.dependencies()?.map { it1 ->
                var ret = it1.getUniqueKey()
                if (it1.isInterface) {
                    ret = startupInterfaceMap[it1]?.getUniqueKey()!!
                }
                ret
            }?.toMutableList()
            // 计算统计入度， 入度为0则保存到0度列表
            // 入度不为0，存放到startupChildrenMap并保存需要依赖的startup
            // 隐私合规处理
            if (!it.needPrivacyAgree() || it.needPrivacyAgree() == startupConfig?.isPrivacyAgree) {
                if (it.dependencies().isNullOrEmpty()) {
                    privacyCheckPassStartupList.add(it)
                    zeroDequeTmp.add(uniqueKey)
                } else {
                    val dependencyTmp = mutableSetOf<String>()
                    it.dependencies()?.forEach { dep ->
                        if (dep.isInterface) {
                            val depModule = startupInterfaceMap[dep]
                            if (depModule != null) {
                                dependencyTmp.add(depModule.getUniqueKey())
                            }
                        } else {
                            dependencyTmp.add(dep.getUniqueKey())
                        }
                    }
                    startupChildrenMap[uniqueKey] = dependencyTmp
                }
            } else {
                privacyCheckFailStartupList.add(it)
            }
        }

        while (!zeroDequeTmp.isEmpty()) {
            zeroDequeTmp.removeFirstOrNull()?.let { uniqueKey ->
                val it = startupChildrenMap.iterator()
                while (it.hasNext()) {
                    val depList = it.next()
                    depList.value.remove(uniqueKey)
                    if (depList.value.size == 0) {
                        zeroDequeTmp.add(depList.key)
                        it.remove()
                        startupMap[depList.key]?.let { it1 -> privacyCheckPassStartupList.add(it1) }
                    }
                }
            }
        }
        // startupChildrenMap 里面剩余的是因为其依赖的startup未通过隐私检查
        startupChildrenMap.forEach {
            startupMap[it.key]?.let { it1 -> privacyCheckFailStartupList.add(it1) }
        }

        return StartupInfoStore(
            startupMap = startupMap, startupInterfaceMap = startupInterfaceMap,
            privacyCheckPassStartupList = privacyCheckPassStartupList,
            privacyCheckFailStartupList = privacyCheckFailStartupList,
            allStartupDependenciesList = allStartupDependenciesList
        )
    }
}