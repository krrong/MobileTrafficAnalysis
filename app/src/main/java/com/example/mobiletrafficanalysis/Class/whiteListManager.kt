package com.example.mobiletrafficanalysis.Class

import android.annotation.SuppressLint
import android.app.Activity
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.NetworkCapabilities
import android.os.RemoteException
import androidx.fragment.app.FragmentActivity
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class whiteListManager(mainActivity: Activity, dataList : ArrayList<AppInfo>) {
    private var activity = mainActivity
    private var whiteList = ArrayList<Int>()                // 화이트 리스트 (ex. com.samsung.*, com.google.*)
    private var packageNameSet : HashSet<String>? = null    // 화이트리스트에 저장된 앱들의 package name
    private var isInitialize = false                        // 초기화 플래그
    private var dataList = dataList

    /**
     * 화이트리스트 초기화
     */
    fun initializeWhiteList(){
        // 이미 초기화를 진행했다면 진행하지 않음
        if(isInitialize == true)
            return

        // 초기화 플래그 true 로 변경
        isInitialize = true
        
        val appSet = HashSet<Int>() // 네트워크 사용 앱 이름을 저장하는 set

        val networkStatsManager = activity?.applicationContext?.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

        // 설치된 앱들 중에서 네트워크 사용 이력이 있는 앱들 정보 저장
        try {
            val networkStats = networkStatsManager.querySummary(
                NetworkCapabilities.TRANSPORT_WIFI,
                "",
                0,
                System.currentTimeMillis()
            )
            do {
                val bucket = NetworkStats.Bucket()
                networkStats.getNextBucket(bucket)
                val uid = bucket.uid

                // 기본 어플리케이션은 저장하지 않음
                if (uid == 0 || uid == 1000) continue
                
                appSet.add(uid)
                
            } while (networkStats.hasNextBucket())
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

        // 설치된 앱 중 네트워크 사용 이력 있는 앱들만 저장
        val apps = activity?.packageManager?.getInstalledApplications(0)

        for (app in apps!!) {
            val packageName = app.packageName
            val label = activity?.packageManager?.getApplicationLabel(app) as String
            val icon = activity?.packageManager?.getApplicationIcon(packageName)
            val uid = app.uid

            // 네트워크 사용하지 이력이 없는 앱이면 pass
            if (!appSet.contains(uid)) continue

            makeWhiteList()

            // 네트워크 사용한 이력이 있는 앱은 화이트리스트에 있는지 여부 플래그와 함께 저장
            val isinWhiteList = isinWhiteList(app.uid)
            dataList.add(AppInfo(label, icon!!, isinWhiteList))
        }
    }

    /**
     * 기존에 가지고 있는 화이트리스트를 반환
     */
    fun loadWhiteList() : HashSet<String>{
        val prefs = activity!!.getSharedPreferences("appUidSet", Context.MODE_PRIVATE)
        val data = prefs.getStringSet("packageNameSet", null)
        return HashSet(data)
    }

    /**
     * 화이트리스트를 새로 저장 
     */
    fun saveWhiteList(){
        val prefs = activity!!.getSharedPreferences("appUidSet", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // 저장되어있던 SharedPreferences 삭제 후 다시 저장
        editor.clear()  
        editor.putStringSet("packageNameSet", packageNameSet)
        editor.apply()
    }

    /**
     * com.google.* or com.samsung.* 으로 시작하는 패키지명은 whiteList 에 등록
     * whiteList 초기화
     */
    private fun makeWhiteList(){
        // 설치되어 있는 어플리케이션의 패키지명 가져오기
        val list = activity?.packageManager?.getInstalledApplications(0) as MutableList<ApplicationInfo>

        // 화이트 리스트에 추가
        for (app in list){
            // com.google.* or com.samsung.* 으로 시작하는 패키지명은 whiteList 에 등록
            if(app.packageName.startsWith("com.google.") || app.packageName.startsWith("com.samsung.")){
                if(!whiteList.contains(app.uid)){
                    whiteList.add(app.uid)
                }
            }
        }
    }

    /**
     * uid가 화이트리스트에 있는지 반환
     */
    fun isinWhiteList(uid : Int) : Boolean {
        // 화이트리스트에 패키지명이 포함되어 있으면 true 반환
        if(whiteList.contains(uid)){
            return true
        }
        return false
    }
}