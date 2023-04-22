package com.profilesplus.slots;

import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerSlotHandler implements SlotHandler{
    private final PlayerData playerData;
    private List<String> profileOrder = new ArrayList<>();

    public void saveProfileOrder() {
        playerData.getConfig().set("profileOrder", profileOrder);
        playerData.saveConfig();
    }

    public void initializeProfilePlacement(){
        if (playerData.getConfig().isList("profileOrder")) {
            profileOrder = playerData.getConfig().getStringList("profileOrder");
        }
        else {
            profileOrder = new ArrayList<>();
        }

        saveProfileOrder();
    }
    public static PlayerSlotHandler get(PlayerData playerData){
        return new PlayerSlotHandler(playerData);
    }
    PlayerSlotHandler(PlayerData playerData){
        this.playerData = playerData;

        initializeProfilePlacement();
    }

    @Override
    public Profile getProfileBySlot(int slot) {
        if (slot >= 0 && slot < profileOrder.size()) {
            String profileName = profileOrder.get(slot);
            return playerData.getProfileMap().get(profileName);
        }
        return null;
    }

    @Override
    public boolean isSlotEmpty(int slot) {
        return slot >= profileOrder.size();
    }

    @Override
    public boolean isSlotLocked(int slot, Player player) {
        String requiredPermission = "profiles.slot." + (slot + 1);
        return !player.hasPermission(requiredPermission);
    }

    @Override
    public boolean isSlotInUse(int slot) {
        return !isSlotEmpty(slot) && playerData.getActiveProfile().equals(profileOrder.get(slot));
    }

}
