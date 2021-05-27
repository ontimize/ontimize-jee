package com.ontimize.jee.common.locator;

import java.util.Map;

public interface InitialContext extends Map {

    public String TIME_ZONE = "TIME_ZONE";

    public String TIME = "TIME";

    public String SERVER_TIME_ZONE = "SERVER_TIME_ZONE";

    public String CLIENT_PERMISSION = "CLIENT_PERMISSION";

    public String LICENSE_OBJECT = "LICENSE_OBJECT";

    public String PRINTING_TEMPLATE = "PRINTING_TEMPLATE";

    public String REMOTE_APPLICATION_PREFERENCES = "REMOTE_APPLICATION_PREFERENCES";

    public String DB_BUNDLE_MANAGER_NAME = "DB_BUNDLE_MANAGER_NAME";

    public String CLIENT_LOCALE = "CLIENT_LOCALE";

    public String USER_KEYS = "USER_KEYS";

    public String ALL_RESOURCES_BUNDLES = "ALL_RESOURCES_BUNDLES";

    public String AVAILABLE_LOCALES = "AVAILABLE_LOCALES";

    public String ATTACHMENT = "ATTACHMENT";

    public String INIT_REMOTE_REFERENCES = "INIT_REMOTE_REFERENCES";

}
