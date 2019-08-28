package com.palsons.bulksmssendingpalsons.Other;

public class Global {

    public static String dateTime = null;
    public static String username = "admin";
    public static String password = "palsons123";
    public static String DBPrefix = "Aqua-Basale";
    public static int delay = 1;

    public static void clearGlobal() {
        dateTime = null;
        delay = 1;
    }
}