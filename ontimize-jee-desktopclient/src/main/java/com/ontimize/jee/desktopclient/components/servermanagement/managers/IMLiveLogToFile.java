package com.ontimize.jee.desktopclient.components.servermanagement.managers;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.annotation.FormComponent;
import com.ontimize.gui.BasicInteractionManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.MessageDialog;
import com.ontimize.gui.container.Row;
import com.ontimize.gui.field.MemoDataField;
import com.ontimize.gui.manager.IFormManager;
import com.ontimize.jee.desktopclient.components.servermanagement.window.filechooser.EmbeddedJFileChooser;
import com.ontimize.jee.desktopclient.components.servermanagement.window.filechooser.IEmbeddedJFileChooser;

/**
 * The Class IMLiveLogConsole.
 */
public class IMLiveLogToFile extends BasicInteractionManager {

    private static final Logger logger = LoggerFactory.getLogger(IMLiveLogToFile.class);

    @FormComponent(attr = "FILECHOOSER")
    protected Row rowFileChooser;

    protected EmbeddedJFileChooser fileChooser;

    protected final Form liveLogConsole;

    public IMLiveLogToFile(Form liveLogConsole) {
        super();
        this.liveLogConsole = liveLogConsole;
    }

    @Override
    public void registerInteractionManager(Form f, IFormManager gf) {
        super.registerInteractionManager(f, gf);
        this.managedForm.setFormTitle("Live log to file");

        this.fileChooser = new EmbeddedJFileChooser(EmbeddedJFileChooser.MODE.BOTH, new LiveLogToFileListener());
        this.fileChooser.setFileFilter(new FileNameExtensionFilter("LOG FILES", "log"));
        this.rowFileChooser.add(new JScrollPane(this.fileChooser),
                new GridBagConstraints(0, 1, 3, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2), 0, 0));
    }

    public class LiveLogToFileListener implements IEmbeddedJFileChooser {

        @Override
        public void setPathFileChooser(final String path) {
            new Thread(new Runnable() {

                String finalPath;

                @Override
                public void run() {
                    this.finalPath = path;
                    // Check extension
                    if (!"log".equalsIgnoreCase(FilenameUtils.getExtension(path))) {
                        // filename is NO OK as-is
                        this.finalPath = this.finalPath + ".log";
                    }

                    // Save file
                    MemoDataField console = (MemoDataField) IMLiveLogToFile.this.liveLogConsole
                        .getElementReference("CONSOLE");
                    String text = (String) console.getValue();
                    if (text != null) {
                        InputStream stream = null;
                        try {
                            stream = new ByteArrayInputStream(text.getBytes());
                            IOUtils.copy(stream, Files.newOutputStream(Paths.get(this.finalPath)));
                        } catch (IOException e) {
                            IMLiveLogToFile.logger.trace(null, e);
                            MessageDialog.showErrorMessage(
                                    SwingUtilities.getWindowAncestor(IMLiveLogToFile.this.managedForm), e.getMessage());
                        } finally {
                            if (stream != null) {
                                try {
                                    stream.close();
                                } catch (IOException e) {
                                    IMLiveLogToFile.logger.trace(null, e);
                                    MessageDialog.showErrorMessage(
                                            SwingUtilities.getWindowAncestor(IMLiveLogToFile.this.managedForm),
                                            e.getMessage());
                                }
                            }
                        }
                        File file = new File(this.finalPath);
                        try {
                            if (file.exists()) {
                                if (Desktop.isDesktopSupported()) {
                                    Desktop.getDesktop().open(file);
                                }
                            }
                        } catch (IOException e) {
                            IMLiveLogToFile.logger.error(null, e);
                        }
                        IMLiveLogToFile.this.fileChooser.rescanCurrentDirectory();
                    }
                }
            }).start();
        }

    }

}
