package com.palsons.bulksmssendingpalsons.Other;

public class Global {

    public static String dateTime = null;
    public static String username = null;
    public static String password = null;
    public static String DBPrefix = "Aqua-Basale";
    public static int delay = 30;

    public static void clearGlobal() {
        dateTime = null;
        delay = 1;
    }
}