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

    /**
     * 패키지 명이 화이트 리스트에 있는지 반환
     */
    fun isInWhiteList(packageName : String) : Boolean {
        return whiteList.contains(packageName)
    }

    /**
     * 현재 시간을 yyyy-MM-dd HH:mm:ss 포맷으로 반환
     */
    fun getTime() : String{
        val dataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val time = System.currentTimeMillis()
        return dataFormat.format(time)
    }

    /**
     * 송신 트래픽이 발생한 어플리케이션의 송신 트래픽 계산
     */
    fun getTxBytesForAllApp() {
        // networkStatsManager 생성
        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

        // packageNameList 는 uid를 이용하여 Data 객체를 가져오는 용도로 사용
        // measureTxBytes 는 같은 uid에서 발생한 txBytes를 계산하고 history와 비교하는 용도로 사용
        val packageNameList : HashMap<Int, Data> = HashMap()
        var measureTxBytes : HashMap<Int, Data> = HashMap()

        for (app in list){
            // 앱 이름 얻어오기
            val label = context.packageManager.getApplicationLabel(app)

//            // 히스토리에 없다면 송신 데이터 양은 0으로 세팅하여 추가
//            if(appHistory.get(app.uid) == null){
//                packageNameList.put(app.uid, Data(label.toString() + "(" + app.packageName + ")", "", app.uid, 0L))
//            }
//            // 히스토리에 있다면 송신 데이터 양은 히스토리에서 얻어와서 추가
//            else{
//                packageNameList.put(app.uid, Data(label.toString() + "(" + app.packageName + ")", "", app.uid, appHistory.get(app.uid)!!))
//            }
            // packageNameList 는 uid를 이용하여 Data 객체를 가져오는 용도로 사용
            packageNameList.put(app.uid, Data(label.toString() + "(" + app.packageName + ")", "", app.uid, 0L))
        }

        // NetworkStats 리턴
        val networkStats = networkStatsManager.querySummary(
            NetworkCapabilities.TRANSPORT_WIFI,
            "",
            System.currentTimeMillis() - period,
            System.currentTimeMillis(),
        )

        do {
            // 같은 uid를 가진 버킷이 여러개 있을 수 있음
            //  --> 버킷을 돌면서 같은 uid에서 나온 txbytes는 모두 더해서 history와 비교해야함

            //앱의 총 송신 트래픽 계산
            val bucket = NetworkStats.Bucket()
            networkStats.getNextBucket(bucket)

            val uid = bucket.uid    // 앱 uid 반환

            // 기본 시스템 앱 pass
            if(uid == 1000 || uid == 0) continue

            val data = packageNameList.get(uid) // uid를 사용하여 (앱이름 + 패키지명, uid, 송신 트래픽 양) 반환

            if(data != null){
                if(!isInWhiteList(data.getPackageName())){
                    Log.e("UID", uid.toString())
                    Log.e("txBytes", bucket.txBytes.toString())

                    val curTxBytes = bucket.txBytes

                    // measureTxBytes 에 uid가 없다면 처음 발생한 송신 트래픽
                    //  --> measureTxBytes 에 새로 추가해줘야 함
                    if(measureTxBytes.contains(uid) == false){
                        data.setTxBytes(curTxBytes)
                        measureTxBytes.put(uid, data)
                    }
                    // measureTxBytes 가 uid가 있다면 이전에 해당 uid에서 송신 트래픽이 발생한 적 있음
                    //  --> 이전 송신 트래픽과 더해줘야 함
                    else{
                        // 이전 송신 트래픽 값
                        val prevTxBytes = measureTxBytes.get(uid)?.getTxBytes()

                        if (prevTxBytes != null) {
                            measureTxBytes.get(uid)?.setTxBytes(prevTxBytes + curTxBytes)
                        }
                    }
                }
            }

//            // data 가 유효하다면
//            if(data != null) {
//                // 화이트리스트에 들어가있지 않다면 송신 트래픽 측정
//                if(!isInWhiteList(data.getPackageName())) {
//                    Log.e("UID", uid.toString())
//                    Log.e("txBytes", bucket.txBytes.toString())
//
//                    var txBytes = bucket.txBytes    // 앱 별 송신 트래픽
//                    val bytes = data?.getTxBytes()
//                    val diff = txBytes - bytes!!    // 현재 계산한 txBytes - 이전에 계산한 txBytes
//
//                    // 차이가 양수면 송신 기록이 있다는 의미-> 사용자에게 보여줘야 함
//                    // 음수면 송신 데이터 양이 적어졌다는 뜻 --> 측정 시간동안 보내지 않았다고 할 수 있음
//                    if (diff > 0L) {
////                if (diff != 0L) {
//                        appHistory.put(uid, diff)
////                    if(appHistory.get(uid) == null){
////                        appHistory.put(uid, diff)
////                    }
////                    else{
////                        appHistory.replace(uid, diff)
////                    }
//                        // data 값 변경
//                        data.setTxBytes(diff)
//                        data.setDetectTime(getTime())
//
//                        // 가장 앞에 새로운 데이터 추가
//                        appDataList.add(0, data)
//                    }
//                }
//            }
        } while (networkStats.hasNextBucket())
        networkStats.close()

        // appHistory와 버킷에서 계산한 값들의 차이를 확인
        for(iter in measureTxBytes){
            var data = iter.value
            
            // appHistory에 없다면 appHistory와 appDataList에 추가
            if(appHistory.contains(data.getUid()) == false){
                appHistory.put(data.getUid(), data.getTxBytes())
                data.setDetectTime(getTime())
                appDataList.add(data)
            }
            // appHistory에 있다면 차이 계산
            else{
                val diff = data.getTxBytes() - appHistory.get(data.getUid())!!
                
                // 차이가 0보다 크면 송신 트래픽이 있다는 의미
                //  --> appDataList에 추가
                if(diff > 0){
                    data.setDetectTime(getTime())
                    appDataList.add(0, data)
                    appHistory.put(data.getUid(), diff)
                }
            }
        }
    }
}