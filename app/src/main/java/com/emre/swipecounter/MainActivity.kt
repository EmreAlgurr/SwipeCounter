package com.emre.swipecounter

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.emre.swipecounter.ui.HomeViewModel
import com.emre.swipecounter.ui.OnboardingScreen
import com.emre.swipecounter.ui.SettingsScreen
import com.emre.swipecounter.ui.StatsScreen
import com.emre.swipecounter.ui.theme.SwipeCounterTheme
import com.emre.swipecounter.ui.CircularLimitIndicator
import com.emre.swipecounter.ui.AccessibilityDisclosureDialog

class MainActivity : ComponentActivity() {

    private lateinit var preferenceManager: PreferenceManager
    private var isOnboardingCompleted by mutableStateOf(false)
    
    // Permission request launcher for POST_NOTIFICATIONS (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.util.Log.d("SwipeCounter", "Notification permission granted")
        } else {
            android.util.Log.d("SwipeCounter", "Notification permission denied")
            Toast.makeText(
                this,
                "Bildirim izni verilmedi. Limit uyarıları gösterilmeyecek.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferenceManager = PreferenceManager(applicationContext)
        isOnboardingCompleted = preferenceManager.isOnboardingCompleted
        
        // Request notification permission on Android 13+ (Tiramisu)
        requestNotificationPermissionIfNeeded()

        setContent {
            SwipeCounterTheme {
                if (!isOnboardingCompleted) {
                    OnboardingScreen(
                        onFinished = {
                            preferenceManager.isOnboardingCompleted = true
                            isOnboardingCompleted = true
                        }
                    )
                } else {
                    MainApp()
                }
            }
        }
    }
    
    /**
     * Request POST_NOTIFICATIONS permission on Android 13+ (API 33)
     */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            when {
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    android.util.Log.d("SwipeCounter", "Notification permission already granted")
                }
                shouldShowRequestPermissionRationale(permission) -> {
                    // Show rationale and request
                    Toast.makeText(
                        this,
                        "Limit uyarıları için bildirim izni gerekli",
                        Toast.LENGTH_SHORT
                    ).show()
                    notificationPermissionLauncher.launch(permission)
                }
                else -> {
                    // Request directly
                    notificationPermissionLauncher.launch(permission)
                }
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val viewModel: HomeViewModel = viewModel()
    val isPremium by RevenueCatManager.isPremium.collectAsStateWithLifecycle()
    
    // Define items for bottom bar
    val items = listOf(
        Screen.Home,
        Screen.Stats,
        Screen.Settings
    )

    // Custom layout: Content -> Ad -> BottomNav
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // NavHost takes up all remaining space
        Box(modifier = Modifier.weight(1f)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Home.route) { 
                    HomeScreen(viewModel) 
                }
                composable(Screen.Stats.route) { 
                    StatsScreen() 
                }
                composable(Screen.Settings.route) { 
                    SettingsScreen(
                        onOpenPaywall = {
                            navController.navigate("paywall")
                        }
                    ) 
                }
                composable("paywall") {
                    com.emre.swipecounter.ui.PaywallScreen(
                        onClose = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
        
        // Ad Banner - visible on ALL screens for non-premium users
        if (!isPremium) {
            com.emre.swipecounter.ui.AdBanner(
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Bottom Navigation Bar
        NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            items.forEach { screen ->
                NavigationBarItem(
                    icon = { Icon(screen.icon, contentDescription = screen.label) },
                    label = { Text(screen.label) },
                    selected = currentRoute == screen.route,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Ana Sayfa", Icons.Default.Home)
    object Stats : Screen("stats", "Analiz", Icons.Default.DateRange)
    object Settings : Screen("settings", "Ayarlar", Icons.Default.Settings)
}

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val context = LocalContext.current
    // Update to use totalSwipeCount
    val swipeCount by viewModel.totalSwipeCount.collectAsStateWithLifecycle()
    val dailyLimit by viewModel.dailyLimit.collectAsStateWithLifecycle()
    val isLimitExceeded by viewModel.isLimitExceeded.collectAsStateWithLifecycle()
    var isServiceEnabled by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isServiceEnabled = isAccessibilityServiceEnabled(context, SwipeDetectionService::class.java)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }



    var showAccessibilityDisclosure by remember { mutableStateOf(false) }

    if (showAccessibilityDisclosure) {
        AccessibilityDisclosureDialog(
            onConfirm = {
                showAccessibilityDisclosure = false
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            },
            onDismiss = {
                showAccessibilityDisclosure = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "GÜNLÜK İLERLEME",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            CircularLimitIndicator(
                currentCount = swipeCount,
                dailyLimit = dailyLimit,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            if (isServiceEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AKTİF",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Arka planda çalışıyor",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Button(
                    onClick = {
                        showAccessibilityDisclosure = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "SERVİSİ BAŞLAT",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Sayacın çalışması için izin gerekli",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
    val expectedComponentName = ComponentName(context, serviceClass)
    val enabledServicesSetting = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    
    val colonSplitter = android.text.TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServicesSetting)
    
    while (colonSplitter.hasNext()) {
        val componentNameString = colonSplitter.next()
        val enabledComponent = ComponentName.unflattenFromString(componentNameString)
        if (enabledComponent != null && enabledComponent == expectedComponentName)
            return true
    }
    return false
}
