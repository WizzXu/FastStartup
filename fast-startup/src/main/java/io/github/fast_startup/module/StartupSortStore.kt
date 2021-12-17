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

    // startup的集合 key为uniqueKey value为startup对象
    val startupMap: ConcurrentHashMap<String, IStartup<*>>,

    /**
     *  维护startup与其对外暴露的继承自Startup的接口 的 一一对应关系
     *  interface IA<T> : IStartup<T>
     *  class A : IA<String> {}
     *  class B : IStartup<String> {
     *      override fun create(context: Context?, isDebug: Boolean?, any: Any?): String? {
     *          return null
     *      }
     *
     *      override fun dependencies(): List<Class<out Startup<*>>>? {
     *          return listOf(IA::class.java)
     *      }
     *  }
     *  key为IA::class.java
     *  value为A::class.java
     */
    val startupInterfaceMap: MutableMap<Class<*>, Class<out IStartup<*>>>,

    // 保存所有startup start 返回的结果
    val startupResultMap: ConcurrentHashMap<String, Any?>,

    // 维护入度不为0的startup的依赖表
    // 当一个startup(A)完成后，其他依赖该startup(A)的依赖表需要删除该startup(A)并进行重新计算
    val startupChildrenMap: ConcurrentHashMap<String, MutableSet<String>>,

    // 工作在ui线程的startup的数量
    var uiThreadTaskSize: Int,

    // 需要ui线程等待的startup的数量
    var needUIThreadWaitTaskSize: Int
){
    fun free(){
        startupMap.clear()
        startupInterfaceMap.clear()
        startupResultMap.clear()
        startupChildrenMap.clear()
    }
}