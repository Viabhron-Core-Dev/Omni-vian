package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.engine.fs.LocalFileManager
import com.example.engine.omniroute.service.OmniRouteProxyService
import com.example.ui.OmniRouteApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    LocalFileManager.init(applicationContext)
    
    val serviceIntent = Intent(this, OmniRouteProxyService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(serviceIntent)
    } else {
        startService(serviceIntent)
    }
    
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        OmniRouteApp()
      }
    }
  }
}
