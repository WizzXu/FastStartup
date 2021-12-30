package io.github.fast_startup.utils

import io.github.fast_startup.config.StartupConfig
import io.github.fast_startup.log.SLog
import io.github.fast_startup.module.StartupInfoStore

/**
 * Author: xuweiyu
 * Date: 2021/12/13
 * Email: wizz.xu@outlook.com
 * Description: 打印Startup依赖关系
 */
internal class DependenciesListPrintUtil {
    companion object {
        @JvmStatic
        fun printDependenciesList(
            startupList: ArrayDeque<String>,
            startupConfig: StartupConfig?,
            startupInfoStore: StartupInfoStore,
        ) {
            if (startupConfig?.isPrintDependencies == true) {
                val sb = StringBuffer(" \n")
                sb.append("|============================================================================\n")
                sb.append("Startup 依赖关系 \n")
                startupList.forEach {
                    sb.append("$it\n")
                    startupInfoStore.allStartupDependenciesList[it]?.forEach { it1 ->
                        sb.append("｜    \\--$it1\n")
                    }
                }
                sb.append("|============================================================================\n")
                SLog.e(sb.toString())
            }
        }
    }
}