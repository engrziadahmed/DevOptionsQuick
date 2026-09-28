package com.ziad.devoptionsquick

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.app.Activity

class MainActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) updateStatus()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(48, 72, 48, 48)
            setBackgroundColor(getColor(R.color.background))
        }

        val title = TextView(this).apply {
            text = "Dev Options Quick"
            textSize = 28f
            setTextColor(getColor(R.color.blue))
            gravity = Gravity.CENTER
        }
        root.addView(title, lp())

        val subtitle = TextView(this).apply {
            text = "Enable Developer Options and set all animation scales to 0.5×"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 32)
        }
        root.addView(subtitle, lp())

        status = TextView(this).apply { textSize = 15f; gravity = Gravity.CENTER }
        root.addView(status, lp())

        val enableService = Button(this).apply {
            text = "1. Enable Accessibility Service"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
        root.addView(enableService, lp())

        val run = Button(this).apply {
            text = "2. Enable + Set 0.5×"
            setOnClickListener {
                if (!isServiceEnabled()) {
                    Toast.makeText(this@MainActivity, "Enable the accessibility service first.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                } else {
                    SettingsAutomationService.requestRun()
                    Toast.makeText(this@MainActivity, "Automation started.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        root.addView(run, lp())

        val note = TextView(this).apply {
            text = "No root or ADB is required. Android/OEM Settings layouts can vary, so the automation is designed primarily for stock/AOSP-style Settings."
            textSize = 13f
            setPadding(0, 28, 0, 0)
            gravity = Gravity.CENTER
        }
        root.addView(note, lp())

        setContentView(root)
        updateStatus()
    }

    private fun updateStatus() {
        status.text = if (isServiceEnabled()) "✓ Accessibility service is enabled" else "⚠ Accessibility service is not enabled"
    }

    private fun isServiceEnabled(): Boolean {
        val manager = getSystemService(ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { info ->
                val component = ComponentName.unflattenFromString(info.resolveInfo.serviceInfo.let {
                    "${it.packageName}/${it.name}"
                })
                component?.packageName == packageName
            }
    }

    private fun lp() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        bottomMargin = 18
    }
}
