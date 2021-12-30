package io.github.fast_startup

import android.os.Looper
import io.github.fast_startup.config.StartupConfig
import io.github.fast_startup.dispatcher.DefaultDispatcher
import io.github.fast_startup.exception.StartupException
import io.github.fast_startup.exception.StartupExceptionMsg
import io.github.fast_startup.extensions.getUniqueKey
import io.github.fast_startup.listener.AllStartupCompleteListener
import io.github.fast_startup.listener.StartupCompleteListener
import io.github.fast_startup.listener.UIStartupCompleteListener
import io.github.fast_startup.log.SLog
import io.github.fast_startup.module.StartupInfoStore
import io.github.fast_startup.sort.SortUtil
import io.github.fast_startup.utils.DependenciesListCheckUtil
import io.github.fast_startup.utils.StartupCostTimesUtil

/**
 * Author: xuweiyu
 * Date: 2021/12/5
 * Email: wizz.xu@outlook.com
 * Description:
 */
object FastStartup {

    private var startupConfig: StartupConfig? = null

    // ui线程startup执行完毕的回调
    private var uiStartupCompleteListeners =
        ArrayDeque<UIStartupCompleteListener>()

    // 所有的startup执行完毕的回调
    private var allStartupCompleteListeners =
        ArrayDeque<AllStartupCompleteListener>()

    // 每一个startup执行完毕的回调
    private var startupCompleteListeners =
        ArrayDeque<StartupCompleteListener>()

    private var startupCostTimesUtils: StartupCostTimesUtil? = null

    private var startupInfoStore: StartupInfoStore? = null

    private val aopStartups: MutableList<IStartup<*>> = mutableListOf()

    private var isInit = false

    /**
     * 是否已经同意隐私协议
     */
    private var isPrivacyAgree: Boolean? = null
        get() = startupConfig?.isPrivacyAgree

    /**
     * 设置是否同意隐私
     */
    fun setPrivacyAgree(isPrivacyAgree: Boolean): FastStartup {
        this.startupConfig?.isPrivacyAgree = isPrivacyAgree
        return this
    }

    /**
     * 注册UI线程Startup都执行完毕的监听
     */
    fun registerUIStartupCompleteListener(uiStartupCompleteListener: UIStartupCompleteListener): FastStartup {
        uiStartupCompleteListeners.add(uiStartupCompleteListener)
        return this
    }

    /**
     * 取消注册UI线程Startup都执行完毕的监听
     */
    fun unregisterUIStartupCompleteListener(uiStartupCompleteListener: UIStartupCompleteListener): FastStartup {
        val it: MutableIterator<UIStartupCompleteListener> =
            uiStartupCompleteListeners.iterator()
        while (it.hasNext()) {
            val listener: UIStartupCompleteListener = it.next()
            if (listener == uiStartupCompleteListener) {
                it.remove()
            }
        }
        return this
    }

    /**
     * 注册所有Startup都执行完毕的监听
     */
    fun registerAllStartupCompleteListener(allStartupCompleteListener: AllStartupCompleteListener): FastStartup {
        allStartupCompleteListeners.add(allStartupCompleteListener)
        return this
    }

    /**
     * 取消注册所有Startup都执行完毕的监听
     */
    fun unregisterAllStartupCompleteListener(allStartupCompleteListener: AllStartupCompleteListener): FastStartup {
        val it: MutableIterator<AllStartupCompleteListener> =
            allStartupCompleteListeners.iterator()
        while (it.hasNext()) {
            val listener: AllStartupCompleteListener = it.next()
            if (listener == allStartupCompleteListener) {
                it.remove()
            }
        }

        return this
    }

    /**
     * 注册Startup执行完毕的监听
     */
    fun registerStartupCompleteListener(startupCompleteListener: StartupCompleteListener): FastStartup {
        startupCompleteListeners.add(startupCompleteListener)
        return this
    }

    /**
     * 取消注册每一个Startup执行完毕的监听
     */
    fun unregisterStartupCompleteListener(startupCompleteListener: StartupCompleteListener): FastStartup {
        val it: MutableIterator<StartupCompleteListener> =
            startupCompleteListeners.iterator()
        while (it.hasNext()) {
            val listener: StartupCompleteListener = it.next()
            if (listener == startupCompleteListener) {
                it.remove()
            }
        }
        return this
    }

    fun init(
        startupConfig: StartupConfig? = null,
    ): FastStartup {
        if (isInit) {
            SLog.i("FastStartup has init")
            return this
        }
        isInit = true
        FastStartup.startupConfig = startupConfig
        startupConfig?.let { config ->
            SLog.init(config.logLevel ?: Int.MAX_VALUE, "FastStartup")
            config.enableTimeStatistics?.let {
                if (it) {
                    this.startupCostTimesUtils = StartupCostTimesUtil(it)
                }
            }
            config.uiStartupCompleteListener?.let {
                registerUIStartupCompleteListener(it)
            }
            config.allStartupCompleteListener?.let {
                registerAllStartupCompleteListener(it)
            }
            config.startupCompleteListener?.let {
                registerStartupCompleteListener(it)
            }
            this.isPrivacyAgree = config.isPrivacyAgree
        }
        SLog.i("FastStartup init succeed")
        return this
    }

    /**
     * 开始启动startup
     */
    fun start(startupList: List<IStartup<*>>? = null): FastStartup {
        startupCostTimesUtils?.initStartTime()
        // 初始化检测
        if (!isInit) {
            throw StartupException(StartupExceptionMsg.NOT_INIT)
        }
        // 主线程检测
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw StartupException(StartupExceptionMsg.NOT_RUN_IN_MAIN_THREAD)
        }
        initAopStartup()
        SLog.i("aopStartups:$aopStartups")
        startupList?.let {
            aopStartups.addAll(startupList)
        }

