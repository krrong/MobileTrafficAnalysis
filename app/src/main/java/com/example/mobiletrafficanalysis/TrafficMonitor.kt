package com.example.mobiletrafficanalysis

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.NetworkCapabilities
import android.util.Log


class TrafficMonitor(private var context: Context, private var appDataList : ArrayList<Data>) : Thread() {
    override fun run() {
        // 어플리케이션의 패키지명 가져오기
        val packageManager = context.packageManager
        val list = packageManager.getInstalledApplications(0)
        Log.d("TrafficMonitor", list.size.toString())

        // networkStatsManager 생성
        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

        for (app in list) {
            // 각 앱의 uid
            val uid = app.uid

            // uid 를 통해 NetworkStats 리턴
            val networkStats = networkStatsManager.queryDetailsForUid(
                NetworkCapabilities.TRANSPORT_WIFI,
                "",
                0,
                System.currentTimeMillis(),
                uid
            )

            // uid 를 가진 앱의 총 송신 트래픽 계산
            var txBytes: Long = 0
            do {
                val bucket = NetworkStats.Bucket()
                networkStats.getNextBucket(bucket)
                txBytes += bucket.txBytes
            } while (networkStats.hasNextBucket())

            // appDataList 에 (패키지명, 총 송신 트래픽) 추가
            appDataList.add(Data(app.packageName, txBytes))
        }
    }
}