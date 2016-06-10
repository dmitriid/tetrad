package com.dmitriid.tetrad.services.utils;

public class TelegramUtils {
    public static String chatTitle(String fullname){
        return fullname.split("::")[0];
    }

    public static String chatId(String fullname){
        return fullname.split("::")[1];
    }
}
