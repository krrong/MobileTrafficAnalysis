package com.example.mobiletrafficanalysis

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.NetworkCapabilities
import android.util.Log


class TrafficMonitor(private var context: Context, private var appDataList : ArrayList<Data>, private var whiteList : ArrayList<String>){

    fun getTxBytesForAllApp() {
        // 설치되어 있는 어플리케이션의 패키지명 가져오기
        val packageManager = context.packageManager
        val list = packageManager.getInstalledApplications(0)

        // networkStatsManager 생성
        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

        // 어댑터에 이미 들어있는 어플리케이션의 데이터를 따로 저장 --> 어댑터에 중복으로 들어가지 않게 하기 위함
        val packageNameList : HashMap<String, Data> = HashMap()
        for (app in appDataList){
            packageNameList.put(app.getPackageName(), app)
        }

        // 설치되어 있는 앱 별 총 송신 트래픽 계산
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
            // uid 인 앱의 총 송신 트래픽 계산
            var txBytes: Long = 0
            do {
                val bucket = NetworkStats.Bucket()
                networkStats.getNextBucket(bucket)
                txBytes += bucket.txBytes
            } while (networkStats.hasNextBucket())

            // 원래 어댑터에 없던 것이라면 추가
            if(packageNameList.get(app.packageName) == null){
                // appDataList 에 (패키지명, 총 송신 트래픽) 추가
                appDataList.add(Data(app.packageName, txBytes))
            }
            // 원래 어댑터에 있던 것이라면 txBytes 만 수정
            else{
                val data = packageNameList.get(app.packageName)
                val index = appDataList.indexOf(data)
                appDataList[index].setTxBytes(txBytes)
            }

            networkStats.close()

            // com.google.* or com.samsung.* 으로 시작하는 패키지명은  whiteList 에 등록
            if(app.packageName.startsWith("com.google.") || app.packageName.startsWith("com.samsung.")){
//                whiteList.add(app.packageName)
                // 이미 화이트리스트에 등록되어 있지 않다면 추가
                if(!whiteList.contains(app.packageName)){
                    whiteList.add(app.packageName)
                }
            }
        }
    }
}