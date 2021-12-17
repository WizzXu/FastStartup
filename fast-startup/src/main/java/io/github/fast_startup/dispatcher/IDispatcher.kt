package io.github.fast_startup.dispatcher

import io.github.fast_startup.IStartup
import io.github.fast_startup.module.StartupSortStore

/**
 * Author: xuweiyu
 * Date: 2021/12/7
 * Email: wizz.xu@outlook.com
 * Description:
 */
internal interface IDispatcher {
    fun dispatch(completeStartup: IStartup<*>, startupSortStore: StartupSortStore)
}