package com.ontimize.jee.desktopclient.components.servermanagement.window;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.Application;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.FormManager;
import com.ontimize.gui.InteractionManager;
import com.ontimize.gui.manager.IFormManager;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.desktopclient.components.servermanagement.managers.IMDownloadLogFiles;
import com.ontimize.jee.desktopclient.components.servermanagement.managers.IMLiveLogConsole;
import com.ontimize.jee.desktopclient.components.servermanagement.managers.IMLiveLogToFile;
import com.ontimize.jee.desktopclient.components.servermanagement.managers.IMMemory;
import com.ontimize.jee.desktopclient.components.servermanagement.managers.IMRequest;
import com.ontimize.jee.desktopclient.components.servermanagement.managers.IMServerManager;
import com.ontimize.jee.desktopclient.components.servermanagement.managers.IMSession;
import com.ontimize.jee.desktopclient.components.servermanagement.managers.IMSetupLogLevel;
import com.ontimize.jee.desktopclient.components.servermanagement.managers.IMSqlManager;
import com.ontimize.jee.desktopclient.components.servermanagement.managers.IMThreads;
import com.ontimize.jee.desktopclient.components.sliderbar.SlideBarSection;
import com.ontimize.jee.desktopclient.components.sliderbar.SliderBar;
import com.ontimize.jee.desktopclient.components.sliderbar.SliderBar.SlideBarMode;

