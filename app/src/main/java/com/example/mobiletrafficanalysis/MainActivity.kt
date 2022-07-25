package com.example.mobiletrafficanalysis

import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.Intent
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.net.Socket


class MainActivity : AppCompatActivity() {
    private val appDataList = ArrayList<Data>()     // 어댑터에 추가할 앱 리스트 (패키지명, 총 송신 트래픽)으로 구성
    private val whiteList = ArrayList<String>()     // 화이트 리스트 (ex. com.samsung.*, com.google.*)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // permission 이 없다면 요청
        if(!checkPermission()){
            requestPermission()
        }
        // permission 이 있다면 앱 별 송신 트래픽 계산 후 뷰 로드
        else{
            val trafficMonitorThread = TrafficMonitor(this, appDataList, whiteList)
            trafficMonitorThread.start()

            try{
                trafficMonitorThread.join()
            }catch (e : InterruptedException){
                e.printStackTrace()
            }
            initView()

            // 화이트리스트에 들어가있는 어플의 패키지명 및 길이 Log
            for(app in whiteList){
                Log.d("CHECK", app)
            }
            Log.d("CHECK", whiteList.size.toString())
        }
    }

    private fun initView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this)
        val appAdapter = AppAdapter(this, appDataList)

        recyclerView.adapter = appAdapter
        recyclerView.layoutManager = layoutManager
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
                val socket = Socket("172.30.1.6", 5000)
                val outStream = socket.getOutputStream()
                val data : Int = 3
                
                // 데이터 송신
                outStream.write(data)
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // 송신이 끝나면 어댑터 내용 변경 후 어댑터에게 알림 -> 화면에 뜨는 데이터 변경
            runOnUiThread{
                val transmitByteByMobile = TrafficStats.getUidTxBytes(android.os.Process.myUid())
                appDataList[0].setTxBytes(transmitByteByMobile)
                appAdapter.notifyDataSetChanged()
            }
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
}
