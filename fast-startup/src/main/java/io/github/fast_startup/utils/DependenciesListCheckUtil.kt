package io.github.fast_startup.utils

import io.github.fast_startup.IStartup
import io.github.fast_startup.exception.StartupException
import io.github.fast_startup.exception.StartupExceptionMsg
import io.github.fast_startup.extensions.getUniqueKey
import io.github.fast_startup.log.SLog
import java.util.*

/**
 * Author: xuweiyu
 * Date: 2021/12/13
 * Email: wizz.xu@outlook.com
 * Description:
 */
class DependenciesListCheckUtil {
    companion object {
        @JvmStatic
        fun dependenciesListCheck(
            startupList: List<IStartup<*>>,
            startupInterfaceMap: MutableMap<Class<*>, Class<out IStartup<*>>>,
        ) {
            val startupMap: HashMap<String, IStartup<*>> = HashMap(startupList.size)
            val startupChildrenMap: HashMap<String, MutableSet<String>> = HashMap()

            val zeroDequeTmp = ArrayDeque<String>()
            startupList.forEach {
                val uniqueKey = it::class.java.getUniqueKey()
                // 保存所有startup
                startupMap[uniqueKey] = it
                // 计算统计入度， 入度为0则保存到0度列表
                // 入度不为0，存放到startupChildrenMap并保存需要依赖的startup
                if (it.dependencies().isNullOrEmpty()) {
                    zeroDequeTmp.offer(uniqueKey)
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
            }


            val orderList = ArrayDeque<String>()
            orderList.addAll(zeroDequeTmp)
            while (!zeroDequeTmp.isEmpty()) {
                zeroDequeTmp.poll()?.let { uniqueKey ->
                    val it = startupChildrenMap.iterator()
                    while (it.hasNext()) {
                        val list = it.next()
                        if (list.value.contains(uniqueKey)) {
                            list.value.remove(uniqueKey)
                        }
                        if (list.value.size == 0) {
                            zeroDequeTmp.offer(list.key)
                            it.remove()
                            orderList.offer(list.key)
                        }
                    }
                }
            }

            if (startupChildrenMap.size == 0) {
                return
            }
            // 有环或者依赖缺失

            val missingDepList = mutableListOf<String>()
            val stringBuilder = StringBuilder().append("  \n").append("依赖关系：\n")
            startupList.forEach {
                it.dependencies()?.forEach { dep ->
                    val depStartup = if (dep.isInterface) {
                        startupInterfaceMap[dep]
                    } else {
                        startupMap[dep.getUniqueKey()]?.javaClass

                    }
                    if (depStartup == null) {
                        missingDepList.add(dep.getUniqueKey())
                    }
                }
                stringBuilder.append(it.javaClass.getUniqueKey())
                stringBuilder.append("  依赖了-->  ")
                stringBuilder.append(it.dependencies())
                stringBuilder.append("\n")
            }

            if (missingDepList.isNotEmpty()) {
                SLog.e(" \n 依赖有缺失: \n${missingDepList} \n ")
            } else {
                var ret = ""
                val iterator = startupChildrenMap.iterator()
                while (iterator.hasNext()) {
                    val dep = iterator.next()
                    ret += dep.key + "  依赖了  " + dep.value + "\n"
                }
                SLog.e(" \n 依赖有环:\n${ret} \n ")
            }
            SLog.e(stringBuilder)
            throw StartupException(StartupExceptionMsg.DEP_CIRCLE_MISSING)
        }
    }
}