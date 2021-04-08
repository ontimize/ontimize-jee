package com.ontimize.jee.desktopclient.components.servermanagement.managers;

import java.awt.GridBagConstraints;
import java.awt.Insets;
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
import com.ontimize.gui.manager.IFormManager;
import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.services.servermanagement.IServerManagementService;
import com.ontimize.jee.desktopclient.components.servermanagement.window.filechooser.EmbeddedJFileChooser;
import com.ontimize.jee.desktopclient.components.servermanagement.window.filechooser.IEmbeddedJFileChooser;
import com.ontimize.jee.desktopclient.spring.BeansFactory;

/**
 * The Class IMMemory.
 */
public class IMMemory extends BasicInteractionManager {

    private static final Logger logger = LoggerFactory.getLogger(IMMemory.class);

    @FormComponent(attr = "FILECHOOSER")
    protected Row rowFileChooser;

    protected EmbeddedJFileChooser fileChooser;

    protected IServerManagementService serverManagement;

    public IMMemory() {
        super();
    }

    @Override
    public void registerInteractionManager(Form f, IFormManager gf) {
        super.registerInteractionManager(f, gf);
        this.managedForm.setFormTitle("Memory dump");

        this.fileChooser = new EmbeddedJFileChooser(EmbeddedJFileChooser.MODE.BOTH, new MemoryToFileListener());
        this.fileChooser.setFileFilter(new FileNameExtensionFilter("BIN FILES", "bin"));
        this.rowFileChooser.add(new JScrollPane(this.fileChooser),
                new GridBagConstraints(0, 1, 3, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2), 0, 0));

        this.serverManagement = BeansFactory.getBean(IServerManagementService.class);
    }

    public class MemoryToFileListener implements IEmbeddedJFileChooser {

        @Override
        public void setPathFileChooser(String path) {
            // Check extension
            if (!"bin".equalsIgnoreCase(FilenameUtils.getExtension(path))) {
                // filename is NO OK as-is
                path = path + ".bin";
            }
            // Save file
            InputStream heapDumpIs = null;
            try {
                heapDumpIs = IMMemory.this.serverManagement.createHeapDump();
                IOUtils.copy(heapDumpIs, Files.newOutputStream(Paths.get(path)));
            } catch (OntimizeJEEException e) {
                IMMemory.logger.trace(null, e);
                MessageDialog.showErrorMessage(SwingUtilities.getWindowAncestor(IMMemory.this.managedForm),
                        e.getMessage());
            } catch (IOException e) {
                IMMemory.logger.trace(null, e);
                MessageDialog.showErrorMessage(SwingUtilities.getWindowAncestor(IMMemory.this.managedForm),
                        e.getMessage());
            } finally {
                if (heapDumpIs != null) {
                    try {
                        heapDumpIs.close();
                    } catch (IOException e) {
                        IMMemory.logger.trace(null, e);
                        MessageDialog.showErrorMessage(SwingUtilities.getWindowAncestor(IMMemory.this.managedForm),
                                e.getMessage());
                    }
                }
            }
            // It doesn't try to open it
            IMMemory.this.fileChooser.rescanCurrentDirectory();
        }

    }

}
