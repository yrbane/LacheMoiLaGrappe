package fr.lachemoilagrappe.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallFilterTileService : TileService() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onStartListening() {
        super.onStartListening()
        
        // Listen to the state and update the tile
        settingsRepository.filterUnknownEnabled
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
            // The flow in onStartListening will update the UI automatically
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        // No need to cancel scope here if we want to keep listening while visible, 
        // but typically we can recreate scope or just let the flow handle it.
        // Actually, best to just let it update.
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun updateTileState(isEnabled: Boolean) {
        val tile = qsTile ?: return
        
        tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = if (isEnabled) "Bouclier Actif" else "Bouclier Inactif"
        tile.subtitle = "Filtrer Inconnus"
        
        tile.updateTile()
    }
}
