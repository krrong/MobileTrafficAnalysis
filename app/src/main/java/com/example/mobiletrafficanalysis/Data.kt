package com.example.mobiletrafficanalysis

public class Data {
    private var packageName : String = ""
    private var txBytes : Long = 0

    constructor(packageName:String, txBytes:Long){
        this.packageName = packageName
        this.txBytes = txBytes
    }

    fun getPackageName(): String {
        return this.packageName
    }

    fun getTxBytes(): Long {
        return this.txBytes
    }

    fun setPackageName(packageName:String) {
        this.packageName = packageName
    }

    fun setTxBytes(txBytes:Long) {
        this.txBytes = txBytes
    }
}