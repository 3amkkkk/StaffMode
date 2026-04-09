package nl.amk.staffmode.command;

import nl.amk.staffmode.service.StaffModeService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public final class StaffModeCommand implements CommandExecutor, TabCompleter {

    private final StaffModeService staffModeService;

    public StaffModeCommand(StaffModeService staffModeService) {
        this.staffModeService = staffModeService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (!player.hasPermission("staffmode.use")) {
            player.sendMessage("You don't have permission to use this command.");
            return true;
        }

        staffModeService.toggle(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}

