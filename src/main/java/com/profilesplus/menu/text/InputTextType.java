package com.profilesplus.menu.text;

import lombok.Getter;

public enum InputTextType {
    NAME("Invalid Name Input"),
    CLASS("Invalid Class Name Input"),
    OTHER("Invalid Input");

    @Getter
    private final String messageError;

    InputTextType(String inputTextTypeMessage){
        messageError = inputTextTypeMessage;
    }
}