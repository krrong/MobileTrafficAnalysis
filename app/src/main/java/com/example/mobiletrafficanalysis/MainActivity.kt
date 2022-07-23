package com.example.mobiletrafficanalysis

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.RemoteException
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {
    val appList = ArrayList<String>()
    val byteList = ArrayList<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val receiveByteByMobile = TrafficStats.getUidRxBytes();
//        val transmitByteByMobile = TrafficStats.getUidTxBytes();

//        appList.add(receiveByteByMobile.toString())
//        appList.add(transmitByteByMobile.toString())


        // 어플리케이션의 패키지명 가져오기
        val packageManager = packageManager
        val list = packageManager.getInstalledApplications(0)

        for (i in list.indices){
//            appList.add(list[i].packageName)
//            val receiveByteByMobile = TrafficStats.getUidRxBytes(list[i].uid);
//            byteList.add(receiveByteByMobile)
        }

        try {
            val networkStatsManager =
                applicationContext.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

            val networkStats = networkStatsManager.queryDetailsForUid(
                NetworkCapabilities.TRANSPORT_WIFI,
                "",
                0,
                System.currentTimeMillis(),
                0
            )

            do {
                val bucket = NetworkStats.Bucket()
                networkStats.getNextBucket(bucket)

                val txByte = bucket.txBytes
                byteList.add(txByte)

            } while (networkStats.hasNextBucket())
        }
        catch (e : Exception){
            e.printStackTrace()
            val builder = AlertDialog.Builder(this)
            builder.setMessage("허용해줭")
            builder.setPositiveButton(
                "확인",
                DialogInterface.OnClickListener{dialog, id ->
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    this.startActivity(intent)
                }
            )
            builder.setNegativeButton(
                "취소",
                DialogInterface.OnClickListener{dialog, id ->

                }
            )

            val alertDialog = builder.create()
            alertDialog.show()
        }

        initView()

    }


    private fun initView(){
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val appAdapter = AppAdapter(this, appList)
        val layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = appAdapter
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
    }
}