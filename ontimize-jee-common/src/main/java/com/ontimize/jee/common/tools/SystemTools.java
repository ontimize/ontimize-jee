package com.ontimize.jee.common.tools;

public class SystemTools {

    public static boolean isWindowsOS() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    public static boolean isUnixOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("nix") || osName.contains("nux");
    }

}
