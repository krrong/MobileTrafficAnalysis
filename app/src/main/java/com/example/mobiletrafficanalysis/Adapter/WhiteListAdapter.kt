package com.example.mobiletrafficanalysis.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrafficanalysis.Activity.MainActivity
import com.example.mobiletrafficanalysis.Class.AppInfo
import com.example.mobiletrafficanalysis.Class.whiteListManager
import com.example.mobiletrafficanalysis.R

class WhiteListAdapter(val dataList : ArrayList<AppInfo>, val whiteListManager: whiteListManager) : RecyclerView.Adapter<WhiteListAdapter.ViewHolder>()  {
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
        private val addImageView = view.findViewById<ImageView>(R.id.addImageView)

        fun bind(item: AppInfo){
            appName?.text = item.getAppName()
            imageView.setImageDrawable(item.getIcon())
            isInWhiteList(item, addImageView)
            setAddImageViewClickListener(item.getUid())
        }

        // 화이트리스트에 있는지 반환
        fun isInWhiteList(item : AppInfo, addImageView: ImageView){
            if(item.getinWhiteList() == true){
                addImageView.setImageResource(R.drawable.minus)
            }
            else{
                addImageView.setImageResource(R.drawable.plus)
            }
        }

        // 
        fun setAddImageViewClickListener(uid : Int){
            addImageView.setOnClickListener(View.OnClickListener {
                // 화이트리스트에 있을 때 터치 시 화이트리스트에서 삭제 및 minus 이미지로 변경
                if(whiteListManager.isinWhiteList(uid) == true){
                    addImageView.setImageResource(R.drawable.plus)
                    whiteListManager.delete(uid)
                    Log.e("WhiteListAdapter", "addImageView plus -> minus")
                }
                // 화이트리스트에 없을 때 터치 시 화이트리스트에 추가 및 plus 이미지로 변경
                else{
                    addImageView.setImageResource(R.drawable.minus)
                    whiteListManager.add(uid)
                    Log.e("WhiteListAdapter", "addImageView minus -> plus")
                }
            })
        }
    }
}