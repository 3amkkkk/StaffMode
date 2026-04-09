package nl.amk.staffmode.listener;

import nl.amk.staffmode.database.DatabaseManager;
import nl.amk.staffmode.service.StaffModeService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerConnectionListener implements Listener {

    private final StaffModeService staffModeService;
    private final DatabaseManager databaseManager;

    public PlayerConnectionListener(StaffModeService staffModeService, DatabaseManager databaseManager) {
        this.staffModeService = staffModeService;
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        databaseManager.setStaffModeAsync(event.getPlayer().getUniqueId(), false);
        staffModeService.refreshVisibilityFor(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        staffModeService.disableIfActive(event.getPlayer());
        databaseManager.setStaffModeAsync(event.getPlayer().getUniqueId(), false);
    }
}

