package com.example.mobiletrafficanalysis

import android.R
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.*
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DetectService() : Service() {
    var touchDetect : TouchDetect? = null       // 터치 감지 클래스
    var windowManager : WindowManager? = null   // 오버레이 뷰 생성을 위해 필요
    var overlayView : View? = null              // 오베레이 뷰
    var trafficMonitor : TrafficMonitor? = null // 송신 트래픽 감지 클래스
    var list = mutableListOf<ApplicationInfo>() // 설치된 어플리케이션의 정보를 저장하는 리스트
    var whiteList = ArrayList<Int>()            // 화이트 리스트 (ex. com.samsung.*, com.google.*)
    var appDataList = ArrayList<Data>()         // 어댑터에 추가할 앱 리스트 (패키지명, 총 송신 트래픽)으로 구성
    var appHistory = HashMap<Int, Long>()       // 앱의 이전 송신 트래픽 양 저장

    override fun onBind(p0: Intent?): IBinder? {
        Log.e("DetectService", "onBind")
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("DetectService", "onCreate 함수 실행")

        initView()      // 리사이클러뷰 연결
        makeWhiteList() // 화이트리스트 생성

        // 메인 액티비티
        val mainActivity : Activity = MainActivity.getActivity()

        touchDetect = TouchDetect(mainActivity, whiteList)  // 터치 감지 클래스 초기화
        trafficMonitor = TrafficMonitor(mainActivity, appDataList, whiteList, list, appHistory, touchDetect!!) // 송신 트래픽 감지 클래스 초기화
    }

    /**
     * MainActivity의 recycler를 받아옴
     * DetectService가 가지고 있는 appDataList를 어댑터 리스트로 사용
     */
    fun initView(){
        val mainActivity : Activity = MainActivity.getActivity()    // 메인 액티비티
        val recyclerView = mainActivity.findViewById<RecyclerView>(com.example.mobiletrafficanalysis.R.id.recyclerView)     // 리사이클러뷰
        val layoutManager = LinearLayoutManager(this)
        val appAdapter = AppAdapter(appDataList)    // 어댑터
        val dividerItemDecoration = DividerItemDecoration(recyclerView?.context, layoutManager.orientation) // 구분선

        recyclerView?.adapter = appAdapter
        recyclerView?.layoutManager = layoutManager
        recyclerView?.addItemDecoration(dividerItemDecoration)
        recyclerView?.setHasFixedSize(true)
    }

    // startService() 메서드 실행 시 콜백되는 함수
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("DetectService", "onStartCommand 함수 실행")

        setNotification()   // Notification 생성 및 알림
        createOverlay()     // 오버레이 뷰 생성

        // 모니터링 시작
        trafficMonitor!!.startMonitoring()

        return Service.START_REDELIVER_INTENT
    }

    // 서비스 종료 시
    override fun onDestroy() {
        super.onDestroy()
        Log.d("DetectService", "onDestroy 함수 실행")

        // 모니터링 종료
        trafficMonitor!!.stopMonitoring()

        // 오버레이 삭제
        if(windowManager != null) {
            if(overlayView != null) {
                windowManager!!.removeView(overlayView);
                overlayView = null;
            }
            windowManager = null;
        }

        stopForeground(true);
        stopSelf();
    }

    /**
     * 오버레이 뷰 생성
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createOverlay() {
        val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // 오버레이
        val params = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,    // width
            ViewGroup.LayoutParams.WRAP_CONTENT,    // height
            30,     // x 좌표
            30,     // y 좌표
            TYPE_APPLICATION_OVERLAY,   // type
                                        // TYPE_APPLICATION_OVERLAY : 최상위에 있도록 해주는 타입
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
//                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT // format
                                    // PixelFormat.TRANSLUCENT : 배경을 투명하게 설정
        )
        // FLAG_NOT_FOCUSABLE        : 포커스를 갖지 않도록 함
        // FLAG_NOT_TOUCH_MODAL      : 윈도우 영역 밖의 다른 윈도우가 터치 이벤트를 받을 수 있도록 함
        // FLAG_WATCH_OUTSIDE_TOUCH  : 윈도우 밖의 영역을 터치했을 경우 ACTION_OUTSIDE 터치 이벤트를 받을 수 있도록 함

        // 오버레이 위치 설정
        params.gravity = Gravity.LEFT or Gravity.BOTTOM
        
        // 오버레이 뷰 인플레이션
        overlayView = layoutInflater.inflate(com.example.mobiletrafficanalysis.R.layout.overlay_view, null)

        // 터치 감지 기능
        overlayView?.setOnTouchListener(object : View.OnTouchListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                Log.d("DetectService", "Occur Touch Event")
                // 터치 감지 시 포어그라운드 앱의 터치 발생 시간 업데이트
                touchDetect?.addEvent()
                return true
            }
        })

        // 오버레이 추가
        windowManager?.addView(overlayView, params)
    }

    /**
     * Notification 설정
     * startForegroundService()으로 서비스가 실행되면, 실행된 서비스는 5초 내에 startForeground()를 호출하여
     * 서비스가 실행 중이라는 Notificaiton을 등록해야 한다. 만약 호출하지 않으면, 시스템은 서비스를 강제로 종료시킨다.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun setNotification() {
        val notificationCompatBuilder : NotificationCompat.Builder

        // NotificationManager 객체 생성
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // API Level 26(O) 이상일 경우 Notification Channel이 필요
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelID = "MobileTrafficAnalysis"
            val channelName = "first channel"
            val channelDescription = "설명"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            // NotificationChannel 객체 생성
            val notificationChannel = NotificationChannel(channelID, channelName, importance)

            // 설명 설정
            notificationChannel.description = channelDescription

            // 채널에 대한 설정
            notificationChannel.lightColor = Color.RED

            // 시스템에 Notification Channel 등록
            notificationManager.createNotificationChannel(notificationChannel)

            // API Level 26(O) 이상에서는 Builder 생성자에 NotificationChannel의 아이디 값을 설정
            notificationCompatBuilder = NotificationCompat.Builder(this, channelID)
        }
        else{
            // 26버전 미만은 생성자에 context만 설정
            notificationCompatBuilder = NotificationCompat.Builder(this)
        }

        // 상단바를 내렸을 때 표시할 알림 속성 설정
        notificationCompatBuilder.let {
            it.setSmallIcon(com.example.mobiletrafficanalysis.R.drawable.ic_launcher_foreground)    // 작은 아이콘 설정
            it.setWhen(System.currentTimeMillis())                                                  // 시간 설정
            it.setContentTitle("MobileTrafficAnalysis")                                             // 알림 메시지 설정
            it.setContentText("어플리케이션이 백그라운드에서 실행 중 입니다.")                              // 알림 내용 설정
//            it.setDefaults(Notification.DEFAULT_VIBRATE)                                            // 알림과 동시에 진동 설정
//            it.setAutoCancel(true)                                                                  // 클릭 시 알림이 삭제되도록 설정
            it.setOngoing(true)                                                                     // 지속적인 알림인지 설정
        }

        // Notification 객체 생성
        val notification = notificationCompatBuilder.build()

        // Notification 등록
        startForeground(1, notification)
    }

    /**
     * com.google.* or com.samsung.* 으로 시작하는 패키지명은 whiteList 에 등록
     * list, whiteList 초기화
     */
    @SuppressLint("QueryPermissionsNeeded")
    private fun makeWhiteList(){
        // 설치되어 있는 모든 어플리케이션의 패키지명 가져오기
        list = this.packageManager.getInstalledApplications(0)

        // 화이트 리스트 초기화
        for (app in list){
            // com.google.* or com.samsung.* 으로 시작하는 패키지명은 whiteList 에 등록
            if(app.packageName.startsWith("com.google.") || app.packageName.startsWith("com.samsung.")){
                // 이미 화이트리스트에 등록되어 있지 않다면 추가
                if(!whiteList.contains(app.uid)){
                    whiteList.add(app.uid)
                }
            }
        }
    }
}