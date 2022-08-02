package com.example.mobiletrafficanalysis

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.PowerManager
import android.util.Log
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * 위험도를 구분 클래스
 */
class DangerDivider(var context : Context,
                    var whiteList: ArrayList<Int>){
    /**
     * 위험도 구분 함수 (0, 1, 2, 3)
     */
    fun divideDangerLevel(uid : Int) : Int {
        // 화이트리스트에 포함되어 있으면 0
        if(inWhiteList(uid)){
            return 0
        }

        // 터치 이벤트가 발생했다면 1
        if(isTouchEvent()) {
            return 1
        }
        else{
            // 터치 이벤트가 발생하지 않고 스크린이 켜져 있다면 2
            if(isScreenOn()){
                return 2
            }
            // 터치 이벤트가 발생하지 않고 스크린이 꺼져 있다면 3
            else{
                return 3
            }
        }
        return -1
    }

    /**
     * 패키지명이 화이트리스트에 있는지 반환
     */
    fun inWhiteList(uid : Int) : Boolean {
        // 화이트리스트에 패키지명이 포함되어 있으면 true 반환
        if(whiteList.contains(uid)){
            return true
        }
        return false
    }

    /**
     * 화면이 켜져있는지 반환
     */
    fun isScreenOn() : Boolean {
        var powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        Log.d("DangerDivider", powerManager.isInteractive.toString())
        return powerManager.isInteractive
    }

    /**
     * 터치 이벤트가 발생했는지 반환
     */
    fun isTouchEvent(): Boolean {
        // 앱의 마지막 터치 이벤트 발생 시각이 현재 시각 10초 내에 있으면 true
        return true
    }

    /**
     * 현재 포그라운드에 있는 앱 이름 반환
     */
    fun getForegroundApp() : String{
        Log.d("DangerDivider", "getForegroundApp")
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val curTime = System.currentTimeMillis()
        val appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, curTime - 1000*1000, curTime)
        var app = ""

        if(appList != null && appList.size > 0){
            var map = HashMap<Long, UsageStats>()
            for(usageStats in appList){
                map.put(usageStats.lastTimeUsed, usageStats)
            }
            val sortedMap = map.toSortedMap()

            if(!sortedMap.isEmpty()){
                app = sortedMap.get(sortedMap.lastKey())?.packageName!!
            }
        }

        return app
    }

}