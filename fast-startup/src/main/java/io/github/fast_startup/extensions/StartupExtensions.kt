package io.github.fast_startup.extensions

import io.github.fast_startup.IStartup

/**
 * Author: xuweiyu
 * Date: 2021/12/1
 * Email: wizz.xu@outlook.com
 * Description:
 */
internal fun Class<out IStartup<*>>.getUniqueKey(): String {
    val canonicalName = this.canonicalName
    requireNotNull(canonicalName) { "Local and anonymous classes can not be Startup" }
    return canonicalName
}