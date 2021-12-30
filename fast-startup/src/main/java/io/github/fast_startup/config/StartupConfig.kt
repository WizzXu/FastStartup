package io.github.fast_startup.config

import android.app.Application
import io.github.fast_startup.listener.AllStartupCompleteListener
import io.github.fast_startup.listener.StartupCompleteListener
import io.github.fast_startup.listener.UIStartupCompleteListener

/**
 * Author: xuweiyu
 * Date: 2021/12/3
 * Email: wizz.xu@outlook.com
 * Description:
 */
class StartupConfig {
    var application: Application? = null

    /**
     * 是否是debug模式
     */
    var isDebug: Boolean? = null

    /**
     * 日志级别，对标 Log.VERBOSE ~ Log.ASSERT
     */
    var logLevel: Int? = Int.MAX_VALUE

    /**
     * 是否开启耗时统计
     */
    var enableTimeStatistics: Boolean? = null

    /**
     * 贯穿所有startup的附加参数，可以配置id之类的一些列参数
     */
    var params: Any? = null

    /**
     * 是否已经同意隐私协议
     */
    var isPrivacyAgree: Boolean? = null

    /**
     * 是否打印依赖关系
     */
    var isPrintDependencies: Boolean? = null

    /**
     * 当有Startup任务执行完毕的回调
     */
    var startupCompleteListener: StartupCompleteListener? = null

    /**
     * UI任务执行完毕的回调
     */
    var uiStartupCompleteListener: UIStartupCompleteListener? = null

    /**
     * 所有任务执行完毕的回调
     */
    var allStartupCompleteListener: AllStartupCompleteListener? = null

    constructor(application: Application?, isDebug: Boolean?) {
        this.application = application
        this.isDebug = isDebug
    }

    constructor(
        application: Application?,
        isDebug: Boolean?,
        logLevel: Int?,
        enableTimeStatistics: Boolean?,
        params: Any?,
        startupCompleteListener: StartupCompleteListener?,
        uiStartupCompleteListener: UIStartupCompleteListener?,
        allStartupCompleteListener: AllStartupCompleteListener?
    ) {
        this.application = application
        this.isDebug = isDebug
        this.logLevel = logLevel
        this.enableTimeStatistics = enableTimeStatistics
        this.params = params
        this.startupCompleteListener = startupCompleteListener
        this.uiStartupCompleteListener = uiStartupCompleteListener
        this.allStartupCompleteListener = allStartupCompleteListener
    }

    constructor(
        application: Application?,
        isDebug: Boolean?,
        logLevel: Int?,
        enableTimeStatistics: Boolean?,
        params: Any?,
        isPrivacyAgree: Boolean?,
        isPrintDependencies: Boolean?,
        startupCompleteListener: StartupCompleteListener?,
        uiStartupCompleteListener: UIStartupCompleteListener?,
        allStartupCompleteListener: AllStartupCompleteListener?
    ) {
        this.application = application
        this.isDebug = isDebug
        this.logLevel = logLevel
        this.enableTimeStatistics = enableTimeStatistics
        this.params = params
        this.isPrivacyAgree = isPrivacyAgree
        this.isPrintDependencies = isPrintDependencies
        this.startupCompleteListener = startupCompleteListener
        this.uiStartupCompleteListener = uiStartupCompleteListener
        this.allStartupCompleteListener = allStartupCompleteListener
    }


    class Builder {
        private var mApplication: Application? = null
        private var mIsDebug: Boolean? = false
        private var mLogLevel: Int? = Int.MAX_VALUE
        private var mEnableTimeStatistics: Boolean? = false
        private var mParams: Any? = null
        private var mIsPrivacyAgree: Boolean? = null
        private var mIsPrintDependencies: Boolean? = null

        private var mStartupCompleteListener: StartupCompleteListener? = null
        private var mUiStartupCompleteListener: UIStartupCompleteListener? = null
        private var mAllStartupCompleteListener: AllStartupCompleteListener? = null

        fun setApplication(application: Application?) = apply {
            mApplication = application
        }

        fun setIsDebug(isDebug: Boolean) = apply {
            mIsDebug = isDebug
        }

        fun setLogLevel(logLevel: Int) = apply {
            mLogLevel = logLevel
        }

        fun setEnableTimeStatistics(enableTimeStatistics: Boolean) = apply {
            mEnableTimeStatistics = enableTimeStatistics
        }

        fun setParams(params: Any?) = apply {
            mParams = params
        }

        fun setIsPrivacyAgree(isPrivacyAgree: Boolean?) = apply {
            mIsPrivacyAgree = isPrivacyAgree
        }

        fun setIsPrintDependencies(isPrintDependencies: Boolean?) = apply {
            mIsPrintDependencies = isPrintDependencies
        }

        fun setStartupCompleteListener(startupCompleteListener: StartupCompleteListener?) = apply {
            mStartupCompleteListener = startupCompleteListener
        }

        fun setUIStartupCompleteListener(uiStartupCompleteListener: UIStartupCompleteListener?) =
            apply {
                mUiStartupCompleteListener = uiStartupCompleteListener
            }

        fun setAllStartupCompleteListener(allStartupCompleteListener: AllStartupCompleteListener?) =
            apply {
                mAllStartupCompleteListener = allStartupCompleteListener
            }

        fun build(): StartupConfig {
            return StartupConfig(
                mApplication,
                mIsDebug,
                mLogLevel,
                mEnableTimeStatistics,
                mParams,
                mIsPrivacyAgree,
                mIsPrintDependencies,
                mStartupCompleteListener,
                mUiStartupCompleteListener,
                mAllStartupCompleteListener
            )
        }
    }


}