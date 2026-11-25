package fr.lachemoilagrappe.service

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import fr.lachemoilagrappe.R
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LacheMoiLaGrappeTileService : TileService() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var listeningJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        listeningJob?.cancel()
        listeningJob = settingsRepository.filterUnknownEnabled
            .onEach { isEnabled ->
                updateTileState(isEnabled)
            }
            .launchIn(serviceScope)
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return

        val isCurrentlyEnabled = tile.state == Tile.STATE_ACTIVE

        serviceScope.launch {
            settingsRepository.setFilterUnknownEnabled(!isCurrentlyEnabled)
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        listeningJob?.cancel()
        listeningJob = null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun updateTileState(isEnabled: Boolean) {
        val tile = qsTile ?: return

        tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = if (isEnabled) {
            getString(R.string.tile_shield_active)
        } else {
            getString(R.string.tile_shield_inactive)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = getString(R.string.filter_unknown_calls)
        }

        tile.updateTile()
    }
}
