package com.example.mobiletrafficanalysis.Activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mobiletrafficanalysis.Fragment.GuideFragment
import com.example.mobiletrafficanalysis.Fragment.MonitorFragment
import com.example.mobiletrafficanalysis.Fragment.WhiteListFragment
import com.example.mobiletrafficanalysis.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.HashSet


class MainActivity : AppCompatActivity() {
    init {
        instance = this
    }

    companion object{
        var instance : MainActivity? = null
        var whiteList = ArrayList<Int>()            // 화이트 리스트 (ex. com.samsung.*, com.google.*)
        lateinit var prefs : SharedPreferences

        fun getActivity():Activity{
            return instance!!
        }

        @JvmName("getWhiteList1")
        fun getWhiteList() : ArrayList<Int>{
            return whiteList
        }

        @JvmName("setWhiteList1")
        fun setWhiteList(whiteList : ArrayList<Int>){
            this.whiteList = whiteList
        }
    }

    private var list = mutableListOf<ApplicationInfo>()         // 설치된 어플리케이션의 정보를 저장하는 리스트
    private var monitorFragment : MonitorFragment? = null       // 모니터링 프래그먼트
    private var whiteListFragment : WhiteListFragment? = null   // 화이트리스트 프래그먼트
    private var guideFragment : GuideFragment? = null           // 가이드 프래그먼트

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
            inItNavigationView()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    /**
     * 바텀 네비게이션 연결 및 선택 리스너 연결
     * replace를 통해 프래그먼트를 띄우면 새로 오픈되는 것이기 때문에 이전 정보가 남아있지 않음
     * show, hide를 통해 선택한 프래그먼트만 보여주도록 진행
     */
    private fun inItNavigationView() {
        // 바텀 네비게이션 바
        val bnv_menu = findViewById<BottomNavigationView>(R.id.bnv_main)
        bnv_menu.run{
            setOnItemSelectedListener {
                when(it.itemId){
                    // 모니터 프래그먼트 선택 시
                    R.id.Monitor -> {
                        // 처음 선택 시 초기화 및 추가
                        if (monitorFragment == null) {
                            monitorFragment = MonitorFragment()
                            supportFragmentManager.beginTransaction().add(R.id.fragment_container, monitorFragment!!).commit()
                        }

                        // 선택된 프래그먼트만 보이도록 변경
                        if(monitorFragment != null) supportFragmentManager.beginTransaction().show(monitorFragment!!).commit()
                        if(whiteListFragment != null) supportFragmentManager.beginTransaction().hide(whiteListFragment!!).commit()
                        if(guideFragment != null) supportFragmentManager.beginTransaction().hide(guideFragment!!).commit()
                    }
                    // 화이트리스트 프래그먼트 선택 시
                    R.id.WhiteList -> {
                        // 처음 선택 시 초기화 및 추가
                        if (whiteListFragment == null) {
                            whiteListFragment = WhiteListFragment()
                            supportFragmentManager.beginTransaction().add(R.id.fragment_container, whiteListFragment!!).commit()
                        }

                        // 선택된 프래그먼트만 보이도록 변경
                        if(monitorFragment != null) supportFragmentManager.beginTransaction().hide(monitorFragment!!).commit()
                        if(whiteListFragment != null) supportFragmentManager.beginTransaction().show(whiteListFragment!!).commit()
                        if(guideFragment != null) supportFragmentManager.beginTransaction().hide(guideFragment!!).commit()
                    }
                    // 가이드 프래그먼트 선택 시
                    R.id.Guide -> {
                        // 처음 선택 시 초기화 및 추가
                        if (monitorFragment == null) {
                            guideFragment = GuideFragment()
                            supportFragmentManager.beginTransaction().add(R.id.fragment_container, guideFragment!!).commit()
                        }

                        // 선택된 프래그먼트만 보이도록 변경
                        if(monitorFragment != null) supportFragmentManager.beginTransaction().hide(monitorFragment!!).commit()
                        if(whiteListFragment != null) supportFragmentManager.beginTransaction().hide(whiteListFragment!!).commit()
                        if(guideFragment != null) supportFragmentManager.beginTransaction().show(guideFragment!!).commit()
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
