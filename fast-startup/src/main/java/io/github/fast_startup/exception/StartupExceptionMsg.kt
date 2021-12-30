package io.github.fast_startup.exception

/**
 * Author: xuweiyu
 * Date: 2021/12/6
 * Email: wizz.xu@outlook.com
 * Description:
 */
interface StartupExceptionMsg {
    companion object {
        // 组件多次添加
        const val MULTI_ADD: String = "added more than one time"

        // 组件多次添加
        const val DEP_CIRCLE_MISSING: String = "dependencies have circle or missing"

        // 没有在主线程运行
        const val NOT_RUN_IN_MAIN_THREAD: String = "FastStartup has not run in Main Thread"

        // 没有初始化
        const val NOT_INIT: String = "FastStartup has not init"

        // 没有先运行start方法
        const val NOT_RUN_START: String = "Please run start() before run reStart()!"
    }
}