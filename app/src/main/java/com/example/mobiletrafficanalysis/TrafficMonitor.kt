package com.example.mobiletrafficanalysis

import android.app.Activity
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.NetworkCapabilities
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class TrafficMonitor(private var context: Activity,
//                     private var context: Context,
                     private var appDataList: ArrayList<Data>,
                     private var whiteList: ArrayList<Int>,
                     private var list: MutableList<ApplicationInfo>,
                     private var appHistory: HashMap<Int, Long>,
                     private var touchDetect: TouchDetect){

    private val period : Long = 10000   // 송신 트래픽 계산 함수 재실행 시간
    private var timer : Timer? = null   // 타이머 객체
    private var timerTask : TimerTask? = null   // 타이머 태스크 객체 (실제로 하는 일)
    private var recyclerView : RecyclerView? = null

    // 생성자
    init {
        // 타이머 생성
        timer = Timer()

        // MainActivity의 리사이클러뷰를 받아옴
        recyclerView = context.findViewById<RecyclerView>(R.id.recyclerView)
    }

    /**
     * 패키지 명이 화이트 리스트에 있는지 반환
     */
    fun isInWhiteList(uid : Int) : Boolean {
        return whiteList.contains(uid)
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
     * 모니터링 시작 10초 주기로 트래픽 측정
     */
    fun startMonitoring(){
        Log.d("TrafficMonitor", "startMonitoring")
        timerTask = object : TimerTask(){
            override fun run() {
                getTxBytesForAllApp()

//                var activity = context as MainActivity
                context.runOnUiThread{
                    recyclerView?.adapter?.notifyDataSetChanged()
                }
            }
        }

        timer!!.schedule(timerTask, 0, period)
    }

    /**
     * 모니터링 종료
     */
    fun stopMonitoring(){
        Log.d("TrafficMonitor", "stopMonitoring")
        timerTask?.cancel()
    }

    /**
     * 송신 트래픽이 발생한 어플리케이션의 송신 트래픽 계산
     */
    fun getTxBytesForAllApp() {
        // networkStatsManager 생성
        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

        // packageNameList는 uid를 이용하여 Data 객체를 가져오는 용도로 사용
        // measureTxBytes는 같은 uid에서 발생한 txBytes를 계산하고 history와 비교하는 용도로 사용
        val packageNameList : HashMap<Int, Data> = HashMap()
        var measureTxBytes : HashMap<Int, Data> = HashMap()

        for (app in list){
            // 앱 이름 얻어오기
            val label = context.packageManager.getApplicationLabel(app)

            // packageNameList 는 uid를 이용하여 Data 객체를 가져오는 용도로 사용
            packageNameList.put(app.uid, Data(label.toString() + "(" + app.packageName + ")", "", app.uid, 0L, -1))
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
                // TODO : 텍스트뷰의 색상을 변경할 수 있도록 dangerLevel 계산 필요
                
                // 패키지명
                val fir = data?.getPackageName()
                val sec = fir?.split("(")?.get(1)
                val appName = sec?.split(")")?.get(0)

                // 위험도 측정
                val dangerLevel : Int? = touchDetect?.measureRisk(uid, appName!!)
                if (dangerLevel != null) {
                    packageNameList.get(uid)?.setRiskLevel(dangerLevel)
                }

//                if(!isInWhiteList(data.getUid())){
                    Log.d("TrafficMonitor", "Uid : "+ uid.toString())
                    Log.d("TrafficMonitor", "Txbytes : " + bucket.txBytes.toString())

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
//                }
            }
        } while (networkStats.hasNextBucket())
        networkStats.close()

        // appHistory와 버킷에서 계산한 값들의 차이를 확인
        for(iter in measureTxBytes){
            val data = iter.value
            
            // appHistory에 없다면 appHistory와 appDataList에 추가
            if(appHistory.contains(data.getUid()) == false){
                appHistory.put(data.getUid(), data.getTxBytes())
                data.setDetectTime(getTime())
                appDataList.add(data)
                Log.e("TrafficMonitor", data.getPackageName()+ " " + data.getRiskLevel().toString())
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

                    Log.e("TrafficMonitor", data.getPackageName()+ " " + data.getRiskLevel().toString())
                }
            }
        }
    }
}