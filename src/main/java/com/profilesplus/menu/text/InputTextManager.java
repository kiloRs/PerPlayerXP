package com.profilesplus.menu.text;

import com.profilesplus.menu.InputTextType;
import com.profilesplus.menu.text.InputTextData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class InputTextManager {
    private final Map<UUID, InputTextData> inputTextDataMap;

    public InputTextManager() {
        inputTextDataMap = new HashMap<>();
    }

    public void startInputMode(Player player, InputTextType inputTextType, Consumer<String> callback) {
        UUID playerUUID = player.getUniqueId();
        InputTextData inputTextData = new InputTextData(playerUUID, inputTextType, callback);
        inputTextDataMap.put(playerUUID, inputTextData);
    }

    public void stopInputMode(Player player) {
        inputTextDataMap.remove(player.getUniqueId());
    }

    public boolean isInInputMode(Player player) {
        return inputTextDataMap.containsKey(player.getUniqueId());
    }

    public void handleChatInput(Player player, String message) {
        InputTextData inputTextData = inputTextDataMap.get(player.getUniqueId());
        inputTextData.getCallback().accept(message);
    }

}