        startupInfoStore = SortUtil.getPrivacyCheckStartup(aopStartups, startupConfig)
        startupInfoStore?.let { startupInfoStore ->
            // 环检测
            DependenciesListCheckUtil.dependenciesListCheck(
                startupList = aopStartups,
                startupConfig = startupConfig, startupInfoStore = startupInfoStore
            )
            // 运行隐私检测通过的startup
            SortUtil.sort(
                startupInfoStore.privacyCheckPassStartupList,
                startupConfig, startupInfoStore
            ).let { startupSortStore ->
                val startupDispatcher = DefaultDispatcher(startupConfig, startupInfoStore)
                startupDispatcher.allStartupCompleteListener = object : AllStartupCompleteListener {
                    override fun startupComplete() {
                        while (allStartupCompleteListeners.isNotEmpty()) {
                            allStartupCompleteListeners.removeFirstOrNull()?.startupComplete()
                        }
                        if (uiStartupCompleteListeners.isEmpty()) {
                            startupCompleteListeners.clear()
                        }
                    }
                }
                startupDispatcher.uiStartupCompleteListener = object : UIStartupCompleteListener {
                    override fun startupComplete() {
                        while (uiStartupCompleteListeners.isNotEmpty()) {
                            uiStartupCompleteListeners.removeFirstOrNull()?.startupComplete()
                        }
                        if (allStartupCompleteListeners.isEmpty()) {
                            startupCompleteListeners.clear()
                        }
                    }
                }
                startupDispatcher.startupCompleteListener = object : StartupCompleteListener {
                    override fun startupComplete(startup: IStartup<*>) {
                        startupCompleteListeners.forEach {
                            it.startupComplete(startup)
                        }
                    }
                }
                startupDispatcher.start(startupSortStore, startupCostTimesUtils)
            }
        }
        return this
    }

    /**
     * 重新执行需要隐私的Startup
     */
    fun reStart() {
        if (isPrivacyAgree != true || startupInfoStore?.privacyCheckFailStartupList?.size == 0) {
            SLog.e("隐私未同意或者没有需要隐私权限的Startup")
            return
        }
        // 初始化检测
        if (!isInit) {
            throw StartupException(StartupExceptionMsg.NOT_INIT)
        }
        // 主线程检测
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw StartupException(StartupExceptionMsg.NOT_RUN_IN_MAIN_THREAD)
        }
        // 检测有没有先运行start()方法
        if (startupInfoStore == null) {
            throw StartupException(StartupExceptionMsg.NOT_RUN_START)
        }
        startupCostTimesUtils?.initStartTime()
        startupInfoStore?.let { startupInfoStore ->
            // 运行隐私检测通过的startup
            SortUtil.sort(
                startupInfoStore.privacyCheckFailStartupList,
                startupConfig, startupInfoStore
            ).let { startupSortStore ->
                val startupDispatcher = DefaultDispatcher(startupConfig, startupInfoStore)
                startupDispatcher.allStartupCompleteListener = object : AllStartupCompleteListener {
                    override fun startupComplete() {
                        while (allStartupCompleteListeners.isNotEmpty()) {
                            allStartupCompleteListeners.removeFirstOrNull()?.startupComplete()
                        }
                        if (uiStartupCompleteListeners.isEmpty()) {
                            startupCompleteListeners.clear()
                        }
                    }
                }
                startupDispatcher.uiStartupCompleteListener = object : UIStartupCompleteListener {
                    override fun startupComplete() {
                        while (uiStartupCompleteListeners.isNotEmpty()) {
                            uiStartupCompleteListeners.removeFirstOrNull()?.startupComplete()
                        }
                        if (allStartupCompleteListeners.isEmpty()) {
                            startupCompleteListeners.clear()
                        }
                    }
                }
                startupDispatcher.startupCompleteListener = object : StartupCompleteListener {
                    override fun startupComplete(startup: IStartup<*>) {
                        startupCompleteListeners.forEach {
                            it.startupComplete(startup)
                        }
                    }
                }
                startupDispatcher.start(startupSortStore, startupCostTimesUtils)
            }
        }
    }

    /**
     * 获取startup实例
     */
    fun <T : IStartup<*>> getStartup(clazz: Class<T>): T? {
        if (clazz.isInterface) {
            val inter = startupInfoStore?.startupInterfaceMap?.get(clazz)
            if (inter != null) {
                startupInfoStore?.startupMap?.get(inter.getUniqueKey())?.let {
                    return it as T
                }
            }
        }
        startupInfoStore?.startupMap?.get(clazz.getUniqueKey())?.let {
            return it as T
        }
        return null
    }

    /**
     * 获取组件启动后的返回值缓存
     */
    fun getStartupResult(clazz: Class<out IStartup<*>>): Any? {
        if (clazz.isInterface) {
            val inter = startupInfoStore?.startupInterfaceMap?.get(clazz)
            if (inter != null) {
                return startupInfoStore?.startupResultMap?.get(inter.getUniqueKey())
            }
        }
        return startupInfoStore?.startupResultMap?.get(clazz.getUniqueKey())
    }

    /**
     * 请勿删除，供AOP插桩使用
     */
    private fun initAopStartup() {

    }

    /**
     * 释放资源
     */
    fun free() {
        this.isInit = false
        this.startupCostTimesUtils = null
        this.startupConfig = null
        this.startupInfoStore?.free()
        this.startupInfoStore = null
        this.aopStartups.clear()
        this.allStartupCompleteListeners.clear()
        this.uiStartupCompleteListeners.clear()
        this.startupCompleteListeners.clear()
    }
}