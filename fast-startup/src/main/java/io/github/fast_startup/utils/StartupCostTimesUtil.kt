package io.github.fast_startup.utils

import io.github.fast_startup.IStartup
import io.github.fast_startup.extensions.getUniqueKey
import io.github.fast_startup.log.SLog
import java.util.concurrent.ConcurrentSkipListMap

/**
 * Author: xuweiyu
 * Date: 2021/12/9
 * Email: wizz.xu@outlook.com
 * Description:
 */
internal class StartupCostTimesUtil(private var isOpenStatistics: Boolean = false) {

    private val costTimesMap = ConcurrentSkipListMap<String, CostTimesModel>()

    private val ACCURACY = 1000 * 1000L

    private var startTime = 0L
    private var uiThreadStartupEndTime = 0L
    private var allStartupEndTime = 0L

    init {
        startTime = System.nanoTime()
    }

    fun initStartTime(){
        startTime = System.nanoTime()
    }

    fun recordUIThreadStartupsEnd() {
        if (checkOpenStatistics()) {
            uiThreadStartupEndTime = System.nanoTime()
        }
    }

    fun recordAllStartupsEnd() {
        if (checkOpenStatistics()) {
            allStartupEndTime = System.nanoTime()
        }
    }

    fun recordStart(startup: IStartup<*>) {
        if (checkOpenStatistics()) {
            costTimesMap[startup::class.java.getUniqueKey()] = CostTimesModel(
                name = startup::class.java.name,
                callOnMainThread = startup.runOnUIThread(),
                needUIThreadWait = startup.needUIThreadWait(),
                needPrivacyAgree = startup.needPrivacyAgree(),
                startTime = System.nanoTime(),
                endTime = 0
            )
        }
    }

    fun recordEnd(startup: IStartup<*>) {
        if (checkOpenStatistics()) {
            costTimesMap[startup::class.java.getUniqueKey()]?.let {
                it.endTime = System.nanoTime()
            }
        }
    }

    fun printAll() {
        if (!checkOpenStatistics()) {
            return
        }
        SLog.e(buildString {
            append("startup cost times detail:")
            append("\n")
            append("|============================================================================")
            costTimesMap.values.forEach {
                append("\n")
                append("|      Startup Name       |   ${it.name}")
                append("\n")
                append("| ----------------------- | -------------------------------------------------")
                append("\n")
                append("|   Call On Main Thread   |   ${it.callOnMainThread}")
                append("\n")
                append("| ----------------------- | -------------------------------------------------")
                append("\n")
                append("|   Need UI Thread Wait   |   ${it.needUIThreadWait}")
                append("\n")
                append("| ----------------------- | -------------------------------------------------")
                append("\n")
                append("|   Need Privacy Agree    |   ${it.needPrivacyAgree}")
                append("\n")
                append("| ----------------------- | -------------------------------------------------")
                append("\n")
                append("|       Cost Times        |   ${(it.endTime - it.startTime) / ACCURACY} ms")
                append("\n")
                append("|============================================================================")
            }
            append("\n")
            append("| Total Main Thread Times |   ${(uiThreadStartupEndTime - startTime) / ACCURACY} ms")
            append("\n")
            append("| All Startup  Cost Times |   ${(allStartupEndTime - startTime) / ACCURACY} ms")
            append("\n")
            append("|============================================================================")
        })
        costTimesMap.clear()
    }

    private fun checkOpenStatistics() = isOpenStatistics
}

private data class CostTimesModel(
    val name: String,
    val callOnMainThread: Boolean,
    val needUIThreadWait: Boolean,
    val needPrivacyAgree: Boolean,
    val startTime: Long,
    var endTime: Long = 0L
)