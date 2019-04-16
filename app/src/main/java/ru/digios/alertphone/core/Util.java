package ru.digios.alertphone.core;

public class Util {
    public static String clearPhone(final String phone) {
        return phone.replace("+", "")
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "").toLowerCase();
    }
}
