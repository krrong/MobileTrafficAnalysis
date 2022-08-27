package com.example.mobiletrafficanalysis.Class

import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon

class AppInfo {
    private var appName : String = ""
    private var icon : Drawable
    private var inWhiteList : Boolean = false   // 화이트 리스트에 있는지 여부

    constructor(appName:String, image:Drawable){
        this.appName = appName
        this.icon = image
    }

    fun getAppName() : String{
        return this.appName
    }

    fun getIcon() : Drawable? {
        return this.icon
    }

    fun getinWhiteList() : Boolean{
        return this.inWhiteList
    }

    fun setAppName(appName: String){
        this.appName = appName
    }

    fun setIcon(icon : Drawable){
        this.icon = icon
    }

    fun setinWhiteList(flag : Boolean){
        this.inWhiteList = flag
    }
}