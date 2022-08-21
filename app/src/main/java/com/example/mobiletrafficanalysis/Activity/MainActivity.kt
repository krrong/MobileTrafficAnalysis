package com.example.mobiletrafficanalysis.Activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mobiletrafficanalysis.Fragment.MonitorFragment
import com.example.mobiletrafficanalysis.Fragment.WhiteListFragment
import com.example.mobiletrafficanalysis.R
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    init {
        instance = this
    }

    companion object{
        var instance : MainActivity? = null

        fun getActivity():Activity{
            return instance!!
        }
    }

    private var whiteList = ArrayList<Int>()            // 화이트 리스트 (ex. com.samsung.*, com.google.*)
    private var list = mutableListOf<ApplicationInfo>() // 설치된 어플리케이션의 정보를 저장하는 리스트
    private var isMonitoring = 0                        // 모니터링이 진행 중인지 나타내는 플래그

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.e("MobileTraffic uid", android.os.Process.myUid().toString())

        // permission 이 없다면 요청
        if(!checkPermission()){
            requestPermission()

            checkPermission2()
        }
        // permission 이 있다면 뷰 로드 후 앱별 송신 트래픽 계산
        else{
            // 퍼미션이 없으면 요청
            checkPermission2()

            initView()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        // 바텀 네비게이션 바
        val bnv_menu = findViewById<BottomNavigationView>(R.id.bnv_main)
        bnv_menu.run{
            setOnItemSelectedListener { item ->
                when(item.itemId){
                    // 모니터링 페이지
                    R.id.Monitor -> {
                        val monitorFragment = MonitorFragment()
                        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, monitorFragment).commit()
                    }
                    // 화이트리스트 페이지
                    R.id.WhiteList -> {
                        val whiteListFragment = WhiteListFragment()
                        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, whiteListFragment).commit()
                    }
                }
                true
            }
            selectedItemId = R.id.Monitor
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
     * 다른 앱 위에 그리기 퍼미션 확인
     */
    private fun checkPermission2(){
        // 마시멜로우 이상인 경우
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // 다른 앱 위에 그리기 체크
            if(!Settings.canDrawOverlays(this)){
                val uri: Uri = Uri.fromParts("package", packageName, null)
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
                startActivity(intent)
            }
            else{
//                startDetectService()
            }
        }
        else{
//            startDetectService()
        }
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
     * list, whiteList 초기화
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
                if(!whiteList.contains(app.uid)){
                    whiteList.add(app.uid)
                }
//                if(!whiteList.contains(app.packageName)){
//                    whiteList.add(this.packageManager.getApplicationLabel(app).toString() + "(" + app.packageName + ")")
//                }
            }
        }
    }
}
