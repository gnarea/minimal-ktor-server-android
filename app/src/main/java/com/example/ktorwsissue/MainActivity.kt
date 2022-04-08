package com.example.ktorwsissue

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ktorwsissue.Constant.Companion.HTTP_PORT
import com.example.ktorwsissue.service.KtorService

class MainActivity : AppCompatActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val i = Intent(this, KtorService::class.java)
        i.putExtra(HTTP_PORT, 8080)
        startService(i)
        findViewById<TextView>(R.id.serverStatusText).text = getString(R.string.serverStartedMessage)
        val localIpAddress = NetUtils.getIpAddressInLocalNetwork()
        if (localIpAddress != null) {
            findViewById<TextView>(R.id.ipAddressText).text =
                getString(R.string.localIpAddressMessage, localIpAddress)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}