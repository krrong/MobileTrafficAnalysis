package com.example.mobiletrafficanalysis

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Binder
import android.os.IBinder
import android.util.Log

// 포그라운드 앱의 터치 감지 -> TouchDetect 를 통해 포그라운드 앱의 마지막 터치 시간 측정
class DetectService() : Service() {
    var touchDetect : TouchDetect? = null
    var activity = this@DetectService

    override fun onBind(p0: Intent?): IBinder? {
        Log.e("DetectService", "onBind")
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("DetectService", "onCreate 함수 실행")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("DetectService", "onDestroy 함수 실행")
    }

    // startService() 메서드 실행 시 콜백되는 함수
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("DetectService", "onStartCommand 함수 실행")

        var whiteList = intent?.getIntegerArrayListExtra("whiteList") as ArrayList<Int>

        touchDetect = TouchDetect(activity, whiteList)

        return Service.START_REDELIVER_INTENT
    }
}