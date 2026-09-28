package com.ziad.devoptionsquick

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.Locale

class SettingsAutomationService : AccessibilityService() {
    companion object {
        @Volatile private var instance: SettingsAutomationService? = null
        @Volatile private var running = false
        fun requestRun() { instance?.startAutomation() }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var step = 0
    private var buildClicks = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!running) return
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event?.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({ advance() }, 350)
        }
    }

    override fun onInterrupt() { }

    override fun onDestroy() {
        running = false
        instance = null
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun startAutomation() {
        if (running) return
        running = true
        step = 0
        buildClicks = 0
        launchAboutPhone()
    }

    private fun launchAboutPhone() {
        try {
            startActivity(Intent(Settings.ACTION_DEVICE_INFO_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    private fun advance() {
        if (!running) return
        val root = rootInActiveWindow ?: return
        when (step) {
            0 -> {
                if (findAny(root, "developer options", "developer mode")) {
                    step = 3
                    openDeveloperOptions()
                } else if (findAny(root, "build number", "build")) {
                    step = 1
                    clickBuildNumber(root)
                } else {
                    clickByText(root, "about phone", "about device", "phone information")
                }
            }
            1 -> clickBuildNumber(root)
            2 -> {
                step = 3
                openDeveloperOptions()
            }
            3 -> configureAnimations(root)
            4 -> finish()
        }
    }

    private fun clickBuildNumber(root: AccessibilityNodeInfo) {
        val node = findNode(root, "build number") ?: findNode(root, "build")
        if (node != null) {
            if (buildClicks < 7) {
                clickNode(node)
                buildClicks++
                handler.postDelayed({
                    if (buildClicks >= 7) {
                        step = 2
                        openDeveloperOptions()
                    } else {
                        advance()
                    }
                }, 450)
            }
        } else {
            step = 2
            openDeveloperOptions()
        }
    }

    private fun openDeveloperOptions() {
        handler.postDelayed({
            try {
                startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                step = 3
            } catch (_: Exception) { finish() }
        }, 250)
    }

    private var animationIndex = 0
    private val animationNames = arrayOf(
        "window animation scale",
        "transition animation scale",
        "animator duration scale"
    )

    private fun configureAnimations(root: AccessibilityNodeInfo) {
        if (animationIndex >= animationNames.size) {
            finish()
            return
        }

        val target = animationNames[animationIndex]
        val node = findNode(root, target)
        if (node == null) {
            val alt = when (animationIndex) {
                0 -> findNode(root, "window animation")
                1 -> findNode(root, "transition animation")
                else -> findNode(root, "animator duration")
            }
            if (alt == null) {
                animationIndex++
                advance()
                return
            }
            clickNode(alt)
        } else {
            clickNode(node)
        }

        handler.postDelayed({ chooseHalfX() }, 350)
    }

    private fun chooseHalfX() {
        val root = rootInActiveWindow ?: return
        val half = findNode(root, "0.5x")
            ?: findNode(root, ".5x")
            ?: findNode(root, "0.5")
            ?: findNode(root, ".5")

        if (half != null) {
            clickNode(half)
            animationIndex++
            handler.postDelayed({ advance() }, 500)
        } else {
            animationIndex++
            handler.postDelayed({ advance() }, 250)
        }
    }

    private fun finish() {
        running = false
        step = 4
        handler.removeCallbacksAndMessages(null)
    }

    private fun clickByText(root: AccessibilityNodeInfo, vararg values: String): Boolean {
        val node = values.asSequence().mapNotNull { findNode(root, it) }.firstOrNull() ?: return false
        return clickNode(node)
    }

    private fun findAny(root: AccessibilityNodeInfo, vararg values: String): Boolean =
        values.any { findNode(root, it) != null }

    private fun findNode(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val q = text.lowercase(Locale.ROOT)
        root.findAccessibilityNodeInfosByText(text).firstOrNull()?.let { return it }
        return findByTree(root, q)
    }

    private fun findByTree(node: AccessibilityNodeInfo?, query: String): AccessibilityNodeInfo? {
        if (node == null) return null
        val text = (node.text?.toString() ?: "").lowercase(Locale.ROOT)
        val desc = (node.contentDescription?.toString() ?: "").lowercase(Locale.ROOT)
        if (text.contains(query) || desc.contains(query)) return node
        for (i in 0 until node.childCount) {
            findByTree(node.getChild(i), query)?.let { return it }
        }
        return null
    }

    private fun clickNode(node: AccessibilityNodeInfo): Boolean {
        var n: AccessibilityNodeInfo? = node
        while (n != null) {
            if (n.isClickable) return n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            n = n.parent
        }
        return false
    }
}
