package com.example.mobiletrafficanalysis.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrafficanalysis.Class.AppInfo
import com.example.mobiletrafficanalysis.R

class WhiteListAdapter(val dataList : ArrayList<AppInfo>) : RecyclerView.Adapter<WhiteListAdapter.ViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.app_whitelist, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        private val imageView = view.findViewById<ImageView>(R.id.imageView)
        private val appName = view.findViewById<TextView>(R.id.packageName)

        fun bind(item: AppInfo){
            appName?.text = "Name : " + item.getPackageName()
            imageView.setImageDrawable(item.getIcon())
        }
    }
}