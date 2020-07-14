package com.ontimize.jee.server.dao.common;

public class UpperCaseNameConvention implements INameConvention {

    @Override
    public String convertName(String name) {
        return name != null ? name.toUpperCase() : name;
    }

}
