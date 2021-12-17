package io.github.fast_startup.listener

import io.github.fast_startup.IStartup

/**
 * Author: xuweiyu
 * Date: 2021/12/13
 * Email: wizz.xu@outlook.com
 * Description:
 */
interface StartupCompleteListener {
    fun startupComplete(startup: IStartup<*>)
}