package com.ontimize.jee.desktopclient.components.servermanagement.managers;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
import com.ontimize.gui.manager.IFormManager;
import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.services.servermanagement.IServerManagementService;
import com.ontimize.jee.desktopclient.components.servermanagement.window.filechooser.EmbeddedJFileChooser;
import com.ontimize.jee.desktopclient.components.servermanagement.window.filechooser.IEmbeddedJFileChooser;
import com.ontimize.jee.desktopclient.spring.BeansFactory;

/**
 * The Class IMThreads.
 */
public class IMThreads extends BasicInteractionManager {

    private static final Logger logger = LoggerFactory.getLogger(IMThreads.class);

    @FormComponent(attr = "FILECHOOSER")
    protected Row rowFileChooser;

    protected EmbeddedJFileChooser fileChooser;

    protected IServerManagementService serverManagement;

    public IMThreads() {
        super();
    }

    @Override
    public void registerInteractionManager(Form f, IFormManager gf) {
        super.registerInteractionManager(f, gf);
        this.managedForm.setFormTitle("Thread dump");

        this.fileChooser = new EmbeddedJFileChooser(EmbeddedJFileChooser.MODE.BOTH, new ThreadsToFileListener());
        this.fileChooser.setFileFilter(new FileNameExtensionFilter("TXT FILES", "txt"));
        this.rowFileChooser.add(new JScrollPane(this.fileChooser),
                new GridBagConstraints(0, 1, 3, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2), 0, 0));

        this.serverManagement = BeansFactory.getBean(IServerManagementService.class);
    }

    public class ThreadsToFileListener implements IEmbeddedJFileChooser {

        @Override
        public void setPathFileChooser(String path) {
            // Check extension
            if (!"txt".equalsIgnoreCase(FilenameUtils.getExtension(path))) {
                // filename is NO OK as-is
                path = path + ".txt";
            }
            // Save file
            InputStream stream = null;
            try {
                String createThreadDump = IMThreads.this.serverManagement.createThreadDump();
                stream = new ByteArrayInputStream(createThreadDump.getBytes(StandardCharsets.UTF_8));
                IOUtils.copy(stream, Files.newOutputStream(Paths.get(path)));
            } catch (OntimizeJEEException e) {
                IMThreads.logger.trace(null, e);
                MessageDialog.showErrorMessage(SwingUtilities.getWindowAncestor(IMThreads.this.managedForm),
                        e.getMessage());
            } catch (IOException e) {
                IMThreads.logger.trace(null, e);
                MessageDialog.showErrorMessage(SwingUtilities.getWindowAncestor(IMThreads.this.managedForm),
                        e.getMessage());
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        IMThreads.logger.trace(null, e);
                        MessageDialog.showErrorMessage(SwingUtilities.getWindowAncestor(IMThreads.this.managedForm),
                                e.getMessage());
                    }
                }
            }
            File file = new File(path);
            try {
                if (file.exists()) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file);
                    }
                }
            } catch (IOException e) {
                IMThreads.logger.error(null, e);
            }
            IMThreads.this.fileChooser.rescanCurrentDirectory();
        }

    }

}
