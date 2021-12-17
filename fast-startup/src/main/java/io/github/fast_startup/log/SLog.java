package io.github.fast_startup.log;

import android.util.Log;

import java.util.AbstractCollection;
import java.util.AbstractMap;

/**
 * Author: xuweiyu
 * Date: 2021/12/8
 * Email: wizz.xu@outlook.com
 * Description:
 */
public class SLog {

    private static String TAG = "SLog";

    private static int defLogLevel = Integer.MAX_VALUE;

    public static void init(int logLevel) {
        defLogLevel = logLevel;
    }

    public static void init(int logLevel, String defaultTag) {
        defLogLevel = logLevel;
        TAG = defaultTag;
    }

    private static String getDefTag() {
        return TAG;
    }

    public static void v(Object msg) {
        print(Log.VERBOSE, getDefTag(), msg);
    }

    public static void v(String tag, Object msg) {
        print(Log.VERBOSE, tag, msg);
    }

    public static void d(Object msg) {
        print(Log.DEBUG, getDefTag(), msg);
    }

    public static void d(String tag, Object msg) {
        print(Log.DEBUG, tag, msg);
    }

    public static void i(Object msg) {
        print(Log.INFO, getDefTag(), msg);
    }

    public static void i(String tag, Object msg) {
        print(Log.INFO, tag, msg);
    }

    public static void w(Object msg) {
        print(Log.WARN, getDefTag(), msg);
    }

    public static void w(String tag, Object msg) {
        print(Log.WARN, tag, msg);
    }

    public static void e(Object msg) {
        print(Log.ERROR, getDefTag(), msg);
    }

    public static void e(String tag, Object msg) {
        print(Log.ERROR, tag, msg);
    }

    private static void print(int priority, String tag, Object msg) {
        if (priority < defLogLevel || msg == null) {
            return;
        }
        if (msg instanceof Number) {
            Log.println(priority, tag, String.valueOf(msg));
        } else if (msg instanceof AbstractCollection) {
            Log.println(priority, tag, msg.toString());
        } else if (msg instanceof AbstractMap) {
            Log.println(priority, tag, msg.toString());
        } else {
            Log.println(priority, tag, msg.toString());
        }

    }
}


