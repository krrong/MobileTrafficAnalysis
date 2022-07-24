package com.example.mobiletrafficanalysis

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.TrafficStats
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.net.Socket


class MainActivity : AppCompatActivity() {
    val appDataList = ArrayList<Data>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 어플리케이션의 패키지명 가져오기
        val packageManager = packageManager
        val list = packageManager.getInstalledApplications(0)

//        val networkStatsManager = applicationContext.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
//
//        val networkStats = networkStatsManager.queryDetailsForUid(
//            NetworkCapabilities.TRANSPORT_WIFI,
//            "",
//        0,
//            System.currentTimeMillis(),
//            android.os.Process.myUid()
//        )
//
//        var txBytes : Long = 0
//
//        do{
//            val bucket = NetworkStats.Bucket()
//            networkStats.getNextBucket(bucket)
//
//            txBytes += bucket.txBytes
//
//        }while(networkStats.hasNextBucket())

        val transmitByteByMobile = TrafficStats.getUidTxBytes(android.os.Process.myUid())
        appDataList.add(Data(android.os.Process.myUid().toString(), transmitByteByMobile))

        initView()
        requestPermission()
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

    inner class NetworkThread(val appAdapter: AppAdapter) : Thread(){
        override fun run(){
            try {
                val socket = Socket("172.30.1.29", 5000)
                val outStream = socket.getOutputStream()
                val inStream = socket.getInputStream()
                val data : Int = 3
                outStream.write(data)
                socket.close()

            } catch (e: Exception) {
                e.printStackTrace()
            }
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
        if(!checkPermission()){
            val builder = AlertDialog.Builder(this)
            builder.setMessage(
                """
                앱을 사용하기 위해서는 앱 실행 기록을 제공해야 합니다.
                설정을 수정하시겠습니까?
                """.trimIndent()
            )

            // 다이얼로그에 "확인" 버튼 추가 및 리스너 바인딩
            builder.setPositiveButton("확인"){dialog, id ->
                val settingIntent = Intent(
                    Settings.ACTION_USAGE_ACCESS_SETTINGS,
                )
                startActivity(settingIntent)
            }

            // 다이얼로그에 "취소" 버튼 추가 및 리스너 바인딩
            builder.setNegativeButton("취소"){dialog, id ->
                // 무시
            }

            val alertDialog = builder.create()
            alertDialog.show()
        }
    }
}
