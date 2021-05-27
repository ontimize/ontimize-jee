package com.ontimize.jee.webclient.export;

import java.io.File;
import java.util.Calendar;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * The Class ExportUtils.
 *
 * @author <a href="daniel.grana@imatia.com">Daniel Grana</a>
 */
public class ExportUtils {

    private static final ExportUtils instance = new ExportUtils();

    private static final ObjectProperty<File> lastKnownDirectoryProperty = new SimpleObjectProperty<>();

    /**
     * Obtiene una instancia unica de ExportUtils.
     * @return instancia unica de ExportUtils
     */
    public static ExportUtils getInstance() {
        return instance;
    }

    /**
     * Export excel.
     * @param ownerWindow owner window
     * @param table table
     * @param fileName file name
     * @param exportType export type
     * @param options options
     */
    // @SuppressWarnings("rawtypes")
    // public final void exportExcel(final Window ownerWindow, final ITXAbstractTableView table, final
    // String fileName,
    // final ExportCallableTask<ExportResult> callable,
    // final ExportExecutionStateControl<ExportResult> executionStateControl,
    // final ExportType exportType, final ExportOptions options) {
    //
    // final ExtensionFilter[] extensionFilters = ExtensionFilterUtils
    // .getExtensionFilter(ExtensionFilterTypes.EXCEL_FILES);
    // this.export(ownerWindow, table, fileName, ExtensionFilterUtils.XLSX_FILE_EXTENSION,
    // extensionFilters, callable,
    // executionStateControl, exportType,
    // options);
    // }
    //
    // @SuppressWarnings("rawtypes")
    // public final void exportPdf(final Window ownerWindow, final ITXAbstractTableView table, final
    // String fileName,
    // final ExportCallableTask<ExportResult, ITXAbstractTableView> callable,
    // final ExportExecutionStateControl<ExportResult> executionStateControl,
    // final ExportType exportType, final ExportOptions options) {
    //
    // final ExtensionFilter[] extensionFilters = ExtensionFilterUtils
    // .getExtensionFilter(ExtensionFilterTypes.PDF_FILES);
    // this.export(ownerWindow, table, fileName, ExtensionFilterUtils.PDF_FILE_EXTENSION,
    // extensionFilters,
    // callable, executionStateControl, exportType, options);
    // }

    /**
     * Export.
     * @param ownerWindow owner window
     * @param table table
     * @param fileName file name
     * @param fileExtension file extension
     * @param extensionFilters extension filters
     * @param exportType export type
     * @param options options
     */
    // @SuppressWarnings("rawtypes")
    // public final void export(final Window ownerWindow, final ITXAbstractTableView table, final String
    // fileName,
    // final String fileExtension, final ExtensionFilter[] extensionFilters,
    // final ExportCallableTask<ExportResult, ITXAbstractTableView> callableTask,
    // final ExportExecutionStateControl<ExportResult> executionStateControl,
    // final ExportType exportType, final ExportOptions options) {
    //
    // File exportFile = null;
    //
    // if (ExportType.ONLY_EMAIL.equals(exportType)) {
    // // TODO
    // } else {
    // exportFile = this.saveFileWindow(ownerWindow, fileName, fileExtension, extensionFilters,
    // options.isIncludeTime());
    // }
    //
    // if (exportFile == null) {
    // return;
    // }
    //
    // callableTask.setTable(table);
    // callableTask.setExportFile(exportFile);
    // callableTask.setExportType(exportType);
    // callableTask.setExportOptions(options);
    // executionStateControl.setExportFile(exportFile);
    // executionStateControl.setExportOptions(options);
    // final WorkerConfigurer<ExportResult> workerConfigurer = null;
    //
    // final WorkerListener notificationLocker = new NotificationWidgetLocker();
    //
    // ServiceExecutor serviceExecutor = ServiceExecutorContext.getServiceExecutor();
    // if (serviceExecutor == null) {
    // serviceExecutor = new SimpleServiceExecutor();
    // }
    // serviceExecutor.execute(callableTask, workerConfigurer, executionStateControl,
    // notificationLocker);
    //
    // }
    //
    /**
     * * Utility methods * *
     */

    /**
     * Save file window.
     * @param ownerWindow owner window
     * @param name name
     * @param extension extension
     * @param extensionFilters extension filters
     * @param includeTime include time
     * @return the file
     */
    @SuppressWarnings("static-method")
    public File saveFileWindow(final Window ownerWindow, final String name, final String extension,
            final ExtensionFilter[] extensionFilters, final boolean includeTime) {
        final FileChooser fileChooser = new FileChooser();

        fileChooser.initialDirectoryProperty().bindBidirectional(lastKnownDirectoryProperty);
        fileChooser.getExtensionFilters().addAll(extensionFilters);

        final String[] tmpFileNameParts = name.split(extension);
        final Calendar calendar = Calendar.getInstance();
        final StringBuilder sbuilder = new StringBuilder();
        sbuilder.append(tmpFileNameParts.length > 0 ? tmpFileNameParts[0] : name)
            .append("_")
            .append(DateFormatUtils.format(calendar.getTime(), DateFormatUtils.ISO_DATE_FORMAT.getPattern()));
        if (includeTime) {
            sbuilder.append("'T'").append(DateFormatUtils.format(calendar.getTime(), "HH_mm_ss"));
        }
        sbuilder.append(extension);

        fileChooser.setInitialFileName(sbuilder.toString());

        final File selectedFile = fileChooser.showSaveDialog(ownerWindow);
        if (selectedFile != null) {
            lastKnownDirectoryProperty.setValue(selectedFile.getParentFile());
        }
        return selectedFile;// existsFile(ownerWindow, selectedFile);
    }

    @SuppressWarnings("static-method")
    private File existsFile(final Window owner, final File file) {

        if (file != null) {
            if (file.exists()) {

                // final Alert msgBox = AlertBuilder.create()
                // .appendAlertType(AlertType.WARNING)
                // .appendOwner(owner)
                // .appendContentText(ResourceBundleUtils.getI18nText(I18nNaming.AVISO_EXPORTAR_REEMPLAZO))
                // .appendModality(Modality.APPLICATION_MODAL)
                // .build();
                //
                // final Optional<ButtonType> showAndWait = msgBox.showAndWait();
                // if (showAndWait.isPresent() && (showAndWait.get() == ButtonType.OK)) {
                // return file;
                // }
                System.out.println("Mensaje provisional: el fichero ya existe!");

            } else {
                return file;
            }
        }

        return null;
    }

}
