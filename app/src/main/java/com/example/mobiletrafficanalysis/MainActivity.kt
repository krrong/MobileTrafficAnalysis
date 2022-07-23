package com.example.mobiletrafficanalysis

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {
    val appList = ArrayList<String>()
    val byteList = ArrayList<Long>()
    private val REQUEST_CODE = 1004

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val receiveByteByMobile = TrafficStats.getUidRxBytes();
//        val transmitByteByMobile = TrafficStats.getUidTxBytes();

//        appList.add(receiveByteByMobile.toString())
//        appList.add(transmitByteByMobile.toString())


        // 어플리케이션의 패키지명 가져오기
        val packageManager = packageManager
        val list = packageManager.getInstalledApplications(0)

        for (i in list.indices) {
//            appList.add(list[i].packageName)
//            val receiveByteByMobile = TrafficStats.getUidRxBytes(list[i].uid);
//            byteList.add(receiveByteByMobile)
        }

        initView()
        checkPermission()
    }

    private fun initView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val appAdapter = AppAdapter(this, appList)
        val layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = appAdapter
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
    }

    /**
     * 런타임 퍼미션 체크 (PACKAGE_USAGE_STATS)
     */
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.PACKAGE_USAGE_STATS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("TEST", "PERMISSION_GRANTED")
        } else {
            showDialog()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("TEST", "GRANTED")
                } else {
                    showDialog()
                }
            }
        }
    }

    /**
     * 사용자가 직접 사용정보 접근 허용할 수 있도록 다이얼로그 생성
     */
    private fun showDialog() {
        // 다이얼로그 빌더 생성
        val builder = AlertDialog.Builder(this)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage(
            """
            앱을 사용하기 위해서는 앱 실행 기록을 제공해야 합니다.
            설정을 수정하시겠습니까?
            """.trimIndent()
        )
        builder.setCancelable(true)

        // 다이얼로그에 "설정" 버튼 추가 및 리스너 바인딩
        builder.setPositiveButton("설정") { dialog, id -> // 설정으로 들어가 바로 수정할 수 있도록 인텐트 실행
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
