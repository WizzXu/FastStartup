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
    var isDebug: Boolean? = null
    var logLevel: Int? = Int.MAX_VALUE
    var enableTimeStatistics: Boolean? = null
    var params: Any? = null

    var startupCompleteListener: StartupCompleteListener? = null
    var uiStartupCompleteListener: UIStartupCompleteListener? = null
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


    class Builder(

    ) {

        private var mApplication: Application? = null
        private var mIsDebug: Boolean? = false
        private var mLogLevel: Int? = Int.MAX_VALUE
        private var mEnableTimeStatistics: Boolean? = false
        private var mParams: Any? = null

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
                mStartupCompleteListener,
                mUiStartupCompleteListener,
                mAllStartupCompleteListener
            )
        }
    }


}