package io.github.fast_startup.exception

import java.lang.RuntimeException

/**
 * Author: xuweiyu
 * Date: 2021/12/1
 * Email: wizz.xu@outlook.com
 * Description:
 */
class StartupException : RuntimeException {

    constructor(message: String) : super(message)
}