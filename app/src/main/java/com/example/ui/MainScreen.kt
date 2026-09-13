package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GoogleAccountDialog
import com.example.ui.screens.GuruAiScreen
import com.example.ui.screens.InstrumentsCatalogScreen
import com.example.ui.screens.ProfileHistoryScreen
import com.example.ui.screens.TunerScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TunerGreen

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val showAccountDialog by viewModel.showAccountDialog.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    // Prompt user on first run if no profile exists yet
    LaunchedEffect(userProfile) {
        if (userProfile == null) {
            viewModel.setShowAccountDialog(true)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurfaceCard,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                // 1. Tuner
                NavigationBarItem(
                    selected = currentTab == MainTab.TUNER,
                    onClick = { viewModel.setTab(MainTab.TUNER) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Tuner"
                        )
                    },
                    label = {
                        Text(
                            text = "Tuner",
                            fontWeight = if (currentTab == MainTab.TUNER) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TunerGreen,
                        selectedTextColor = TunerGreen,
                        indicatorColor = DarkSurfaceElevated,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_tuner")
                )

                // 2. Guru AI
                NavigationBarItem(
                    selected = currentTab == MainTab.GURU,
                    onClick = { viewModel.setTab(MainTab.GURU) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Guru AI"
                        )
                    },
                    label = {
                        Text(
                            text = "Guru AI",
                            fontWeight = if (currentTab == MainTab.GURU) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoldAccent,
                        selectedTextColor = GoldAccent,
                        indicatorColor = DarkSurfaceElevated,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_guru")
                )

                // 3. Instruments
                NavigationBarItem(
                    selected = currentTab == MainTab.INSTRUMENTS,
                    onClick = { viewModel.setTab(MainTab.INSTRUMENTS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Instruments"
                        )
                    },
                    label = {
                        Text(
                            text = "Instruments",
                            fontWeight = if (currentTab == MainTab.INSTRUMENTS) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TunerGreen,
                        selectedTextColor = TunerGreen,
                        indicatorColor = DarkSurfaceElevated,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_instruments")
                )

                // 4. Profile
                NavigationBarItem(
                    selected = currentTab == MainTab.PROFILE,
                    onClick = { viewModel.setTab(MainTab.PROFILE) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile"
                        )
                    },
                    label = {
                        Text(
                            text = "Profile",
                            fontWeight = if (currentTab == MainTab.PROFILE) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TunerGreen,
                        selectedTextColor = TunerGreen,
                        indicatorColor = DarkSurfaceElevated,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.TUNER -> TunerScreen(viewModel = viewModel)
                MainTab.GURU -> GuruAiScreen(viewModel = viewModel)
                MainTab.INSTRUMENTS -> InstrumentsCatalogScreen(viewModel = viewModel)
                MainTab.PROFILE -> ProfileHistoryScreen(viewModel = viewModel)
            }

            if (showAccountDialog) {
                GoogleAccountDialog(
                    currentProfile = userProfile,
                    onDismiss = { viewModel.setShowAccountDialog(false) },
                    onSaveProfile = { name, email ->
                        viewModel.saveGoogleProfile(name, email)
                    },
                    onContinueAsGuest = {
                        viewModel.continueAsGuest()
                    }
                )
            }
        }
    }
}
