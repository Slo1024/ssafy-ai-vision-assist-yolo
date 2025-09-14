package com.project.lookey.path.utils;

public class BrandUtil {
    public static String detect(String name) {
        if (name == null) return null;
        String n = name.toLowerCase();
        if (n.contains("gs25")) return "GS25";
        if (n.contains("세븐일레븐") || n.contains("7-eleven") || n.contains("7 eleven")) return "7-ELEVEN";
        if (n.startsWith("cu") || n.contains(" cu")) return "CU";
        return null;
    }
}
