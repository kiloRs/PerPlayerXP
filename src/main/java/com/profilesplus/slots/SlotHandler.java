package com.profilesplus.slots;

import com.profilesplus.players.Profile;
import org.bukkit.entity.Player;

public interface SlotHandler {
    Profile getProfileBySlot(int slot);
    boolean isSlotEmpty(int slot);
    boolean isSlotLocked(int slot, Player player);
    boolean isSlotInUse(int slot);
}
