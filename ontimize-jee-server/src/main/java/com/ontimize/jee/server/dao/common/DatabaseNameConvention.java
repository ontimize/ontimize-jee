package com.ontimize.jee.server.dao.common;

public class DatabaseNameConvention implements INameConvention {

    @Override
    public String convertName(String name) {
        return name;
    }

}
