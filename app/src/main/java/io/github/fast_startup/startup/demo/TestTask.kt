package io.github.fast_startup.startup.demo

import android.util.Log
import androidx.annotation.UiThread

/**
 * Author: xuweiyu
 * Date: 2021/12/6
 * Email: wizz.xu@outlook.com
 * Description:
 */
object TestTask {
    private val testTaskList: ArrayDeque<TestBase> = ArrayDeque<TestBase>()

    fun addTask(task: TestBase) {
        testTaskList.add(task)
    }

    @UiThread
    fun startNext() {
        val task = testTaskList.removeFirstOrNull()
        if (task == null) {
            Log.e("场景测试:", "所有任务执行完毕")
        } else {
            task.test()
        }
    }
}