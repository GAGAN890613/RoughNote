package com.gagan.roughnotes

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(28,28,28,28); setBackgroundColor(Color.rgb(12,18,32)) }
        root.addView(TextView(this).apply { text="RoughNotes"; textSize=30f; setTextColor(Color.WHITE); gravity=Gravity.CENTER })
        root.addView(TextView(this).apply { text="Floating transparent scratchpad • pen • shapes • colors • compact/full workspace"; textSize=16f; setTextColor(0xffcbd5e1.toInt()); gravity=Gravity.CENTER; setPadding(0,20,0,24) })
        root.addView(Button(this).apply { text="Grant overlay permission"; setOnClickListener { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) } })
        root.addView(Button(this).apply { text="Start floating notes"; setOnClickListener { if(Settings.canDrawOverlays(this@MainActivity)) { startService(Intent(this@MainActivity,OverlayService::class.java)); finish() } else startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:$packageName"))) } })
        setContentView(root)
    }
}
