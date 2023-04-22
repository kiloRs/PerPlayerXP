package com.profilesplus.menu;

import org.bukkit.entity.Player;

public interface MenuHandler {
    void handleOpen(Player player);
    void handleClose(Player player);
}
