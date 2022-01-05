package io.github.fast_startup.sort

import io.github.fast_startup.IStartup
import io.github.fast_startup.config.StartupConfig
import io.github.fast_startup.exception.StartupException
import io.github.fast_startup.exception.StartupExceptionMsg
import io.github.fast_startup.extensions.getUniqueKey
import io.github.fast_startup.module.StartupInfoStore
import io.github.fast_startup.module.StartupSortStore
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

        // 所有任务的数量
        val allTaskSize = startupList.size

        // 工作在ui线程的startup的队列
        val uiZeroDeque: ArrayDeque<IStartup<*>> = ArrayDeque(allTaskSize)

        // 工作在io线程的startup的队列
        val ioZeroDeque: ArrayDeque<IStartup<*>> = ArrayDeque(allTaskSize)

        // 维护入度不为0的startup的依赖表
        // 当一个startup(A)完成后，其他依赖该startup(A)的依赖表需要删除该startup(A)并进行重新计算
        val startupChildrenMap: MutableMap<String, MutableList<String>> = HashMap(allTaskSize)

        // 工作在ui线程的startup的数量
        var uiThreadTaskSize = 0

        // 需要ui线程等待的startup的数量
        var needUIThreadWaitTaskSize = 0

        // 初次计算运行的主线程和IO线程的入度为0的startup，分别存储在uiZeroDeque和ioZeroDeque
        // 入度不为0的startup和他依赖的startup的依赖关系保存在startupChildrenMap中
        // 计算所有需要运行在主线程的startup的数量 和 需要主线程等待的startup的数量
        for (i in 0 until allTaskSize) {
            startupList.removeFirstOrNull()?.let {
                val uniqueKey = it::class.java.getUniqueKey()
                // 符合隐私检测
                if (!it.needPrivacyAgree() || it.needPrivacyAgree() == startupConfig?.isPrivacyAgree) {
                    // 计算统计入度， 入度为0则保存到0度列表
                    // 入度不为0，存放到startupChildrenMap并保存需要依赖的startup
                    val depList = startupInfoStore.allStartupDependenciesList[uniqueKey]!!
                    if (depList.isEmpty()) {
                        if (it.runOnUIThread()) {
                            uiZeroDeque.add(it)
                        } else {
                            ioZeroDeque.add(it)
                        }
                    } else {
                        startupChildrenMap[uniqueKey] = depList
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

        val privacyCheckFailStartupList = ArrayDeque<IStartup<*>>()

        val startupInterfaceMap = mutableMapOf<Class<*>, Class<out IStartup<*>>>()

        // 工作在ui线程的startup的数量
        var uiThreadTaskSize = 0

        // 需要ui线程等待的startup的数量
        var needUIThreadWaitTaskSize = 0

        // 工作在ui线程的startup的队列
        val uiZeroDeque: ArrayDeque<IStartup<*>> = ArrayDeque(startupList.size)

        // 工作在io线程的startup的队列
        val ioZeroDeque: ArrayDeque<IStartup<*>> = ArrayDeque(startupList.size)

        // 维护入度不为0的startup的依赖表
        // 当一个startup(A)完成后，其他依赖该startup(A)的依赖表需要删除该startup(A)并进行重新计算
        val startupChildrenMap: MutableMap<String, MutableList<String>> = mutableMapOf()

        val startupChildrenMapTmp: MutableMap<String, MutableList<String>> = mutableMapOf()

        val allStartupDependenciesList: MutableMap<String, MutableList<String>> = mutableMapOf()

        // 构建startupInterfaceMap依赖关系表
        startupList.forEach { startup ->
            startup::class.java.interfaces.forEach {
                if (it.interfaces.contains(IStartup::class.java)) {
                    startupInterfaceMap[it] = startup.javaClass
                }
            }
        }

        val zeroDequeTmp = ArrayDeque<String>()
        startupList.forEach {
            val uniqueKey = it::class.java.getUniqueKey()
            if (startupMap.containsKey(uniqueKey)) {
                throw StartupException("[$uniqueKey] ${StartupExceptionMsg.MULTI_ADD}")
            }
            // 保存所有startup
            startupMap[uniqueKey] = it
            // 保存所有 startup 依赖关系, 去除接口依赖，维护真正的依赖类
            val realDepList: MutableList<String> = mutableListOf()
            it.dependencies()?.forEach { dep ->
                if (dep.isInterface) {
                    startupInterfaceMap[dep]?.let { it1 -> realDepList.add(it1.getUniqueKey()) }
                } else {
                    realDepList.add(dep.getUniqueKey())
                }
            }
            allStartupDependenciesList[uniqueKey] = realDepList
            // 计算统计入度， 入度为0则保存到0度列表
            // 入度不为0，存放到startupChildrenMap并保存需要依赖的startup
            // startupChildrenMap中的startup可能依赖项没有通过隐私检查，需要后面过滤掉这部分内容
            // 隐私合规处理
            if (!it.needPrivacyAgree() || it.needPrivacyAgree() == startupConfig?.isPrivacyAgree) {
                if (it.dependencies().isNullOrEmpty()) {
                    if (it.runOnUIThread()) {
                        uiZeroDeque.add(it)
                    } else {
                        ioZeroDeque.add(it)
                    }
                    zeroDequeTmp.add(uniqueKey)
                } else {
                    // 方法体中需要计算，所以需要一个新的列表，不能修改原来的列表
                    startupChildrenMapTmp[uniqueKey] =
                        allStartupDependenciesList[uniqueKey]!!.toMutableList()
                }

                if (it.runOnUIThread()) {
                    uiThreadTaskSize++
                }

                if (it.needUIThreadWait()) {
                    needUIThreadWaitTaskSize++
                }
            } else {
                privacyCheckFailStartupList.add(it)
            }
        }

        while (!zeroDequeTmp.isEmpty()) {
            zeroDequeTmp.removeFirstOrNull()?.let { startup ->
                val it = startupChildrenMapTmp.iterator()
                while (it.hasNext()) {
                    val depList = it.next()
                    if (depList.value.also { it.remove(startup) }.isEmpty()) {
                        zeroDequeTmp.add(depList.key)
                        it.remove()
                        // 该startup的依赖项为空，证明其依赖项都通过隐私过滤，所以能够参与到启动流程
                        // startupChildrenMap中的list需要使用统一维护的列表对象
                        startupChildrenMap[depList.key] = allStartupDependenciesList[depList.key]!!
                    }
                }
            }
        }
        // startupChildrenMap 里面剩余的是因为其依赖的startup未通过隐私检查
        startupChildrenMapTmp.forEach {
            startupMap[it.key]?.let { it1 ->
                privacyCheckFailStartupList.add(it1)
                if (it1.runOnUIThread()) {
                    uiThreadTaskSize--
                }

                if (it1.needUIThreadWait()) {
                    needUIThreadWaitTaskSize--
                }
            }
        }
        val startupSortStore = StartupSortStore(
            uiZeroDeque = uiZeroDeque,
            ioZeroDeque = ioZeroDeque,
            startupChildrenMap = startupChildrenMap,
            uiThreadTaskSize = uiThreadTaskSize,
            needUIThreadWaitTaskSize = needUIThreadWaitTaskSize,
            allTaskSize = uiZeroDeque.size + ioZeroDeque.size + startupChildrenMap.size
        )

        return StartupInfoStore(
            startupMap = startupMap, startupInterfaceMap = startupInterfaceMap,
            fistFilterStartupSortStore = startupSortStore,
            privacyCheckFailStartupList = privacyCheckFailStartupList,
            allStartupDependenciesList = allStartupDependenciesList
        )
    }
}