package nl.amk.staffmode.session;

import org.bukkit.inventory.ItemStack;

public final class StaffSession {

    private final ItemStack[] contents;
    private final ItemStack[] armorContents;
    private final ItemStack[] extraContents;
    private final int heldItemSlot;
    private final boolean allowFlight;
    private final boolean flying;

    public StaffSession(ItemStack[] contents,
                        ItemStack[] armorContents,
                        ItemStack[] extraContents,
                        int heldItemSlot,
                        boolean allowFlight,
                        boolean flying) {
        this.contents = contents;
        this.armorContents = armorContents;
        this.extraContents = extraContents;
        this.heldItemSlot = heldItemSlot;
        this.allowFlight = allowFlight;
        this.flying = flying;
    }

    public ItemStack[] contents() {
        return contents;
    }

    public ItemStack[] armorContents() {
        return armorContents;
    }

    public ItemStack[] extraContents() {
        return extraContents;
    }

    public int heldItemSlot() {
        return heldItemSlot;
    }

    public boolean allowFlight() {
        return allowFlight;
    }

    public boolean flying() {
        return flying;
    }
}
