package com.example.mobiletrafficanalysis

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.NetworkCapabilities
import android.util.Log
import java.text.SimpleDateFormat
import java.time.LocalDateTime


class TrafficMonitor(private var context: Context,
                     private var appDataList: ArrayList<Data>,
                     private var whiteList: ArrayList<String>,
                     private var list: MutableList<ApplicationInfo>,
                     private  var appHistory : HashMap<Int, Long>){

    private val period : Int = 10000

    fun getTxBytesForAllApp() {
//        // 함수 시작 시간 Log
//        val dataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//        val time = System.currentTimeMillis()
//        Log.e("getTxBytesForAllApp", dataFormat.format(time))

        // networkStatsManager 생성
        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

        // 어댑터에 이미 들어있는 어플리케이션의 데이터를 따로 저장 --> 어댑터에 중복으로 들어가지 않게 하기 위함
        val packageNameList : HashMap<Int, Data> = HashMap()
        for (app in list){
            // 앱 이름 얻어오기
            val label = context.packageManager.getApplicationLabel(app)

            // 히스토리에 없다면 송신 데이터 양은 0으로 세팅하여 추가
            if(appHistory.get(app.uid) == null){
                packageNameList.put(app.uid, Data(label.toString() + "(" + app.packageName + ")", "", app.uid, 0L))
            }
            // 히스토리에 있다면 송신 데이터 양은 히스토리에서 얻어와서 추가
            else{
                packageNameList.put(app.uid, Data(label.toString() + "(" + app.packageName + ")", "", app.uid, appHistory.get(app.uid)!!))
            }
        }

        // NetworkStats 리턴
        val networkStats = networkStatsManager.querySummary(
            NetworkCapabilities.TRANSPORT_WIFI,
            "",
            System.currentTimeMillis() - period,
            System.currentTimeMillis(),
        )

        do {
            //앱의 총 송신 트래픽 계산
            val bucket = NetworkStats.Bucket()
            networkStats.getNextBucket(bucket)

            val uid = bucket.uid    // 앱 uid 반환

            val data = packageNameList.get(uid) // uid를 사용하여 (앱이름 + 패키지명, uid, 송신 트래픽 양) 반환

            // 기본 시스템 앱은 pass
            if(uid == 1000 || uid == 0) continue

            // 데이터가 null 이 아니고 화이트리스트에 들어가 있지 않다면 송신 트래픽 측정
            if(data != null && !whiteList.contains(data.getPackageName())) {
                Log.e("UID", uid.toString())
                Log.e("txBytes", bucket.txBytes.toString())

                var txBytes = bucket.txBytes   // 앱 별 송신 트래픽
                val bytes = data?.getTxBytes()
                val diff = txBytes - bytes!!    // 현재 계산한 txBytes - 이전에 계산한 txBytes

                // 차이가 양수면 송신 기록이 있다는 의미-> 사용자에게 보여줘야 함
                // 음수면 송신 데이터 양이 적어졌다는 뜻 --> 측정 시간동안 보내지 않았다고 할 수 있음
                if (diff > 0L) {
//                if (diff != 0L) {
                    appHistory.put(uid, diff)
//                    if(appHistory.get(uid) == null){
//                        appHistory.put(uid, diff)
//                    }
//                    else{
//                        appHistory.replace(uid, diff)
//                    }
                    val dataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val time = System.currentTimeMillis()
                    data.setTxBytes(diff)
                    data.setDetectTime(dataFormat.format(time))

                    // 가장 앞에 새로운 데이터 추가
                    appDataList.add(0, data)
                }
            }
        } while (networkStats.hasNextBucket())



        networkStats.close()
    }
}