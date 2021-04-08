package com.ontimize.jee.desktopclient.components.treetabbedformmanager.levelmanager;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.ontimize.util.FormatPattern;

public interface Level {

    String LEVEL_MANAGER = "levelmanager";

    String DISPLAY_TEXT_DATE_FORMAT = "displaytextdateformat";

    String DISPLAY_TEXT_FORMAT = "displaytextformat";

    String SHOW_IN_MAIN_FORM_AVAILABLE = "showinmainformavailable";

    String ID = "id";

    String PREVIOUS_LEVEL = "previouslevel";

    String NEXT_LEVEL = "nextlevel";

    String getDisplayText();

    Map<String, List<?>> getSelectedData();

    void setNextLevelId(String nextLevelId);

    String getNextLevelId();

    void setPreviousLevelId(String previousLevelId);

    String getPreviousLevelId();

    void reload();

    String getId();

    String getFormName();

    Map<?, ?> getKeysValues();

    List<?> getParentKeys();

    String getEntityName();

    FormatPattern getDetailFormatPattern();

    ResourceBundle getResourceBundle();

    LevelManager getLevelManager();

}
