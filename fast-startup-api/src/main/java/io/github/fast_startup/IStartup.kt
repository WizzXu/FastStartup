package io.github.fast_startup

import android.content.Context

/**
 * Author: xuweiyu
 * Date: 2021/12/1
 * Email: wizz.xu@outlook.com
 * Description:
 */
interface IStartup<T> {
    /**
     * 组件启动的方法
     */
    fun start(context: Context?, isDebug: Boolean? = false, any: Any? = null): T?

    /**
     * 返回该组件依赖的其他组件  List<Startup<*>>
     */
    fun dependencies(): List<Class<out IStartup<*>>>? {
        return null
    }

    /**
     * startup是否要在UI线程执行
     */
    fun runOnUIThread(): Boolean = false

    /**
     * UI线程是否需要等待此startup执行完毕后才能执行后续操作
     */
    fun needUIThreadWait(): Boolean = false

    /**
     * 组件开始启动之前的操作
     */
    fun onStartPrepare() {}

    /**
     *组件启动完成
     */
    fun onStartCompleted() {}
}