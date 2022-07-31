package com.example.mobiletrafficanalysis

import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.net.Socket
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    private var appDataList = ArrayList<Data>()         // 어댑터에 추가할 앱 리스트 (패키지명, 총 송신 트래픽)으로 구성
    private var appHistory = HashMap<Int, Long>()       // 앱의 이전 송신 트래픽 양 저장
    private var whiteList = ArrayList<String>()         // 화이트 리스트 (ex. com.samsung.*, com.google.*)
    private var list = mutableListOf<ApplicationInfo>() // 설치된 어플리케이션의 정보를 저장하는 리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.e("MobileTraffic uid", android.os.Process.myUid().toString())

        // permission 이 없다면 요청
        if(!checkPermission()){
            requestPermission()
        }
        // permission 이 있다면 앱 별 송신 트래픽 계산 후 뷰 로드
        else{
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
            initView(recyclerView)

            makeWhiteList()

            // TrafficMonitor 생성
            val trafficMonitor = TrafficMonitor(this, appDataList, whiteList, list, appHistory)

            // delay 만큼 주기적으로 트래픽 양 계산
            var timer = Timer()
            timer.schedule(object : TimerTask(){
                override fun run(){
                    try{
                        val startTime = System.currentTimeMillis()
                        trafficMonitor.getTxBytesForAllApp()
                        val endTime = System.currentTimeMillis()
                        Log.e("Elapse Time", (endTime - startTime).toString())
                    }catch(e : Exception){
                        Log.e("MainActivity", e.toString())
                    }

                    // 리사이클러뷰 어댑터 내용 수정 알림
                    runOnUiThread{
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                }
            }, 0, 10000) // delay는 task가 처음 실행될 때 영향, period는 task가 실행될 때마다 영향
        }
    }

    private fun initView(recyclerView : RecyclerView) {
        val layoutManager = LinearLayoutManager(this)
        val appAdapter = AppAdapter(this, appDataList)
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, layoutManager.orientation)

        recyclerView.adapter = appAdapter
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.setHasFixedSize(true)

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener(View.OnClickListener {
            val thread = NetworkThread(appAdapter)
            thread.start()
        })
    }

    /**
     * 서버로 값을 보내 송신 트래픽을 증가시키는 스레드
     */
    inner class NetworkThread(val appAdapter: AppAdapter) : Thread(){
        override fun run(){
            try {
                // 소켓 연결
                val socket = Socket("172.20.10.2", 5000)
                val outStream = socket.getOutputStream()
                val data : Int = 3
                
                // 데이터 송신
                outStream.write(data)
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
//            // 송신이 끝나면 어댑터 내용 변경 후 어댑터에게 알림 -> 화면에 뜨는 데이터 변경
//            runOnUiThread{
//                val transmitByteByMobile = TrafficStats.getUidTxBytes(android.os.Process.myUid())
//                appDataList[0].setTxBytes(transmitByteByMobile)
//                appAdapter.notifyDataSetChanged()
//            }
        }
    }

    /**
     * 런타임 퍼미션 체크 (PACKAGE_USAGE_STATS)
     */
    private fun checkPermission() : Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode : Int

        // 버전 29 이상은 unsafeCheckOpNoThrow() 를 사용해야함
        if(android.os.Build.VERSION.SDK_INT >= 29){
            mode = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), applicationContext.packageName )
        }
        // 버전 28 이하는 checkOpNoThrow() 를 사용해야함
        else{
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), applicationContext.packageName )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * 런타임 퍼미션 요청
     */
    private fun requestPermission(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage(
            """
            앱을 사용하기 위해서는 앱 실행 기록을 제공해야 합니다.
            설정을 수정하시겠습니까?
            """.trimIndent()
        )

        // 다이얼로그에 "확인" 버튼 추가 및 리스너 바인딩
        builder.setPositiveButton("확인"){_, _ ->
            val settingIntent = Intent(
                Settings.ACTION_USAGE_ACCESS_SETTINGS,
            )
            startActivity(settingIntent)
        }

        // 다이얼로그에 "취소" 버튼 추가 및 리스너 바인딩
        builder.setNegativeButton("취소"){_, _ ->
            // 무시
        }

        // 다이얼로그 보여주기
        val alertDialog = builder.create()
        alertDialog.show()
    }

    /**
     * com.google.* or com.samsung.* 으로 시작하는 패키지명은 whiteList 에 등록
     */
    private fun makeWhiteList(){
        // 설치되어 있는 어플리케이션의 패키지명 가져오기
        list = this.packageManager.getInstalledApplications(0)

        // 화이트 리스트에 추가
        for (app in list){
            // com.google.* or com.samsung.* 으로 시작하는 패키지명은 whiteList 에 등록
            if(app.packageName.startsWith("com.google.") || app.packageName.startsWith("com.samsung.")){
//                whiteList.add(app.packageName)
                // 이미 화이트리스트에 등록되어 있지 않다면 추가
                if(!whiteList.contains(app.packageName)){
                    whiteList.add(this.packageManager.getApplicationLabel(app).toString() + "(" + app.packageName + ")")
                }
            }
        }
    }
}
