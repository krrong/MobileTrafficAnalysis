package com.example.mobiletrafficanalysis.Fragment

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrafficanalysis.Activity.MainActivity
import com.example.mobiletrafficanalysis.Adapter.WhiteListAdapter
import com.example.mobiletrafficanalysis.Class.AppInfo
import com.example.mobiletrafficanalysis.Class.whiteListManager
import com.example.mobiletrafficanalysis.R


class WhiteListFragment : Fragment() {
    var list = mutableListOf<ApplicationInfo>()         // 설치된 어플리케이션의 정보를 저장하는 리스트
    var dataList = ArrayList<AppInfo>()                 // 어댑터에 추가할 앱 리스트 (패키지명, 총 송신 트래픽)으로 구성
    private var whiteList = ArrayList<Int>()            // 화이트 리스트(MainActivity 에서 생성한 것으로 사용)
    var whitelistManager : whiteListManager? = null     // 화이트리스트 매니저

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_white_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity : Activity = MainActivity.getActivity()         // MainActivity 에서 받아와 사용

        MainActivity.prefs = mainActivity.getSharedPreferences("packageNameSet", Context.MODE_PRIVATE)
        whitelistManager = whiteListManager(mainActivity, dataList)
        whitelistManager!!.initializeWhiteList()

        whiteList = whitelistManager!!.loadWhiteList() as ArrayList<Int>

//        getApplication()
        initView(view)
    }

    private fun initView(view: View){
        val whiteListRecyclerView = view.findViewById<RecyclerView>(R.id.WhiteListRecyclerView) // 화이트 리스트 레이아웃의 리사이클러뷰
        val whiteListAdapter = WhiteListAdapter(dataList, whitelistManager!!)  // 어댑터
        val layoutManager = LinearLayoutManager(activity)
        val dividerItemDecoration = DividerItemDecoration(whiteListRecyclerView?.context, layoutManager.orientation) // 구분선

        whiteListRecyclerView?.adapter = whiteListAdapter
        whiteListRecyclerView?.layoutManager = layoutManager
        whiteListRecyclerView?.addItemDecoration(dividerItemDecoration)
        whiteListRecyclerView?.setHasFixedSize(true)

        // 저장 버튼은 화이트리스트를 sharedPreferences로 저장 및 MainActivity의 whiteList에 저장
        val saveBtn = view.findViewById<Button>(R.id.saveBtn)
        saveBtn.setOnClickListener(View.OnClickListener {
            whitelistManager!!.saveWhiteList()
            MainActivity.setWhiteList(whitelistManager!!.getCurrentWhiteList())
        })

        // 테스트 버튼
        // TODO : button2는 추후에 삭제 필요
        val button2 = view.findViewById<Button>(R.id.button2)
        button2.setOnClickListener(View.OnClickListener {
            val testData = whitelistManager!!.loadWhiteList()
            Log.e("TEST","ASDF")
        })
    }


    /**
     * 설치된 어플리케이션을 리사이클러뷰 어댑터에 추가
     */
    private fun getApplication(){
        // 설치되어 있는 모든 어플리케이션의 패키지명 가져오기
        list = activity?.packageManager?.getInstalledApplications(0) as MutableList<ApplicationInfo>

        for (app in list){
            val packageName = app.packageName
            val label = activity?.packageManager?.getApplicationLabel(app) as String
            val icon = activity?.packageManager?.getApplicationIcon(packageName)
            val uid = app.uid

            val isinWhiteList = inWhiteList(uid)
            dataList.add(AppInfo(label, icon!!, isinWhiteList, uid))
        }
    }

    /**
     * uid가 화이트리스트에 있는지 반환
     */
    fun inWhiteList(uid : Int) : Boolean {
        // 화이트리스트에 패키지명이 포함되어 있으면 true 반환
        if(whiteList.contains(uid)){
            return true
        }
        return false
    }
}