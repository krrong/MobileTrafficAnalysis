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
import com.example.mobiletrafficanalysis.Activity.MainActivity
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class whiteListManager(mainActivity: Activity, dataList : ArrayList<AppInfo>) {
    private var activity = mainActivity
    private var whiteList = ArrayList<Int>()                // 화이트리스트 (ex. com.samsung.*, com.google.*)
    private var dataList = dataList                         // 화이트리스트 어댑터에 연결한 데이터리스트(이름, 아이콘, 화이트리스트 존재 여부)

    /**
     * 화이트리스트 초기화
     */
    fun initializeWhiteList(){
        var isInitialize = MainActivity.prefs.getString("initialize", null)



        val appSet = HashSet<Int>() // 네트워크 사용 앱 이름을 저장하는 set

        val networkStatsManager = activity.applicationContext?.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

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
        val apps = activity.packageManager?.getInstalledApplications(0)

        for (app in apps!!) {
            val packageName = app.packageName
            val label = activity.packageManager?.getApplicationLabel(app) as String
            val icon = activity.packageManager?.getApplicationIcon(packageName)
            val uid = app.uid

            // 네트워크 사용하지 이력이 없는 앱이면 pass
            if (!appSet.contains(uid)) continue

            // 초기화를 진행한 적 없다면 makeWhiteList() 함수를 이용하여 화이트리스트 생성
            if(isInitialize == null){
                makeWhiteList()

                // 초기화를 처음 진행하면 초기화 플래그 true 로 변경
                val editor = MainActivity.prefs.edit()
                isInitialize = "1"
                editor.putString("initialize", isInitialize)
                editor.apply()
            }
            // 초기화를 진행한 적이 있다면 저장된 화이트리스트를 불러온다.
            else{
                whiteList = loadWhiteList() as ArrayList<Int>
            }

            // 네트워크 사용한 이력이 있는 앱은 화이트리스트에 있는지 여부 플래그와 함께 저장
            val isinWhiteList = isinWhiteList(app.uid)
            dataList.add(AppInfo(label, icon!!, isinWhiteList))
        }
    }

    /**
     * 기존에 저장했던 화이트리스트 반환
     * @return 화이트리스트의 uid 를 MutableList<Int> 로 반환
     */
    fun loadWhiteList() : MutableList<Int> {
        // "," 기준으로 uid split
        val data = MainActivity.prefs.getString("uidSet", null)?.split(",")?.toMutableList()
        val result : MutableList<Int> = mutableListOf<Int>()

        // 각 원소를 Int 형태로 변경하여 다시 저장
        // 첫 번째 원소 형태 : "[ 1000"
        // 마지막 원소 형태 : " 1000]"
        // 나머지 원소 형태 : " 1000"
        for(i in 0 until data!!.size){
            if(i == 0){
                data[i] = data[i].removePrefix("[")
            }
            else if(i == data!!.size - 1){
                data[i] = data[i].removePrefix(" ")
                data[i] = data[i].removeSuffix("]")
            }
            else{
                data[i] = data[i].removePrefix(" ")
            }
            result.add(data[i].toInt())
        }
        return result
    }

    /**
     * 화이트리스트 새로 저장
     */
    fun saveWhiteList(){
        val editor = MainActivity.prefs.edit()
        val data = whiteList.toString()

        // 저장되어있던 SharedPreferences 삭제 후 다시 저장
        editor.clear()
        editor.putString("uidSet", data)
        editor.apply()
    }

    /**
     * com.google.* or com.samsung.* 으로 시작하는 패키지명은 whiteList 에 등록
     * whiteList 초기화
     */
    private fun makeWhiteList(){
        // 설치되어 있는 어플리케이션의 패키지명 가져오기
        val list = activity.packageManager?.getInstalledApplications(0) as MutableList<ApplicationInfo>

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