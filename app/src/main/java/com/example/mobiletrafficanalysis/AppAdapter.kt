package com.example.mobiletrafficanalysis

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

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
                    appName.setTextColor(Color.parseColor("#FF5EAEFF"))
                    occurTime.setTextColor(Color.parseColor("#FF5EAEFF"))
                    txBytes.setTextColor(Color.parseColor("#FF5EAEFF"))
                    uid.setTextColor(Color.parseColor("#FF5EAEFF"))
                }
                1 -> {
                    appName.setTextColor(Color.parseColor("#FFFFF200"))
                    occurTime.setTextColor(Color.parseColor("#FFFFF200"))
                    txBytes.setTextColor(Color.parseColor("#FFFFF200"))
                    uid.setTextColor(Color.parseColor("#FFFFF200"))
                }
                2 -> {
                    appName.setTextColor(Color.parseColor("#FFFF8040"))
                    occurTime.setTextColor(Color.parseColor("#FFFF8040"))
                    txBytes.setTextColor(Color.parseColor("#FFFF8040"))
                    uid.setTextColor(Color.parseColor("#FFFF8040"))
                }
                3 -> {
                    appName.setTextColor(Color.parseColor("#FFFF0000"))
                    occurTime.setTextColor(Color.parseColor("#FFFF0000"))
                    txBytes.setTextColor(Color.parseColor("#FFFF0000"))
                    uid.setTextColor(Color.parseColor("#FFFF0000"))
                }
            }

        }
    }
}