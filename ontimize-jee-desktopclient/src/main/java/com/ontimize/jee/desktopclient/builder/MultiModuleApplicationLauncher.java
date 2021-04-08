package com.ontimize.jee.desktopclient.builder;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.FocusManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.builder.xml.XMLApplicationBuilder;
import com.ontimize.gui.Application;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.FixedFocusManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.MessageDialog;
import com.ontimize.gui.table.Table;
import com.ontimize.gui.table.TableConfigurationManager;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.desktopclient.spring.BeansFactory;
import com.ontimize.report.ReportManager;
import com.ontimize.xml.DefaultXMLParametersManager;

/**
 * The Class MultiModuleApplicationLauncher.
 */
public class MultiModuleApplicationLauncher {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(MultiModuleApplicationLauncher.class);

    public static String START_LOOK_AND_FEEL = UIManager.getSystemLookAndFeelClassName();

    /**
     * Instantiates a new multi module application launcher.
     */
    public MultiModuleApplicationLauncher() {
        super();
    }

    /**
     * Launch.
     * @param labelsPath the labels path
     * @param clientApplicationPath the client application path
     * @param springConfigurationFiles the spring configuration files
     * @param args the args
     * @throws OntimizeJEERuntimeException the ontimize jee exception
     */
    public void launch(final String labelsPath, final String clientApplicationPath,
            final String[] springConfigurationFiles, final String[] args) {

        URL urlLabelsFile = Thread.currentThread().getContextClassLoader().getResource(labelsPath);
        if (urlLabelsFile == null) {
            throw new OntimizeJEERuntimeException("'" + labelsPath + "' file cannot be found");
        }
        if (clientApplicationPath == null) {
            throw new OntimizeJEERuntimeException("clientApplicationPath cannot be empty");
        }

        Thread th = new Thread("Multimodule application launcher thread") {

            @Override
            public void run() {
                MultiModuleApplicationLauncher.this.doInCreationThread(labelsPath, clientApplicationPath,
                        springConfigurationFiles, args);
            }
        };
        th.start();
    }

