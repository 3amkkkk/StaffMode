package nl.amk.staffmode;

import nl.amk.staffmode.command.StaffModeCommand;
import nl.amk.staffmode.database.DatabaseManager;
import nl.amk.staffmode.listener.PlayerConnectionListener;
import nl.amk.staffmode.service.StaffModeService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class StaffMode extends JavaPlugin {

    private DatabaseManager databaseManager;
    private StaffModeService staffModeService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        databaseManager = new DatabaseManager(this);
        databaseManager.start();

        staffModeService = new StaffModeService(this, databaseManager);

        var cmd = getCommand("staffmode");
        if (cmd != null) {
            var executor = new StaffModeCommand(staffModeService);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(staffModeService, databaseManager), this);

        for (var player : Bukkit.getOnlinePlayers()) {
            databaseManager.setStaffModeAsync(player.getUniqueId(), false);
        }
        staffModeService.refreshVisibility();
    }

    @Override
    public void onDisable() {
        if (staffModeService != null) {
            staffModeService.shutdown();
        }
        if (databaseManager != null) {
            databaseManager.stop();
        }
    }
}
