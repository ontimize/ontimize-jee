package com.ontimize.jee.desktopclient.components.treetabbedformmanager.levelmanager;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.container.Column;
import com.ontimize.gui.container.Row;
import com.ontimize.util.ParseUtils;

public class LinkPathManager implements PathManager {

    private static final Logger logger = LoggerFactory.getLogger(LinkPathManager.class);

    private static final String DISPLAY_PATH_SEPARATOR = "displaypathseparator";

    private static final String HANDLE_HTML = "displaypathhtml";

    private String separator;

    private boolean isDisplayPathHtml;

    private LevelManager levelManager;

    private List<Level> pathLevels;

    private boolean ignorePaint = false;

    private Column column;

    private final List<Row> rows = new ArrayList<>();

    private ResourceBundle resourceBundle;

    public LinkPathManager() {
        this.init(new HashMap<>());
    }

    public LinkPathManager(Map<Object, Object> parameters) {
        this.init(parameters);
    }

    private void init(Map<Object, Object> parameters) {
        this.separator = (String) parameters.get(LinkPathManager.DISPLAY_PATH_SEPARATOR);
        this.isDisplayPathHtml = ParseUtils.getBoolean((String) parameters.get(LinkPathManager.HANDLE_HTML), true);
    }

    @Override
    public void showPath(List<Level> pathLevels) {
        this.ignorePaint = true;
        this.column.removeAll();
        LinkPathManager.this.rows.clear();

        this.pathLevels = pathLevels;
        int i = 0;
        for (Level level : LinkPathManager.this.pathLevels) {
            if (i == (this.pathLevels.size() - 1)) {
                LinkPathManager.addLevel(this.column, LinkPathManager.this.rows, level, LinkPathManager.this,
                        this.separator, true, this.resourceBundle);
            } else {
                LinkPathManager.addLevel(this.column, LinkPathManager.this.rows, level, LinkPathManager.this,
                        this.separator, false, this.resourceBundle);
            }
            i++;
        }
        for (Row r : LinkPathManager.this.rows) {
            r.updateUI();
        }
        this.ignorePaint = false;
        this.column.updateUI();
    }

    @Override
    public void setLevelManager(LevelManager levelManager) {
        this.levelManager = levelManager;
        this.setResourceBundle(levelManager.getResourceBundle());
    }

    @Override
    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    @Override
    public Component createGUIComponent() {
        Hashtable<Object, Object> params = new Hashtable<>();
        params.put("expandlast", "no");
        params.put("expand", "no");
        params.put("align", "left");
        this.column = new Column(params) {

            @Override
            protected void paintComponent(Graphics g) {
                if (!LinkPathManager.this.ignorePaint) {
                    super.paintComponent(g);
                }
            }

        };
        this.column.getSize();
        return this.column;
    }

    private static void addLevel(Column column, List<Row> rows, final Level level,
            final LinkPathManager linkPathManager, String separator, boolean isLast,
            ResourceBundle resourceBundle) {
        LinkLabel levelButton = null;
        if (isLast) {
            levelButton = new LinkLabel(LinkPathManager.translate(level.getEntityName(), resourceBundle));
            levelButton.setToolTipText(LinkPathManager.translate(level.getEntityName(), resourceBundle));
        } else {
            levelButton = new LinkLabel(level.getDisplayText());
            levelButton.setToolTipText(level.getDisplayText());
        }
        levelButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        levelButton.setHorizontalAlignment(SwingConstants.LEFT);
        levelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                linkPathManager.levelManager.show(level.getId());
            }
        });
        levelButton.setFocusable(false);

        boolean createRow = false;
        if (rows.size() == 0) {
            createRow = true;
        } else {
            Row current = rows.get(rows.size() - 1);
            if ((current.getPreferredSize().width + levelButton.getPreferredSize().width) > column.getSize().width) {
                createRow = true;
            }
        }
        Row r = null;
        if (createRow) {
            Hashtable<Object, Object> parameters = new Hashtable<>();
            parameters.put("expand", "no");
            parameters.put("expandlast", "no");
            parameters.put("align", "left");
            parameters.put("layout", "flow");
            r = new Row(parameters);
            column.add(r);
            rows.add(r);
        } else {
            r = rows.get(rows.size() - 1);
        }

        if ((rows.size() > 1) || (r.getComponentCount() > 0)) {
            JLabel l = new JLabel(separator);
            l.setOpaque(false);
            r.add(l);
        }
        r.add(levelButton);

    }

    private static String translate(String text, ResourceBundle resourceBundle) {
        if (resourceBundle == null) {
            return text;
        } else {
            try {
                return resourceBundle.getString(text);
            } catch (MissingResourceException e) {
                logger.trace(null, e);
                return text;
            }
        }
    }

    /**
     * An extension of JLabel which looks like a link and responds appropriately when clicked. Note that
     * this class will only work with Swing 1.1.1 and later. Note that because of the way this class is
     * implemented, getText() will not return correct values, user <code>getNormalText</code> instead.
     */

    private static class LinkLabel extends JLabel {

        /**
         * The normal text set by the user.
         */

        private String text;

        /**
         * Creates a new LinkLabel with the given text.
         */

        public LinkLabel(String text) {
            super(text);

            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            this.enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        }

        /**
         * Sets the text of the label.
         */

        @Override
        public void setText(String text) {
            super.setText("<html><font color=\"#0000CF\"><u>" + text + "</u></font></html>"); //$NON-NLS-1$ //$NON-NLS-2$
            this.text = text;
        }

        /**
         * Returns the text set by the user.
         */

        public String getNormalText() {
            return this.text;
        }

        /**
         * Processes mouse events and responds to clicks.
         */

        @Override
        protected void processMouseEvent(MouseEvent evt) {
            super.processMouseEvent(evt);
            if (evt.getID() == MouseEvent.MOUSE_CLICKED) {
                this.fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, this.getNormalText()));
            }
        }

        /**
         * Adds an ActionListener to the list of listeners receiving notifications when the label is
         * clicked.
         */

        public void addActionListener(ActionListener listener) {
            this.listenerList.add(ActionListener.class, listener);
        }

        /**
         * Removes the given ActionListener from the list of listeners receiving notifications when the
         * label is clicked.
         */

        public void removeActionListener(ActionListener listener) {
            this.listenerList.remove(ActionListener.class, listener);
        }

        /**
         * Fires an ActionEvent to all interested listeners.
         */

        protected void fireActionPerformed(ActionEvent evt) {
            Object[] listeners = this.listenerList.getListenerList();
            for (int i = 0; i < listeners.length; i += 2) {
                if (listeners[i] == ActionListener.class) {
                    ActionListener listener = (ActionListener) listeners[i + 1];
                    listener.actionPerformed(evt);
                }
            }
        }

    }

}