    /**
     * Task to do in creation thread.
     * @param labelsPath the labels path
     * @param clientApplicationPath the client application path
     * @param springConfigurationFiles the spring configuration files
     * @param args the args
     */
    protected void doInCreationThread(final String labelsPath, final String clientApplicationPath,
            final String[] springConfigurationFiles, String[] args) {
        try {
            BeansFactory.init(springConfigurationFiles);
            this.checkLibraries();
            if (!ApplicationManager.jvmVersionHigherThan_1_4_0()) {
                MultiModuleApplicationLauncher.logger.info("FixedFocusManager established");
                FocusManager.setCurrentManager(new FixedFocusManager());
            }
            XMLMultiModuleApplicationBuilder applicationBuilder = new XMLMultiModuleApplicationBuilder(
                    Thread.currentThread().getContextClassLoader().getResource(labelsPath).toString());
            XMLApplicationBuilder.setXMLApplicationBuilder(applicationBuilder);

            final Application application = applicationBuilder.buildApplication(clientApplicationPath);
            application.login();
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    application.show();
                }
            });
        } catch (Exception e) {
            MultiModuleApplicationLauncher.logger.error("ERROR: Application cannot be started", e);
        }
    }

    /**
     * Launches the client application. <br/>
     * <code>Syntax: ApplicationLauncher 'xmlLabelFile' 'xmlApplicationFile' ['package'] [-d(for debug)] [-https/http (for https/http
     * tunneling)] [-nathttp/nathttps (for https/http tunneling using NAT socket factory)] [-ssl -sslnoreconnect]</code>
     * <br/>
     * The parameters configuration is:<br/>
     * <p>
     * <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX>
     * <tr>
     * <td><b>param</b></td>
     * <td><b>meaning</b></td>
     * <td><b>status</b></td>
     * <td><b>example</b></td>
     * </tr>
     * <tr>
     * <td>xmlLabelFile</td>
     * <td>the path to the file that contains the label information, which is used to interpret the xml
     * tags</td>
     * <td>mandatory; a default file exist</td>
     * <td>com/ontimize/gui/labels.xml, which is the default path with the labels of the default
     * Ontimize fields and components</td>
     * </tr>
     * <tr>
     * <td>xmlApplicationFile</td>
     * <td>the path to the file that describes the application</td>
     * <td>mandatory</td>
     * <td>com/project/client/clientapplication.xml</td>
     * </tr>
     * <tr>
     * <td>package</td>
     * <td></td>
     * <td>optional</td>
     * <td></td>
     * </tr>
     * <tr>
     * <td></td>
     * <td></td>
     * <td></td>
     * <td></td>
     * </tr>
     * </Table>
     * @param args the arguments
     */
    public void launch(String[] args) {
        args = MultiModuleApplicationLauncher.configureSystemProperties(args);

        if ((args == null) || (args.length < 2)) {
            MultiModuleApplicationLauncher.logger.debug(
                    "Syntax: ApplicationLauncher 'xmlLabelFile' 'xmlApplicationFile' ['springConfigurationFile'] [-d(for debug)] [-conf conffiles]. \nThe first and the second parameters must include the complete path relative to the classpath");
            System.exit(-1);
        }

        String lf = System.getProperty("com.ontimize.gui.lafclassname");
        if (lf == null) {
            lf = MultiModuleApplicationLauncher.START_LOOK_AND_FEEL;
        }

        try {
            UIManager.setLookAndFeel(lf);
        } catch (Exception e) {
            MultiModuleApplicationLauncher.logger.error(null, e);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                MultiModuleApplicationLauncher.logger.trace(null, ex);
            }
        }

        String sLabelsFile = args[0];
        String sXMLFile = args[1];
        String[] springConfigurationFiles = null;
        if ((args.length > 2) && (args[2].charAt(0) != '-')) {
            springConfigurationFiles = args[2].split(";");
        }

        for (int i = 0; i < args.length; i++) {
            // debug option
            if ((args[i] != null) && "-d".equalsIgnoreCase(args[i])) {
                Form.RELOAD_BUTTON_VISIBLE = true;
                ApplicationManager.DEBUG = true;
                ApplicationManager.setApplicationManagerWindowVisible(true);
                break;
            }
            // configurationfiles option
            if ((i < (args.length - 1)) && (args[i] != null) && "-conf".equalsIgnoreCase(args[i])
                    && (args[i + 1] != null)) {
                DefaultXMLParametersManager.setXMLDefaultParameterFile(args[i + 1]);
                break;
            }
        }

        try {
            this.launch(sLabelsFile, sXMLFile, springConfigurationFiles, args);
        } catch (Exception e) {
            MultiModuleApplicationLauncher.logger.error(null, e);
            MessageDialog.showErrorMessage(null, e.getMessage());
        }
    }

    /**
     * Check libraries.
     */
    protected void checkLibraries() {
        new Thread("Check Libraries") {

            @Override
            public void run() {
                super.run();
                ReportManager.isReportsEnabled();

                try {
                    if (Table.rendererEditorConfigurationFile != null) {
                        TableConfigurationManager.getTableConfigurationManager(Table.rendererEditorConfigurationFile,
                                true);
                    }
                } catch (Exception e) {
                    MultiModuleApplicationLauncher.logger.error(null, e);
                }

            }
        }.start();
    }

    /**
     * Ensures to define as System properties all parameters received as arguments. From Java 7 update
     * 45 there is not available to define it in JNLP file, was ignored. When receives a "ignore"
     * parameter will be ignored.
     *
     * Common use is as soon as posibble in launcher code this: "args =
     * ClientLaucherUtils.configureSystemProperties(args);"
     * @param args the args
     * @return the string[]
     */
    public static String[] configureSystemProperties(String[] args) {
        List<String> arguments = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String value = args[i];
            if (value != null) {
                value = value.trim();
                if (value.startsWith("-D") && (value.length() > 2) && (value.indexOf('=') > 0)) {
                    String inputP = value.substring(2);
                    StringTokenizer token = new StringTokenizer(inputP, "=");
                    if (token.hasMoreTokens()) {
                        String propertyKey = token.nextToken();
                        if (token.hasMoreTokens()) {
                            String propertyValue = token.nextToken();
                            System.setProperty(propertyKey, propertyValue);
                        }
                    }
                } else {
                    if (!"ignore".equals(value)) {
                        arguments.add(value);
                    }
                }
            }
        }
        return arguments.toArray(new String[arguments.size()]);
    }

}
