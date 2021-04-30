package com.ontimize.jee.common.util.logging;

public enum Level {

    TRACE("TRACE"), DEBUG("DEBUG"), INFO("INFO"), WARN("WARN"), ERROR("ERROR"), OFF("OFF");

    private final String name;

    Level(String name) {
        this.name = name;
    }

}
