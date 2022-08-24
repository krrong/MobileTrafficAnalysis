package com.example.mobiletrafficanalysis.Fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.mobiletrafficanalysis.Service.DetectService
import com.example.mobiletrafficanalysis.R
import java.net.Socket

class MonitorFragment : Fragment() {
    var isMonitoring = 0        // 모니터링이 진행 중인지 나타내는 플래그

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_monitor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inItView(view)
    }

    /**
     * 버튼 리스너 바인딩
     */
    fun inItView(view : View){
        val operationBtn = view.findViewById<Button>(R.id.operationBtn)
        operationBtn.setOnClickListener(View.OnClickListener {
            // 모니터링이 진행중이지 않다면 실행
            if(isMonitoring == 0){
                isMonitoring = 1
                startDetectService()
                operationBtn.text = "ON"
            }
            // 모니터링이 진행중이면 종료
            else{
                isMonitoring = 0
                stopDetectService()
                operationBtn.text = "OFF"
            }
        })

        val networkBtn = view.findViewById<Button>(R.id.networkBtn)
        networkBtn.setOnClickListener(View.OnClickListener {
            val networkThread = NetworkThread()
            networkThread.start()
        })
    }

    /**
     * 서버로 값을 보내 송신 트래픽을 증가시키는 스레드
     */
    inner class NetworkThread() : Thread(){
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
        }
    }

    /**
     * 서비스 실행
     */
    private fun startDetectService(){
        // 오레오 이상이면 startForegroundService 함수 사용
        var intent : Intent = Intent(activity, DetectService::class.java)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            activity?.startForegroundService(intent)
        }
        // 오레오 이하이면 startService 함수 사용
        else{
            activity?.startService(intent)
        }
    }

    /**
     * service 종료
     */
    fun stopDetectService(){
        val intent = Intent(activity, DetectService::class.java)
        activity?.stopService(intent)
    }
}