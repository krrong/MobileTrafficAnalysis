package com.example.mobiletrafficanalysis.Adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrafficanalysis.Class.Data
import com.example.mobiletrafficanalysis.R

class AppAdapter(val appList : ArrayList<Data>) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.app_list_activity, parent, false)

        return ViewHolder(view);
    }

    override fun getItemCount(): Int {
        return appList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(appList[position])
    }

    inner class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        private val appName = itemView.findViewById<TextView>(R.id.packageName)
        private val occurTime = itemView.findViewById<TextView>(R.id.detectTime)
        private val txBytes = itemView.findViewById<TextView>(R.id.txBytes)
        private val uid = itemView.findViewById<TextView>(R.id.uid)

        fun bind(item: Data){
            appName?.text = "Name : " + item.getPackageName()
            occurTime?.text = "Detect Time : " + item.getDetectTime()
            txBytes?.text = "Transmit Bytes : " + item.getTxBytes().toString() + "bytes"
            uid?.text = "UID : " + item.getUid().toString()

            setTextColor(item.getRiskLevel())
        }

        // 위험도에 따른 색상 변경
        @SuppressLint("ResourceAsColor")
        fun setTextColor(dangerLevel : Int){
            when(dangerLevel){
                0 -> {
                    // 검은색
                    appName.setTextColor(Color.parseColor("#FF000000"))
                    occurTime.setTextColor(Color.parseColor("#FF000000"))
                    txBytes.setTextColor(Color.parseColor("#FF000000"))
                    uid.setTextColor(Color.parseColor("#FF000000"))
                }
                1 -> {
                    // 초록색
                    appName.setTextColor(Color.parseColor("#FF00A653"))
                    occurTime.setTextColor(Color.parseColor("#FF00A653"))
                    txBytes.setTextColor(Color.parseColor("#FF00A653"))
                    uid.setTextColor(Color.parseColor("#FF00A653"))
                }
                2 -> {
                    // 주황색
                    appName.setTextColor(Color.parseColor("#FFD96C00"))
                    occurTime.setTextColor(Color.parseColor("#FFD96C00"))
                    txBytes.setTextColor(Color.parseColor("#FFD96C00"))
                    uid.setTextColor(Color.parseColor("#FFD96C00"))
                }
                3 -> {
                    // 빨간색
                    appName.setTextColor(Color.parseColor("#FFFF0000"))
                    occurTime.setTextColor(Color.parseColor("#FFFF0000"))
                    txBytes.setTextColor(Color.parseColor("#FFFF0000"))
                    uid.setTextColor(Color.parseColor("#FFFF0000"))
                }
            }
            // FF5EAEFF : 하늘색
        }
    }
}