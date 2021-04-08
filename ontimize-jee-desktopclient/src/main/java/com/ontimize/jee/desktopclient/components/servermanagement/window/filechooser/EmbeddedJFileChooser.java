package com.ontimize.jee.desktopclient.components.servermanagement.window.filechooser;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.MessageDialog;
import com.ontimize.gui.button.Button;
import com.ontimize.gui.container.Column;
import com.ontimize.gui.container.Row;
import com.ontimize.gui.field.TextDataField;

public class EmbeddedJFileChooser extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedJFileChooser.class);

    protected final TextDataField fileName;

    protected final Button bSave, bOpen;

    protected final CustomJFileChooser fileChooser;

    protected final MODE mode;

    protected final IEmbeddedJFileChooser listener;

    public static enum MODE {

        OPEN, SAVE, BOTH

    }

    public EmbeddedJFileChooser(MODE mode, IEmbeddedJFileChooser listener) {
        super();
        this.mode = mode;
        this.listener = listener;
        this.setLayout(new GridBagLayout());
        this.fileName = this.createFileNameField();
        this.bSave = (Button) this.createSaveButtonComp();
        this.bOpen = (Button) this.createOpenButtonComp();
        this.fileChooser = new CustomJFileChooser();

        this.add(this.fileChooser, new GridBagConstraints(0, 0, 3, 1, 10, 10, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

        switch (this.mode) {
            case OPEN:
                this.add(this.bOpen, new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.EAST,
                        GridBagConstraints.LINE_END, new Insets(2, 2, 2, 2), 0, 0));
                break;
            case SAVE:
                this.add(this.fileName, new GridBagConstraints(0, 1, 3, 1, 0, 0, GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
                this.add(this.bSave, new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.EAST,
                        GridBagConstraints.LINE_END, new Insets(2, 2, 2, 2), 0, 0));
                break;
            default:
                this.add(this.fileName, new GridBagConstraints(0, 1, 3, 1, 0, 0, GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
                this.add(this.createRowButtonsComp(this.bOpen, this.bSave),
                        new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.LINE_END,
                                new Insets(2, 2, 2, 2), 0, 0));
                break;
        }
        this.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // file selected listener to update fileName TextDataField
                if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                    File file = (File) evt.getNewValue();
                    if (file != null) {
                        EmbeddedJFileChooser.this.fileName.setValue(file.getName());
                    }
                }
            }
        });
        this.bOpen.addActionListener(new OpenFileEmbbededListener());
        this.bSave.addActionListener(new SaveFileEmbbededListener());
    }

    public void setFileFilter(FileNameExtensionFilter fileNameExtensionFilter) {
        this.fileChooser.setFileFilter(fileNameExtensionFilter);
    }

    public void rescanCurrentDirectory() {
        this.fileChooser.rescanCurrentDirectory();
    }

    private Component createRowButtonsComp(Button... buttons) {
        Hashtable<Object, Object> parameters = new Hashtable<>();
        parameters.clear();
        Row row = new Row(parameters);
        for (int i = 0; i <= (buttons.length - 1); i++) {
            row.add(buttons[i]);
        }

        parameters.clear();
        parameters.put("expand", "no");
        Column column = new Column(parameters);

        column.add(row);

        parameters.clear();
        parameters.put("expand", "yes");
        Column column2 = new Column(parameters);

        parameters.clear();
        parameters.put("expand", "no");
        Row row2 = new Row(parameters);

        row2.add(column2);
        row2.add(column);

        return row2;
    }

    private Component createSaveButtonComp() {
        Hashtable<Object, Object> parameters = new Hashtable<>();
        parameters.clear();
        parameters.put("align", "left");
        parameters.put("attr", "B_SAVE");
        parameters.put("text", ResourceBundle.getBundle("ontimize-jee-i18n.bundle").getString("B_SAVE"));
        return new Button(parameters);
    }

    private Component createOpenButtonComp() {
        Hashtable<Object, Object> parameters = new Hashtable<>();
        parameters.clear();
        parameters.put("align", "left");
        parameters.put("attr", "B_OPEN");
        parameters.put("text", ResourceBundle.getBundle("ontimize-jee-i18n.bundle").getString("B_OPEN"));
        return new Button(parameters);
    }

    private TextDataField createFileNameField() {
        Hashtable<Object, Object> parameters = new Hashtable<>();
        parameters.put("align", "left");
        parameters.put("attr", "FILE_NAME");
        parameters.put("dim", "text");
        parameters.put("enabled", "yes");
        parameters.put("labelvisible", "yes");
        parameters.put("labelposition", "top");
        parameters.put("text", ResourceBundle.getBundle("ontimize-jee-i18n.bundle").getString("FILE_NAME"));
        return new TextDataField(parameters);
    }

    /******************
     * AUXILIAR CLASSES
     */

    public class OpenFileEmbbededListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    File selectedFile = EmbeddedJFileChooser.this.fileChooser.getSelectedFile();
                    if (selectedFile != null) {
                        try {
                            if (selectedFile.exists()) {
                                if (Desktop.isDesktopSupported()) {
                                    Desktop.getDesktop().open(selectedFile);
                                }
                            }
                        } catch (IOException e) {
                            EmbeddedJFileChooser.logger.error(null, e);
                        }
                    }

                }
            }).start();
        }

    }

    public class SaveFileEmbbededListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    File currentDirectory = EmbeddedJFileChooser.this.fileChooser.getCurrentDirectory();
                    File selectedFile = EmbeddedJFileChooser.this.fileChooser.getSelectedFile();
                    String fileName = (String) EmbeddedJFileChooser.this.fileName.getValue();
                    // Check empty fileName
                    if (fileName == null) {
                        MessageDialog.showErrorMessage(
                                SwingUtilities.getWindowAncestor(EmbeddedJFileChooser.this.fileChooser),
                                "E_EMPTY_FILENAME");
                    } else {
                        // Check Overwrite
                        if (selectedFile != null) {
                            if (selectedFile.getName().equals(fileName)) {
                                boolean showQuestionMessage = MessageDialog
                                    .showQuestionMessage(EmbeddedJFileChooser.this.fileChooser, "Q_OVERWRITE_FILE");
                                if (!showQuestionMessage) {
                                    return;
                                }
                            }
                        }
                        if (EmbeddedJFileChooser.this.listener != null) {
                            EmbeddedJFileChooser.this.listener
                                .setPathFileChooser(currentDirectory.getAbsolutePath() + "\\" + fileName);
                        }
                    }

                }
            }).start();
        }

    }

    public class CustomJFileChooser extends JFileChooser {

        public CustomJFileChooser() {
            super();
            this.setFileSelectionMode(JFileChooser.FILES_ONLY);
            this.setControlButtonsAreShown(false);
            this.setFileHidingEnabled(false);
            this.disableTextComponents(this);
            this.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    // file selected listener to update fileName TextDataField
                    if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                        File file = (File) evt.getNewValue();
                        if (file != null) {
                            EmbeddedJFileChooser.this.fileName.setValue(file.getName());
                        }
                    }
                }
            });
        }

        private void disableTextComponents(Container parent) {
            Component[] c = parent.getComponents();
            for (int j = 0; j < c.length; j++) {
                String unpack = this.unpack(c[j]);
                if ("SynthFileChooserUIImpl$3".equals(unpack) || "SynthFileChooserUIImpl$AlignedLabel".equals(unpack)) {
                    c[j].getParent().setVisible(false);
                }
                if (((Container) c[j]).getComponentCount() > 0) {
                    this.disableTextComponents((Container) c[j]);
                }
            }
        }

        private String unpack(Component c) {
            String s = c.getClass().getName();
            int dot = s.lastIndexOf(".");
            if (dot != -1) {
                s = s.substring(dot + 1);
            }
            return s;
        }

    }

}
