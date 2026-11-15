package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.AppRoute
import com.example.state.FarmViewModel
import com.example.ui.components.CloudConflictDialog
import com.example.ui.components.SignInScreen
import com.example.ui.screens.ContentManagerScreen
import com.example.ui.screens.CreditsScreen
import com.example.ui.screens.GameplayHudScreen
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.HubScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.MapSelectionScreen
import com.example.ui.screens.MultiplayerScreen
import com.example.ui.screens.NewCareerSetupScreen
import com.example.ui.screens.PerformanceMonitorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.ShopScreenMode
import com.example.ui.screens.StorageManagerScreen
import com.example.ui.screens.WorkshopScreen
import com.example.ui.theme.AgroSprout
import com.example.ui.theme.FarmBrasilTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FarmBrasilTheme {
                FarmBrasilApp()
            }
        }
    }
}

@Composable
private fun FarmBrasilApp(viewModel: FarmViewModel = viewModel()) {
    val activityCtx = LocalContext.current
    val accountUser by viewModel.accountUser.collectAsStateWithLifecycle()
    val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    if (accountUser == null) {
        SignInScreen(
            authLoading = authLoading,
            authError = authError,
            isFirebaseConfigured = viewModel.isFirebaseConfigured,
            onTryAutoSignIn = { viewModel.tryAutoSignIn(activityCtx) },
            onSignInClick = { viewModel.signInWithGoogle(activityCtx) },
            onOfflineModeClick = { viewModel.enterOfflineMode() },
        )
        return
    }

    val user = accountUser!!
    key(user.uid) {
        val route by viewModel.route.collectAsStateWithLifecycle()
        val worldState by viewModel.worldState.collectAsStateWithLifecycle()
        val hasActiveCareer by viewModel.hasActiveCareer.collectAsStateWithLifecycle()
        val cameraMode by viewModel.cameraMode.collectAsStateWithLifecycle()
        val timeScale by viewModel.timeScale.collectAsStateWithLifecycle()
        val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
        val showFullMap by viewModel.showFullMap.collectAsStateWithLifecycle()
        val hudHidden by viewModel.hudHidden.collectAsStateWithLifecycle()
        val selectedMapId by viewModel.selectedMapForNewCareer.collectAsStateWithLifecycle()
        val workshopFocusUid by viewModel.workshopFocusUid.collectAsStateWithLifecycle()
        val careerSlots by viewModel.careerSlots.collectAsStateWithLifecycle()
        val hubPacks by viewModel.hubPacks.collectAsStateWithLifecycle()
        val installedPackIds by viewModel.installedPackIds.collectAsStateWithLifecycle()
        val hubFavorites by viewModel.hubFavorites.collectAsStateWithLifecycle()
        val downloadStates by viewModel.downloadStates.collectAsStateWithLifecycle()
        val quotaConfig by viewModel.quotaConfig.collectAsStateWithLifecycle()
        val settings by viewModel.settings.collectAsStateWithLifecycle()
        val perfStats by viewModel.perfStats.collectAsStateWithLifecycle()
        val cloudSaves by viewModel.cloudSaves.collectAsStateWithLifecycle()
        val cloudConflict by viewModel.cloudConflict.collectAsStateWithLifecycle()
        val isSyncingCloud by viewModel.isSyncingCloud.collectAsStateWithLifecycle()
        val openRooms by viewModel.openRooms.collectAsStateWithLifecycle()
        val activeRoomCode by viewModel.activeRoomCode.collectAsStateWithLifecycle()
        val roomMembers by viewModel.roomMembers.collectAsStateWithLifecycle()
        val roomMessages by viewModel.roomMessages.collectAsStateWithLifecycle()
        val mpStatus by viewModel.mpStatus.collectAsStateWithLifecycle()
        val mpIsHost by viewModel.mpIsHost.collectAsStateWithLifecycle()
        val constructionPreview by viewModel.constructionPreview.collectAsStateWithLifecycle()

        BackHandler(enabled = route != AppRoute.MainMenu || showFullMap || activeTab != null || constructionPreview != null) {
            when {
                constructionPreview != null -> viewModel.cancelConstructionPlacement()
                showFullMap -> viewModel.toggleFullMap(false)
                activeTab != null -> viewModel.openInGameTab(null)
                else -> viewModel.goBack()
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (route) {
                AppRoute.MainMenu -> MainMenuScreen(
                    accountUser = user,
                    hasActiveCareer = hasActiveCareer,
                    careerSlots = careerSlots,
                    cloudSaves = cloudSaves,
                    installedPackCount = installedPackIds.size,
                    onContinueCareer = {
                        if (hasActiveCareer) viewModel.navigateTo(AppRoute.Gameplay)
                        else viewModel.continueMostRecentCareer()
                    },
                    onLoadSlot = { viewModel.loadCareer(it) },
                    onDownloadCloudSlot = { viewModel.downloadCloudSlot(it) },
                    onNavigate = { viewModel.navigateTo(it) },
                    onSignOut = { viewModel.signOut() },
                )

                AppRoute.NewCareerSetup -> NewCareerSetupScreen(
                    selectedMapId = selectedMapId,
                    installedPackIds = installedPackIds,
                    careerSlots = careerSlots,
                    onSelectMap = { viewModel.setSelectedMapForNewCareer(it) },
                    onStartCareer = { slot, farm, mapId, money, mode, grid, app ->
                        viewModel.startNewCareer(slot, farm, mapId, money, mode, grid, app)
                    },
                    onBack = { viewModel.goBack() },
                )

                AppRoute.Gameplay -> GameplayHudScreen(
                    viewModel = viewModel,
                    worldState = worldState,
                    settings = settings,
                    cameraMode = cameraMode,
                    timeScale = timeScale,
                    activeTab = activeTab,
                    showFullMap = showFullMap,
                    hudHidden = hudHidden,
                    roomMembers = roomMembers,
                    localUid = user.uid,
                )

                AppRoute.MapSelection -> MapSelectionScreen(
                    selectedMapId = selectedMapId,
                    installedPackIds = installedPackIds,
                    onSelectAndStart = { mapId ->
                        viewModel.setSelectedMapForNewCareer(mapId)
                        viewModel.navigateTo(AppRoute.NewCareerSetup)
                    },
                    onBack = { viewModel.goBack() },
                )

                AppRoute.Dealership -> ShopScreen(
                    mode = ShopScreenMode.DEALERSHIP,
                    worldState = worldState,
                    installedPackIds = installedPackIds,
                    onBuyVehicle = { viewModel.buyVehicle(it) },
                    onBuyImplement = { viewModel.buyImplement(it) },
                    onBidAuction = { uid, bid -> viewModel.placeAuctionBid(uid, bid) },
                    onBack = { viewModel.goBack() },
                )

                AppRoute.ImplementShop -> ShopScreen(
                    mode = ShopScreenMode.IMPLEMENTS,
                    worldState = worldState,
                    installedPackIds = installedPackIds,
                    onBuyVehicle = { viewModel.buyVehicle(it) },
                    onBuyImplement = { viewModel.buyImplement(it) },
                    onBidAuction = { uid, bid -> viewModel.placeAuctionBid(uid, bid) },
                    onBack = { viewModel.goBack() },
                )

                AppRoute.UsedMarket -> ShopScreen(
                    mode = ShopScreenMode.USED,
                    worldState = worldState,
                    installedPackIds = installedPackIds,
                    onBuyVehicle = { viewModel.buyVehicle(it) },
                    onBuyImplement = { viewModel.buyImplement(it) },
                    onBidAuction = { uid, bid -> viewModel.placeAuctionBid(uid, bid) },
                    onBack = { viewModel.goBack() },
                )

                AppRoute.Garage -> GarageScreen(
                    worldState = worldState,
                    onSwitchVehicle = {
                        viewModel.switchToVehicle(it)
                        viewModel.navigateTo(AppRoute.Gameplay)
                    },
                    onOpenWorkshop = { viewModel.openWorkshopForVehicle(it) },
                    onSellVehicle = { viewModel.sellVehicle(it) },
                    onBack = { viewModel.goBack() },
                )

                AppRoute.Workshop -> WorkshopScreen(
                    worldState = worldState,
                    focusVehicleUid = workshopFocusUid,
                    onCustomize = { uid, patch -> viewModel.customizeVehicleUniversal(uid, patch) },
                    onRepairFull = { uid, refuel -> viewModel.repairVehicleFullAction(uid, refuel) },
                    onRepairComp = { uid, comp -> viewModel.repairVehicleCompAction(uid, comp) },
                    onPreventive = { viewModel.preventiveMaintenanceAction(it) },
                    onWash = { viewModel.washVehicleAction(it) },
                    onTow = { viewModel.towVehicleAction(it) },
                    inspectQuote = { viewModel.inspectVehicleQuote(it) },
                    onBack = { viewModel.goBack() },
                )

                AppRoute.Hub -> HubScreen(
                    packs = hubPacks,
                    favorites = hubFavorites,
                    downloadStates = downloadStates,
                    onStartDownload = { viewModel.startHubPackDownload(it) },
                    onPauseDownload = { viewModel.pauseHubDownload(it) },
                    onResumeDownload = { viewModel.resumeHubDownload(it) },
                    onCancelDownload = { viewModel.cancelHubDownload(it) },
                    onUpdatePack = { viewModel.updateHubPack(it) },
                    onUninstallPack = { viewModel.uninstallHubPack(it) },
                    onToggleFavorite = { viewModel.toggleHubFavorite(it) },
                    onImportCustomJson = { viewModel.importCustomHubMod(it) },
                    onBack = { viewModel.goBack() },
                )

                AppRoute.Multiplayer -> MultiplayerScreen(
                    accountUser = user,
                    openRooms = openRooms,
                    activeRoomCode = activeRoomCode,
                    roomMembers = roomMembers,
                    roomMessages = roomMessages,
                    mpStatus = mpStatus,
                    mpIsHost = mpIsHost,
                    onHostRoom = { code, shared, pause -> viewModel.hostMultiplayerRoom(code, shared, pause) },
                    onJoinRoom = { viewModel.joinMultiplayerRoom(it) },
                    onApproveMember = { uid, ok -> viewModel.approveRoomMember(uid, ok) },
                    onSendChat = { viewModel.sendRoomChat(it) },
                    onLeaveRoom = { viewModel.leaveMultiplayerRoom() },
                    onEnterGameplay = { viewModel.navigateTo(AppRoute.Gameplay) },
                    onBack = { viewModel.goBack() },
                )

                AppRoute.Settings -> SettingsScreen(
                    settings = settings,
                    onApplyPreset = { viewModel.applyGraphicsPreset(it) },
                    onUpdateCustomGraphics = { viewModel.updateCustomGraphics(it) },
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onUpdateHudLayout = { viewModel.updateHudLayout(it) },
                    onBack = { viewModel.goBack() },
                )

                AppRoute.ContentManager -> ContentManagerScreen(
                    careerSlots = careerSlots,
                    worldState = worldState,
                    cloudSaves = cloudSaves,
                    isSyncingCloud = isSyncingCloud,
                    onLoadSlot = { viewModel.loadCareer(it) },
                    onDeleteSlot = { viewModel.deleteSlot(it) },
                    onExportJson = { viewModel.exportCurrentCareerJson() },
                    onImportJson = { raw, slot -> viewModel.importCareerJson(raw, slot) },
                    onUploadCurrentToCloud = { viewModel.uploadCurrentToCloud() },
                    onDownloadCloudSlot = { viewModel.downloadCloudSlot(it) },
                    onDeleteCloudSlot = { viewModel.deleteCloudSlot(it) },
                    onBack = { viewModel.goBack() },
                )

                AppRoute.PerformanceMonitor -> PerformanceMonitorScreen(
                    perfStats = perfStats,
                    settings = settings,
                    onApplyPreset = { viewModel.applyGraphicsPreset(it) },
                    onToggleAutoOptimize = { auto -> viewModel.updateSettings { it.copy(autoOptimizePerformance = auto) } },
                    onBack = { viewModel.goBack() },
                )

                AppRoute.StorageManager -> StorageManagerScreen(
                    quotaConfig = quotaConfig,
                    computeBreakdown = { viewModel.storageBreakdown() },
                    onUpdateQuota = { viewModel.updateStorageQuotaConfig(it) },
                    onClearBackups = { viewModel.clearLocalBackups() },
                    onBack = { viewModel.goBack() },
                )

                AppRoute.Credits -> CreditsScreen(onBack = { viewModel.goBack() })
            }

            // Global cloud conflict resolution dialog
            cloudConflict?.let { conflict ->
                CloudConflictDialog(
                    conflict = conflict,
                    onResolve = { choice -> viewModel.resolveCloudConflict(choice) },
                )
            }

            // Status alert banner on non-gameplay screens
            if (route != AppRoute.Gameplay && worldState.statusAlert != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AgroSprout.copy(alpha = 0.6f)),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 6.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = worldState.statusAlert ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        IconButton(
                            onClick = { viewModel.clearStatusAlert() },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "Fechar aviso", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
