package pl.example.connectnear

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import pl.example.connectnear.ui.theme.ConnectNearTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )
        enableEdgeToEdge()

        setContent {
            ConnectNearTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
                    val navController = rememberNavController()
                    val context = LocalContext.current

                    val locationPermissionRequest = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions(),
                        onResult = { permissions ->
                            if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                                startLocationService()
                            }
                        }
                    )

                    LaunchedEffect(Unit) {
                        locationPermissionRequest.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    }

                    val startDestination = "first_stage"
                    
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val bottomBarVisibleRoutes = listOf("fourth_stage", "friends_list_screen", "groups_screen", "ai_screen", "profile_screen", "events_screen")

                    Scaffold(
                        containerColor = Color.Transparent,
                        bottomBar = { 
                            if (currentRoute in bottomBarVisibleRoutes) {
                                BottomNavigationBar(navController = navController) 
                            }
                        }
                    ) { paddingValues ->
                        Box(modifier = Modifier.padding(paddingValues)) {
                            // POPRAWKA: Usunięto parametr userSelection
                            AppNavigation(
                                navController = navController,
                                startDestination = startDestination
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseService.clearUserLocation()
        stopLocationService()
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopLocationService() {
        val intent = Intent(this, LocationService::class.java)
        stopService(intent)
    }
}
