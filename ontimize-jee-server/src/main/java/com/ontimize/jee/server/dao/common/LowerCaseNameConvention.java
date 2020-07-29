package com.ontimize.jee.server.dao.common;

public class LowerCaseNameConvention implements INameConvention {

    @Override
    public String convertName(String name) {
        return name != null ? name.toLowerCase() : name;
    }

}
