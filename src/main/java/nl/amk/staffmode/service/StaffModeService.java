package nl.amk.staffmode.service;

import nl.amk.staffmode.database.DatabaseManager;
import nl.amk.staffmode.session.StaffSession;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class StaffModeService {

    private final Plugin plugin;
    private final DatabaseManager databaseManager;
    private final Set<UUID> active = new HashSet<>();
    private final Map<UUID, StaffSession> sessions = new HashMap<>();

    public StaffModeService(Plugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public void toggle(Player player) {
        if (active.contains(player.getUniqueId())) {
            disable(player);
        } else {
            enable(player);
        }
    }

    public void enable(Player player) {
        UUID uuid = player.getUniqueId();
        if (active.contains(uuid)) {
            return;
        }

        var inv = player.getInventory();
        StaffSession session = new StaffSession(
                cloneItems(inv.getContents()),
                cloneItems(inv.getArmorContents()),
                cloneItems(inv.getExtraContents()),
                inv.getHeldItemSlot(),
                player.getAllowFlight(),
                player.isFlying()
        );
        sessions.put(uuid, session);

        clearInventory(player);
        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setCollidable(false);

        active.add(uuid);
        databaseManager.setStaffModeAsync(uuid, true);
        refreshVisibility();
        player.sendMessage("Staffmode enabled.");
    }

    public void disable(Player player) {
        UUID uuid = player.getUniqueId();
        if (!active.contains(uuid)) {
            return;
        }

        StaffSession session = sessions.remove(uuid);

        clearInventory(player);
        player.setGameMode(GameMode.SURVIVAL);
        player.setCollidable(true);

        if (session != null) {
            var inv = player.getInventory();
            inv.setContents(session.contents());
            inv.setArmorContents(session.armorContents());
            inv.setExtraContents(session.extraContents());
            inv.setHeldItemSlot(session.heldItemSlot());
            player.setAllowFlight(session.allowFlight());
            player.setFlying(session.flying());
        } else {
            player.setAllowFlight(false);
            player.setFlying(false);
        }

        active.remove(uuid);
        databaseManager.setStaffModeAsync(uuid, false);
        refreshVisibility();
        player.sendMessage("Staffmode disabled.");
    }

    public void disableIfActive(Player player) {
        if (active.contains(player.getUniqueId())) {
            disable(player);
        }
    }

    public void refreshVisibilityFor(Player viewer) {
        UUID viewerId = viewer.getUniqueId();
        boolean viewerIsStaff = active.contains(viewerId);
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.getUniqueId().equals(viewerId)) {
                continue;
            }
            boolean otherIsStaff = active.contains(other.getUniqueId());
            if (!viewerIsStaff && otherIsStaff) {
                viewer.hidePlayer(plugin, other);
            } else {
                viewer.showPlayer(plugin, other);
            }
        }
    }

    public void refreshVisibility() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            refreshVisibilityFor(viewer);
        }
    }

    public void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            disableIfActive(player);
        }
    }

    private void clearInventory(Player player) {
        var inv = player.getInventory();
        inv.clear();
        inv.setArmorContents(new ItemStack[4]);
        inv.setExtraContents(new ItemStack[1]);
        inv.setHeldItemSlot(0);
    }

    private ItemStack[] cloneItems(ItemStack[] items) {
        if (items == null) {
            return null;
        }
        return Arrays.stream(items).map(i -> i == null ? null : i.clone()).toArray(ItemStack[]::new);
    }
}
