package com.example.mobiletrafficanalysis

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(private val context : Context, val appList : ArrayList<Data>) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.app_list_activity, parent, false)

        return ViewHolder(view);
    }

    override fun getItemCount(): Int {
        return appList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.bind(appList[position])
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
        fun setTextColor(dangerLevel : Int){
            when(dangerLevel){
                0 -> {
                    appName.setTextColor(ContextCompat.getColor(context, R.color.risk0))
                    occurTime.setTextColor(ContextCompat.getColor(context, R.color.risk0))
                    txBytes.setTextColor(ContextCompat.getColor(context, R.color.risk0))
                    uid.setTextColor(ContextCompat.getColor(context, R.color.risk0))
                }
                1 -> {
                    appName.setTextColor(ContextCompat.getColor(context, R.color.risk1))
                    occurTime.setTextColor(ContextCompat.getColor(context, R.color.risk1))
                    txBytes.setTextColor(ContextCompat.getColor(context, R.color.risk1))
                    uid.setTextColor(ContextCompat.getColor(context, R.color.risk1))
                }
                2 -> {
                    appName.setTextColor(ContextCompat.getColor(context, R.color.risk2))
                    occurTime.setTextColor(ContextCompat.getColor(context, R.color.risk2))
                    txBytes.setTextColor(ContextCompat.getColor(context, R.color.risk2))
                    uid.setTextColor(ContextCompat.getColor(context, R.color.risk2))
                }
                3 -> {
                    appName.setTextColor(ContextCompat.getColor(context, R.color.risk3))
                    occurTime.setTextColor(ContextCompat.getColor(context, R.color.risk3))
                    txBytes.setTextColor(ContextCompat.getColor(context, R.color.risk3))
                    uid.setTextColor(ContextCompat.getColor(context, R.color.risk3))
                }
            }

        }
    }
}