public class ServerManagementWindow extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(ServerManagementWindow.class);

    protected JPanel cards;

    protected SliderBar sliderPanel;

    protected static final ArrayList<String> logList = new ArrayList<>(
            Arrays.asList("Live log console", "Live log to file", "Download log files", "Setup log level"));

    protected static final ArrayList<String> dumpList = new ArrayList<>(Arrays.asList("Memory", "Threads"));

    protected static final ArrayList<String> statisticsList = new ArrayList<>(Arrays.asList("Request", "Session"));

    protected static final ArrayList<String> dataBaseList = new ArrayList<>(Arrays.asList("Sql manager"));

    protected static final ArrayList<String> serverList = new ArrayList<>(Arrays.asList("Daos"));

    protected JList<String> logJList;

    protected JList<String> dumpJList;

    protected JList<String> statisticsJList;

    protected JList<String> dataBaseJList;

    protected JList<String> serverJList;

    private final ResourceBundle bundle;

    public ServerManagementWindow() {
        super();
        this.bundle = ResourceBundle.getBundle("ontimize-jee-i18n.bundle");
        this.setTitle(this.bundle.getString("SERVER_MANAGEMENT"));
        this.setIconImage(ApplicationManager.getApplication().getFrame().getIconImage());
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(this.createPanel(), BorderLayout.CENTER);
        this.pack();
    }

    public JPanel createPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        this.sliderPanel = this.getSliderPanel();
        this.cards = this.getCardsPanel();

        mainPanel.add(this.sliderPanel, BorderLayout.WEST);
        mainPanel.add(this.cards, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel getCardsPanel() {
        JPanel cards = new JPanel(new CardLayout());

        // **Log**
        // Create the "Live log console".
        JPanel liveLogConsolePanel = this.createPanel("ontimize-jee-forms/servermanagement/formLiveLogConsole.form",
                new IMLiveLogConsole());
        // Create the "Live log to file".
        JPanel liveLogToFilePanel = this.createPanel("ontimize-jee-forms/servermanagement/formToFile.form",
                new IMLiveLogToFile((Form) liveLogConsolePanel));
        // Create the "Live log console".
        JPanel downloadLogFilesPanel = this.createPanel("ontimize-jee-forms/servermanagement/formDownloadLogFiles.form",
                new IMDownloadLogFiles());
        // Create the "Live log console".
        JPanel setupLogLevelPanel = this.createPanel("ontimize-jee-forms/servermanagement/formSetupLogLevel.form",
                new IMSetupLogLevel());

        // **Dump**
        // Create the "Live log console".
        JPanel memoryPanel = this.createPanel("ontimize-jee-forms/servermanagement/formToFile.form", new IMMemory());
        // Create the "Live log console".
        JPanel threadsPanel = this.createPanel("ontimize-jee-forms/servermanagement/formToFile.form", new IMThreads());

        // **Statistics**
        // Create the "Live log console".
        JPanel requestsPanel = this.createPanel("ontimize-jee-forms/servermanagement/formRequests.form",
                new IMRequest());
        // Create the "Live log console".
        JPanel sessionPanel = this.createPanel("ontimize-jee-forms/servermanagement/formSession.form", new IMSession());

        // **Database**
        // Create the "Sql manager".
        JPanel sqlManagerPanel = this.createPanel("ontimize-jee-forms/servermanagement/formSqlManager.form",
                new IMSqlManager());

        // **Server**
        // Create the "Sql manager".
        JPanel daosPanel = this.createPanel("ontimize-jee-forms/servermanagement/formDaosManager.form",
                new IMServerManager());

        // Create the panel that contains the "cards".
        cards.add(new JPanel(), "EMPTY");

        cards.add(liveLogConsolePanel, "Live log console");// bundle.getString(....)
        cards.add(liveLogToFilePanel, "Live log to file");
        cards.add(downloadLogFilesPanel, "Download log files");
        cards.add(setupLogLevelPanel, "Setup log level");

        cards.add(memoryPanel, "Memory");
        cards.add(threadsPanel, "Threads");

        cards.add(requestsPanel, "Request");
        cards.add(sessionPanel, "Session");

        cards.add(sqlManagerPanel, "Sql manager");

        cards.add(daosPanel, "Daos");
        return cards;
    }

    /**
     * Creates the a new panel.
     * @return the component
     */
    private Form createPanel(String formName, InteractionManager iManager) {
        try {
            Application application = ApplicationManager.getApplication();
            IFormManager formManager = (IFormManager) ((Entry) ((Map) ReflectionTools.getFieldValue(application,
                    "formsManagers")).entrySet().iterator().next()).getValue();

            JPanel container = new JPanel();
            URL url = Thread.currentThread().getContextClassLoader().getResource(formName);
            Form form = formManager.getFormBuilder().buildForm(container, url.toString());
            form.createLists();

            form.setInteractionManager(iManager);
            try {
                form.setResourceBundle(ResourceBundle.getBundle("ontimize-jee-i18n.bundle"));
            } catch (Exception ex) {
                ServerManagementWindow.logger.warn("Ingores app bundle", ex);
            }
            iManager.registerInteractionManager(form, new FormManager(null, new JPanel(), null, "none", null) {

                @Override
                protected void loadFormInEDTh(String arg0) {
                    // do nothing
                }

                @Override
                public synchronized void loadInEDTh() {
                    // do nothing
                }
            });
            return form;
        } catch (Exception error) {
            throw new OntimizeJEERuntimeException(error);
        }
    }

    private SliderBar getSliderPanel() {
        SliderBar sliderBar = new SliderBar(SlideBarMode.TOP_LEVEL, true, 200, true);
        UniqueSlideBarSelectionListener listener = new UniqueSlideBarSelectionListener();

        this.logJList = this.createSlideBarSection(sliderBar, "Log", ServerManagementWindow.logList, listener);
        this.dumpJList = this.createSlideBarSection(sliderBar, "Dump", ServerManagementWindow.dumpList, listener);
        this.statisticsJList = this.createSlideBarSection(sliderBar, "Statistics",
                ServerManagementWindow.statisticsList, listener);
        this.dataBaseJList = this.createSlideBarSection(sliderBar, "Database", ServerManagementWindow.dataBaseList,
                listener);
        this.serverJList = this.createSlideBarSection(sliderBar, "Server management", ServerManagementWindow.serverList,
                listener);

        return sliderBar;
    }

    private JList<String> createSlideBarSection(SliderBar sliderBar, String name, List<String> elements,
            UniqueSlideBarSelectionListener listener) {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String element : elements) {
            model.add(elements.indexOf(element), element);
        }
        JList<String> list = new JList<>();
        list.setModel(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(listener);
        sliderBar.addSection(new SlideBarSection(sliderBar, name, list, null));
        return list;
    }

    public class UniqueSlideBarSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(final ListSelectionEvent e) {
            new Thread(new Runnable() {

                private void checkPreviewSelections(String selectedValue) {
                    // Clear preview selected value
                    this.clearPreviewSelection(selectedValue, ServerManagementWindow.logList,
                            ServerManagementWindow.this.logJList);
                    this.clearPreviewSelection(selectedValue, ServerManagementWindow.dumpList,
                            ServerManagementWindow.this.dumpJList);
                    this.clearPreviewSelection(selectedValue, ServerManagementWindow.dataBaseList,
                            ServerManagementWindow.this.dataBaseJList);
                    this.clearPreviewSelection(selectedValue, ServerManagementWindow.statisticsList,
                            ServerManagementWindow.this.statisticsJList);
                    this.clearPreviewSelection(selectedValue, ServerManagementWindow.serverList,
                            ServerManagementWindow.this.serverJList);
                }

                private void clearPreviewSelection(String selectedValue, ArrayList<String> list, JList<String> jList) {
                    if (!list.contains(selectedValue) && !jList.isSelectionEmpty()) {
                        jList.removeListSelectionListener(UniqueSlideBarSelectionListener.this);
                        jList.clearSelection();
                        jList.addListSelectionListener(UniqueSlideBarSelectionListener.this);
                    }
                }

                @Override
                public void run() {
                    CardLayout cl = (CardLayout) ServerManagementWindow.this.cards.getLayout();
                    String selectedValue = ((JList<String>) e.getSource()).getSelectedValue();
                    this.checkPreviewSelections(selectedValue);
                    cl.show(ServerManagementWindow.this.cards, selectedValue);
                }
            }).start();
        }

    }

}
