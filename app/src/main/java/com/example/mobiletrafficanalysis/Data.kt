package com.example.mobiletrafficanalysis

public class Data {
    private var packageName : String = ""
    private var detectTime : String = ""
    private var uid : Int = 0
    private var txBytes : Long = 0
    private var riskLevel : Int = -1

    constructor(packageName:String, detectTime:String, uid : Int, txBytes:Long, riskLevel:Int){
        this.packageName = packageName
        this.detectTime = detectTime
        this.uid = uid
        this.txBytes = txBytes
        this.riskLevel = riskLevel
    }

    fun getPackageName(): String {
        return this.packageName
    }

    fun getDetectTime(): String {
        return this.detectTime
    }

    fun getTxBytes(): Long {
        return this.txBytes
    }

    fun getUid(): Int {
        return this.uid
    }

    fun getRiskLevel():Int{
        return this.riskLevel
    }

    fun setPackageName(packageName:String) {
        this.packageName = packageName
    }

    fun setDetectTime(detectTime:String) {
        this.detectTime = detectTime
    }

    fun setTxBytes(txBytes:Long) {
        this.txBytes = txBytes
    }

    fun setUid(uid:Int) {
        this.uid = uid
    }

    fun setRiskLevel(riskLevel: Int){
        this.riskLevel = riskLevel
    }
}