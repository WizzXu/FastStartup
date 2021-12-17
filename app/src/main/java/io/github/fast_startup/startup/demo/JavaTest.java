package io.github.fast_startup.startup.demo;

import android.app.Application;

import io.github.fast_startup.FastStartup;
import io.github.fast_startup.config.StartupConfig;

/**
 * Author: xuweiyu
 * Date: 2021/12/13
 * Email: wizz.xu@outlook.com
 * Description:
 */
public class JavaTest {
    public static void test() {
        FastStartup.INSTANCE.init(new StartupConfig.Builder().build());
        FastStartup.INSTANCE.init(new StartupConfig(new Application(), true));
        FastStartup.INSTANCE.init(new StartupConfig.Builder().setApplication(new Application())
                .setIsDebug(true).build());
    }
}
