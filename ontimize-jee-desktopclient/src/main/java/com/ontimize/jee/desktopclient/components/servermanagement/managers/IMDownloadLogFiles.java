package com.ontimize.jee.desktopclient.components.servermanagement.managers;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JFileChooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import com.ontimize.annotation.FormComponent;
import com.ontimize.db.EntityResult;
import com.ontimize.gui.BasicInteractionManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.MessageDialog;
import com.ontimize.gui.button.Button;
import com.ontimize.gui.manager.IFormManager;
import com.ontimize.gui.table.Table;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.services.servermanagement.IServerManagementService;
import com.ontimize.jee.desktopclient.spring.BeansFactory;

/**
 * The Class IMLiveLogConsole.
 */
public class IMDownloadLogFiles extends BasicInteractionManager {

    private static final Logger logger = LoggerFactory.getLogger(IMDownloadLogFiles.class);

    @FormComponent(attr = "B_REFRESH")
    protected Button bRefresh;

    @FormComponent(attr = "B_DOWNLOAD")
    protected Button bDownload;

    @FormComponent(attr = "LOGFILES")
    protected Table tLogFiles;

    private IServerManagementService serverManagement;

    private JFileChooser chooser;

    public IMDownloadLogFiles() {
        super();
    }

    @Override
    public void registerInteractionManager(Form f, IFormManager gf) {
        super.registerInteractionManager(f, gf);
        this.managedForm.setFormTitle("Download log files");
        this.bRefresh.addActionListener(new RefreshDownloadLogFilesListener());
        this.bDownload.addActionListener(new DownloadLogFileListener());
        this.serverManagement = BeansFactory.getBean(IServerManagementService.class);
    }

    @Override
    public void setQueryInsertMode() {
        super.setQueryInsertMode();
        this.managedForm.enableButtons();
        this.managedForm.enableDataFields();
    }

    private File getTargetFile() {
        if (this.chooser == null) {
            this.chooser = new JFileChooser();
        }
        this.chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = this.chooser.showSaveDialog(this.bRefresh);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return this.chooser.getSelectedFile();
        }
        return null;
    }

    protected EntityResult getLogFiles() throws Exception {
        this.ensureServerManagement();
        return this.serverManagement.getLogFiles();
    }

    protected InputStream getLogger(String fileName) throws Exception {
        this.ensureServerManagement();
        return this.serverManagement.getLogFileContent(fileName);
    }

    protected void ensureServerManagement() {
        if (this.serverManagement == null) {
            throw new OntimizeJEERuntimeException("No server management reference");
        }
    }

    public class RefreshDownloadLogFilesListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        EntityResult res = IMDownloadLogFiles.this.getLogFiles();
                        IMDownloadLogFiles.this.tLogFiles.setValue(res);
                    } catch (Exception ex) {
                        IMDownloadLogFiles.logger.trace(null, ex);
                        MessageDialog.showErrorMessage(IMDownloadLogFiles.this.managedForm.getJDialog(),
                                ex.getMessage());
                    }
                }
            }).start();
        }

    }

    public class DownloadLogFileListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (IMDownloadLogFiles.this.tLogFiles.getSelectedRowsNumber() != 1) {
                            throw new Exception("Debes seleccionar una fila");
                        }
                        File folder = IMDownloadLogFiles.this.getTargetFile();
                        if (folder == null) {
                            return;
                        }
                        String fileName = ((List<String>) IMDownloadLogFiles.this.tLogFiles.getSelectedRowData()
                            .get("FILE_NAME")).get(0);
                        InputStream is = IMDownloadLogFiles.this.getLogger(fileName);
                        ZipInputStream zip = new ZipInputStream(is);
                        ZipEntry nextEntry = zip.getNextEntry();
                        File file = new File(folder, nextEntry.getName());
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            StreamUtils.copy(zip, fos);
                        }
                        Desktop.getDesktop().open(file);
                    } catch (Exception ex) {
                        IMDownloadLogFiles.logger.error(null, ex);
                        MessageDialog.showErrorMessage(IMDownloadLogFiles.this.managedForm.getJDialog(),
                                ex.getMessage());
                    }
                }
            }).start();
        }

    }

}
