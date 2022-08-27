package com.example.mobiletrafficanalysis.Fragment

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrafficanalysis.Adapter.AppAdapter
import com.example.mobiletrafficanalysis.Adapter.WhiteListAdapter
import com.example.mobiletrafficanalysis.Class.AppInfo
import com.example.mobiletrafficanalysis.Class.Data
import com.example.mobiletrafficanalysis.R


class WhiteListFragment : Fragment() {
    var list = mutableListOf<ApplicationInfo>() // 설치된 어플리케이션의 정보를 저장하는 리스트
    var dataList = ArrayList<AppInfo>()         // 어댑터에 추가할 앱 리스트 (패키지명, 총 송신 트래픽)으로 구성

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_white_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        makeWhiteList()
        initView(view)
    }

    private fun initView(view: View){
        var whiteListRecyclerView = view.findViewById<RecyclerView>(R.id.WhiteListRecyclerView) // 화이트 리스트 레이아웃의 리사이클러뷰
        var WhiteListAdapter = WhiteListAdapter(dataList)  // 어댑터
        var layoutManager = LinearLayoutManager(activity)
        val dividerItemDecoration = DividerItemDecoration(whiteListRecyclerView?.context, layoutManager.orientation) // 구분선

        whiteListRecyclerView?.adapter = WhiteListAdapter
        whiteListRecyclerView?.layoutManager = layoutManager
        whiteListRecyclerView?.addItemDecoration(dividerItemDecoration)
        whiteListRecyclerView?.setHasFixedSize(true)
    }

    // TODO : 앱 목록을 리사이클러뷰에 보여주고 화이트 리스트에 등록할 수 있도록 기능 추가
    private fun makeWhiteList(){
        // 설치되어 있는 모든 어플리케이션의 패키지명 가져오기
        list = activity?.packageManager?.getInstalledApplications(0) as MutableList<ApplicationInfo>

        for (app in list){
            val packageName = app.packageName
            val label = activity?.packageManager?.getApplicationLabel(app) as String
            val icon = activity?.packageManager?.getApplicationIcon(packageName)
            dataList.add(AppInfo(label, icon!!))
        }
    }
}