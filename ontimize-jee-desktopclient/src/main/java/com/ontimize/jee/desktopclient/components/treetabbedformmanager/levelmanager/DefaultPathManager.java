package com.ontimize.jee.desktopclient.components.treetabbedformmanager.levelmanager;

import java.awt.Component;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.ontimize.gui.field.Label;
import com.ontimize.util.ParseUtils;

public class DefaultPathManager implements PathManager {

    private static final String DISPLAY_PATH_SEPARATOR = "displaypathseparator";

    private static final String HANDLE_HTML = "displaypathhtml";

    private String separator;

    private boolean isDisplayPathHtml;

    private Label label;

    public DefaultPathManager() {
        this.init(new HashMap<>());
    }

    public DefaultPathManager(Map<Object, Object> parameters) {
        this.init(parameters);
    }

    private void init(Map<Object, Object> parameters) {
        this.separator = (String) parameters.get(DefaultPathManager.DISPLAY_PATH_SEPARATOR);
        this.isDisplayPathHtml = ParseUtils.getBoolean((String) parameters.get(DefaultPathManager.HANDLE_HTML), true);
    }

    public void appendLevel(int idx, String displayText, StringBuilder sb) {

        if (this.isDisplayPathHtml) {
            StringBuilder sb2 = new StringBuilder();
            for (int i = 0; i < idx; i++) {
                sb2.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            }
            sb.append("<p>");
            sb.append(sb2);
            if ((this.separator != null) && (idx != 0)) {
                sb.append(this.separator);
            } else if (idx != 0) {
                sb.append("-&gt");
            }
            sb.append(displayText);
            sb.append("</p>");
        } else {
            if ((this.separator != null) && (idx != 0)) {
                sb.append(this.separator);
            } else if (idx != 0) {
                sb.append("->");
            }
            sb.append(displayText);
        }

    }

    @Override
    public void showPath(List<Level> pathLevels) {

        StringBuilder sb = new StringBuilder();
        if (this.isDisplayPathHtml) {
            sb.append("<html><body>");
        }
        for (int i = 0; i < pathLevels.size(); i++) {
            this.appendLevel(i, pathLevels.get(i).getDisplayText(), sb);
        }
        if (this.isDisplayPathHtml) {
            sb.append("</body></html>");
        }
        this.label.setText(sb.toString());

    }

    @Override
    public void setLevelManager(LevelManager levelManager) {
        // do nothing
    }

    @Override
    public void setResourceBundle(ResourceBundle resourceBundle) {
        // do nothing
    }

    @Override
    public Component createGUIComponent() {
        Hashtable params = new Hashtable();
        params.put("attr", "actual");
        params.put("visible", "yes");
        params.put("labelvisible", "no");
        params.put("dim", "text");
        params.put("enabled", "no");
        this.label = new Label(params);
        return this.label;
    }

}
