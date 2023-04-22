package com.profilesplus.menu;

import java.util.UUID;
import java.util.function.Consumer;

public class InputTextData {
    private final UUID playerUUID;
    private final InputTextType inputTextType;
    private final Consumer<String> callback;

    public InputTextData(UUID playerUUID, InputTextType inputTextType, Consumer<String> callback) {
        this.playerUUID = playerUUID;
        this.inputTextType = inputTextType;
        this.callback = callback;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public InputTextType getInputTextType() {
        return inputTextType;
    }

    public Consumer<String> getCallback() {
        return callback;
    }
}
