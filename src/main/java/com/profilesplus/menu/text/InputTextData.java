package com.profilesplus.menu.text;

import lombok.Getter;

import java.util.UUID;
import java.util.function.Consumer;

@Getter
public class InputTextData {
    private final UUID playerUUID;
    private final InputTextType inputTextType;
    private final Consumer<String> callback;

    public InputTextData(UUID playerUUID, InputTextType inputTextType, Consumer<String> callback) {
        this.playerUUID = playerUUID;
        this.inputTextType = inputTextType;
        this.callback = callback;
    }
}
