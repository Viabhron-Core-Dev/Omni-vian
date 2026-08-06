package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.engine.fs.LocalFileManager
import com.example.ui.OmniRouteApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    LocalFileManager.init(applicationContext)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        OmniRouteApp()
      }
    }
  }
}
