package io.github.fast_startup.module

import io.github.fast_startup.IStartup
import java.util.concurrent.ConcurrentHashMap

/**
 * Author: xuweiyu
 * Date: 2021/12/31
 * Email: wizz.xu@outlook.com
 * Description:
 */
internal data class StartupInfoStore(
    /**
     * 保存所有startup start 返回的结果
     */
    val startupResultMap: ConcurrentHashMap<String, Any?> = ConcurrentHashMap(),

    /**
     * startup的集合 key为uniqueKey value为startup对象
     */
    val startupMap: MutableMap<String, IStartup<*>>,

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

    /**
     * 保存隐私检查通过的Startup
     */
    val fistFilterStartupSortStore: StartupSortStore,

    /**
     * 保存隐私检查未通过的Startup
     */
    val privacyCheckFailStartupList: ArrayDeque<IStartup<*>>,

    /**
     * 所有startup的依赖关系
     */
    val allStartupDependenciesList: MutableMap<String, MutableList<String>>
) {
    fun free() {
        startupMap.clear()
        startupInterfaceMap.clear()
        startupResultMap.clear()
        fistFilterStartupSortStore.free()
        privacyCheckFailStartupList.clear()
        allStartupDependenciesList.clear()
    }
}