package com.emre.swipecounter

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.emre.swipecounter.data.SwipeDataStore
import com.emre.swipecounter.data.SwipeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SwipeDetectionService : AccessibilityService() {

    companion object {
        // Use distinct notification IDs to avoid conflicts
        private const val NOTIFICATION_ID_SERVICE = 1
        private const val NOTIFICATION_ID_ALERT = 999
        // Use v2 channel ID to force Android to recreate with high importance
        private const val LIMIT_ALERT_CHANNEL_ID = "limit_alert_channel_v2"
    }

    private var swipeCount: Int = 0 // Local cache for overlay
    private var dailyLimit: Int = 500 // Local cache for limit
    private var lastSwipeTime: Long = 0L
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    
    // Track if overlay is currently in "limit exceeded" state to avoid redundant updates
    private var isOverlayInLimitState: Boolean = false

    private var windowManager: WindowManager? = null
    private var overlayTextView: TextView? = null
    private var overlayLayoutParams: WindowManager.LayoutParams? = null

    private lateinit var repository: SwipeRepository
    private lateinit var dataStore: SwipeDataStore
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mainHandler = Handler(Looper.getMainLooper())

    private val allowedPackages: Set<String> = setOf(
        "com.zhiliaoapp.musically",
        "com.instagram.android",
        "com.ss.android.ugc.trill"
    )

    // Overlay styling constants
    private object OverlayStyle {
        // Normal state
        const val NORMAL_BG_COLOR = 0xAA000000.toInt()
        const val NORMAL_TEXT_COLOR = Color.WHITE
        const val NORMAL_TEXT_SIZE = 14f
        const val NORMAL_PADDING_H = 24
        const val NORMAL_PADDING_V = 16
        
        // Limit exceeded state (aggressive red)
        const val LIMIT_BG_COLOR = 0xFFFF0033.toInt()
        const val LIMIT_TEXT_COLOR = Color.WHITE
        const val LIMIT_TEXT_SIZE = 16f
        const val LIMIT_PADDING_H = 28
        const val LIMIT_PADDING_V = 20
    }

    private fun loadOrResetCounter() {
        serviceScope.launch {
            repository.checkAndResetIfNewDay()
            // Update local cache for overlay
            swipeCount = repository.totalCount.first()
            // Load initial daily limit
            dailyLimit = dataStore.dailyLimit.first()
            // Update overlay on main thread after data is loaded
            mainHandler.post { updateOverlayText() }
        }
    }

    private fun updateOverlayText() {
        mainHandler.post {
            overlayTextView?.let { tv ->
                val isLimitExceeded = swipeCount >= dailyLimit
                
                if (isLimitExceeded) {
                    tv.text = "⛔ $swipeCount"
                } else {
                    tv.text = "Swipes: $swipeCount"
                }
                
                // Only update styling if state changed (to avoid lag)
                if (isLimitExceeded != isOverlayInLimitState) {
                    updateOverlayStyle(isLimitExceeded)
                    isOverlayInLimitState = isLimitExceeded
                }
            }
        }
    }
    
    /**
     * Updates the overlay visual style based on limit state
     */
    private fun updateOverlayStyle(isLimitExceeded: Boolean) {
        overlayTextView?.let { tv ->
            if (isLimitExceeded) {
                // Aggressive RED style
                tv.setBackgroundColor(OverlayStyle.LIMIT_BG_COLOR)
                tv.setTextColor(OverlayStyle.LIMIT_TEXT_COLOR)
                tv.textSize = OverlayStyle.LIMIT_TEXT_SIZE
                tv.setTypeface(tv.typeface, Typeface.BOLD)
                tv.setPadding(
                    OverlayStyle.LIMIT_PADDING_H,
                    OverlayStyle.LIMIT_PADDING_V,
                    OverlayStyle.LIMIT_PADDING_H,
                    OverlayStyle.LIMIT_PADDING_V
                )
            } else {
                // Normal style
                tv.setBackgroundColor(OverlayStyle.NORMAL_BG_COLOR)
                tv.setTextColor(OverlayStyle.NORMAL_TEXT_COLOR)
                tv.textSize = OverlayStyle.NORMAL_TEXT_SIZE
                tv.setTypeface(tv.typeface, Typeface.NORMAL)
                tv.setPadding(
                    OverlayStyle.NORMAL_PADDING_H,
                    OverlayStyle.NORMAL_PADDING_V,
                    OverlayStyle.NORMAL_PADDING_H,
                    OverlayStyle.NORMAL_PADDING_V
                )
            }
            
            // Update layout to reflect size changes
            try {
                overlayLayoutParams?.let { params ->
                    windowManager?.updateViewLayout(tv, params)
                }
            } catch (e: Exception) {
                Log.e("SwipeCounter", "Failed to update overlay layout: ${e.message}")
            }
        }
    }

    private fun observeDailyLimit() {
        serviceScope.launch {
            dataStore.dailyLimit.collect { limit ->
                dailyLimit = limit
                Log.d("SwipeCounter", "Daily limit updated: $limit")
                // Re-check overlay state when limit changes
                mainHandler.post { updateOverlayText() }
            }
        }
    }
    
    private fun observeShowOverlay() {
        serviceScope.launch {
            dataStore.showOverlay.collect { show ->
                mainHandler.post {
                    overlayTextView?.visibility = if (show) View.VISIBLE else View.GONE
                }
                Log.d("SwipeCounter", "Show overlay updated: $show")
            }
        }
    }
    
    /**
     * Create notification channel for limit warnings (Android O+)
     * Using v2 channel ID to force recreation with high importance
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                LIMIT_ALERT_CHANNEL_ID,
                "Daily Limit Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alerts when you exceed your daily swipe limit"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("SwipeCounter", "Notification channel created: $LIMIT_ALERT_CHANNEL_ID")
        }
    }
    
    /**
     * Show a high-priority notification when limit is reached
     * Uses NOTIFICATION_ID_ALERT (999) to avoid conflicts with foreground service notifications
     */
    private fun showLimitNotification(currentCount: Int, limit: Int) {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = NotificationCompat.Builder(this, LIMIT_ALERT_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("⛔ DAILY LIMIT REACHED!")
                .setContentText("You have crossed your limit of $limit swipes. Current: $currentCount")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("You have crossed your daily limit of $limit swipes.\nCurrent count: $currentCount\nTake a break! 🛑"))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 300, 200, 300))
                .setLights(Color.RED, 1000, 500)
                .setDefaults(Notification.DEFAULT_ALL)
                .setFullScreenIntent(pendingIntent, true) // Makes it pop up like a call
                .build()
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID_ALERT, notification)
            
            Log.d("SwipeCounter", "Limit notification fired with ID $NOTIFICATION_ID_ALERT: $currentCount/$limit")
        } catch (e: Exception) {
            Log.e("SwipeCounter", "Failed to show notification: ${e.message}")
        }
    }

    override fun onCreate() {
        super.onCreate()

        val metrics = resources.displayMetrics
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        
        dataStore = SwipeDataStore(this)
        val database = com.emre.swipecounter.data.AppDatabase.getDatabase(this)
        repository = SwipeRepository(dataStore, database.swipeDao())
        loadOrResetCounter()
        
        // Create notification channel
        createNotificationChannel()
        
        // Start observing daily limit changes
        observeDailyLimit()
        
        // Start observing show overlay preference
        observeShowOverlay()

        if (!Settings.canDrawOverlays(this)) {
            Log.d("SwipeCounter", "Overlay permission not granted")
            return
        }

        windowManager = getSystemService(WINDOW_SERVICE) as? WindowManager
        if (windowManager == null) return
        
        // Check initial limit state
        val initialLimitExceeded = swipeCount >= dailyLimit
        isOverlayInLimitState = initialLimitExceeded

        val tv = TextView(this).apply {
            if (initialLimitExceeded) {
                text = "⛔ $swipeCount"
                setTextColor(OverlayStyle.LIMIT_TEXT_COLOR)
                setBackgroundColor(OverlayStyle.LIMIT_BG_COLOR)
                textSize = OverlayStyle.LIMIT_TEXT_SIZE
                setTypeface(typeface, Typeface.BOLD)
                setPadding(
                    OverlayStyle.LIMIT_PADDING_H,
                    OverlayStyle.LIMIT_PADDING_V,
                    OverlayStyle.LIMIT_PADDING_H,
                    OverlayStyle.LIMIT_PADDING_V
                )
            } else {
                text = "Swipes: $swipeCount"
                setTextColor(OverlayStyle.NORMAL_TEXT_COLOR)
                setBackgroundColor(OverlayStyle.NORMAL_BG_COLOR)
                textSize = OverlayStyle.NORMAL_TEXT_SIZE
                setPadding(
                    OverlayStyle.NORMAL_PADDING_H,
                    OverlayStyle.NORMAL_PADDING_V,
                    OverlayStyle.NORMAL_PADDING_H,
                    OverlayStyle.NORMAL_PADDING_V
                )
            }
        }

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 24
        }
        
        tv.setOnTouchListener(object : android.view.View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0f
            private var initialTouchY: Float = 0f

            override fun onTouch(v: android.view.View?, event: MotionEvent): Boolean {
                val lp = overlayLayoutParams ?: return false
                val wm = windowManager ?: return false

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = lp.x
                        initialY = lp.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        lp.x = initialX + (event.rawX - initialTouchX).toInt()
                        lp.y = initialY + (event.rawY - initialTouchY).toInt()
                        try {
                            wm.updateViewLayout(tv, lp)
                        } catch (_: Throwable) {
                        }
                        return true
                    }

                    else -> return false
                }
            }
        })

        try {
            windowManager?.addView(tv, params)
            overlayTextView = tv
            overlayLayoutParams = params
        } catch (t: Throwable) {
            Log.d("SwipeCounter", "Failed to add overlay: ${t.message}")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return
        if (!allowedPackages.contains(packageName)) return

        val source = event.source
        if (source != null) {
            val rect = Rect()
            source.getBoundsInScreen(rect)
            val height = rect.height()
            val width = rect.width()
            source.recycle() 

            if (height < screenHeight * 0.70 || width < screenWidth * 0.95) {
                return
            }
        }

        val currentTime = System.currentTimeMillis()

        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            if (currentTime - lastSwipeTime < 800L) return
            
            lastSwipeTime = currentTime
            incrementSwipeCount(packageName)
        }
    }

    private fun incrementSwipeCount(pkg: String) {
        // Update local cache immediately for UI responsiveness
        swipeCount += 1
        updateOverlayText()
        Log.d("SwipeCounter", "Swipe detected. package=$pkg count=$swipeCount")
        
        // Check daily limit and show warning (anti-spam logic)
        checkAndShowLimitWarning(swipeCount, dailyLimit)
        
        serviceScope.launch {
            // Log to DataStore (legacy)
            repository.incrementSwipe(pkg)
            // Sync local count with source of truth
            swipeCount = repository.totalCount.first()
        }
    }
    
    /**
     * Anti-spam limit warning logic:
     * - Show warning exactly when limit is reached
     * - Show warning every 50 swipes after the limit (e.g., at 500, 550, 600...)
     */
    private fun checkAndShowLimitWarning(currentCount: Int, limit: Int) {
        if (currentCount < limit) return
        
        val swipesOverLimit = currentCount - limit
        
        // Show warning when exactly at limit, or every 50 swipes after
        val shouldShowWarning = swipesOverLimit == 0 || swipesOverLimit % 50 == 0
        
        if (shouldShowWarning) {
            // Show toast
            mainHandler.post {
                Toast.makeText(
                    this,
                    "⚠️ Daily Limit Reached! ($currentCount/$limit)",
                    Toast.LENGTH_SHORT
                ).show()
            }
            
            // Show high-priority notification
            showLimitNotification(currentCount, limit)
            
            Log.d("SwipeCounter", "Limit warning shown: $currentCount/$limit")
        }
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        serviceScope.cancel()
        try {
            overlayTextView?.let { tv ->
                windowManager?.removeView(tv)
            }
        } catch (_: Throwable) {
        } finally {
            overlayTextView = null
            overlayLayoutParams = null
            windowManager = null
        }
        super.onDestroy()
    }
}
