package com.ontimize.jee.webclient.export.base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.webclient.export.exception.ExportException;
import com.ontimize.jee.webclient.export.providers.ExportDataProvider;
import com.ontimize.jee.webclient.export.util.ApplicationContextUtils;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.ontimize.jee.common.db.AdvancedEntityResult;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.db.SQLStatementBuilder.SQLOrder;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.server.rest.QueryParameter;
import com.ontimize.jee.webclient.export.CellStyleContext;
import com.ontimize.jee.webclient.export.ExportColumn;
import com.ontimize.jee.webclient.export.ExportColumnStyle;
import com.ontimize.jee.webclient.export.HeadExportColumn;
import com.ontimize.jee.webclient.export.ExportColumnStyle.CellColor;
import com.ontimize.jee.webclient.export.ExportColumnStyle.VerticalAlignment;
import com.ontimize.jee.webclient.export.ExportColumnStyle.HorizontalAlignment;
import com.ontimize.jee.webclient.export.support.DefaultExportColumnStyle;
import com.ontimize.jee.webclient.export.support.exporter.DefaultXSSFExcelExporter;
import com.ontimize.jee.webclient.export.support.sheetnameprovider.DefaultSheetNameProvider;
import com.ontimize.jee.webclient.export.support.sheetnameprovider.PaginatedSheetNameProvider;
import com.ontimize.jee.webclient.export.support.styleprovider.DefaultExcelExportStyleProvider;
import com.ontimize.jee.webclient.export.providers.ExcelExportDataProvider;
import com.ontimize.jee.webclient.export.providers.ExportColumnProvider;
import com.ontimize.jee.webclient.export.providers.ExportStyleProvider;
import com.ontimize.jee.webclient.export.providers.SheetNameProvider;
import com.ontimize.jee.webclient.export.rule.CellSelectionRule;
import com.ontimize.jee.webclient.export.rule.CellSelectionRuleFactory;
import com.ontimize.jee.webclient.export.rule.RowSelectionRule;
import com.ontimize.jee.webclient.export.rule.RowSelectionRuleFactory;
import com.ontimize.jee.webclient.export.support.BaseExportColumnProvider;
import com.ontimize.jee.webclient.export.support.DefaultHeadExportColumn;
import com.ontimize.jee.webclient.export.util.ExportOptions;

/**
 * Servicio de exportación en formato Excel. El formato de la entityResult es el siguiente:
 * <ul>
 * <li>data: lista de mapas de datos, uno por cada registro, con un par clave->valor por cada
 * campo</li>
 * <li>columns: mapa de pares columnId-> contenido. Las columnas pueden anidarse</li>
 * <li>columnTitles: mapa de pares columnId->título</li>
 * <li>columnTypes: mapa de pares columnId->clase entre comillas, como
 * <code>"java.lang.String"</code></li>
 * <li>styles: mapa de pares nombre_de_estilo->mapa de pares nombre_de_formato->valor</li>
 * <li>columnStyles: mapa de pares columnId->nombre_de_estilo o bien lista de nombres_de_estilo</li>
 * <li>rowStyles: mapa de pares regla_de_selección_de_fila->nombre_de_estilo o bien lista de
 * nombres_de_estilo</li>
 * <li>cellStyles: mapa de pares coordenadas_de_celda->nombre_de_estilo o bien lista de
 * nombres_de_estilo</li>
 * </ul>
 *
 * @author <a href="antonio.vazquez@imatia.com">Antonio Vazquez Araujo</a>
 */
@Service("ExcelExportService")
public class ExcelExportService extends BaseExportService implements IExcelExportService, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(ExcelExportService.class);

    private ApplicationContext appContext;
    
    private ApplicationContextUtils applicationContextUtils;

    /**
     * Method that generates the data provider
     * @param service The query service.
     * @param queryParameters Columns and sqltypes.
     * @param dao The query dao.
     * @param keysValues The keys values.
     * @param attributesValues The attributes values.
     * @param bodyColumns Columns list.
     * @param pageSize Size of the page.
     * @param orderBy Table field by which data is sorted.
     * @param advancedQuery Boolean that specifies if it is a normal or advanced query.
     * @param page Page number to export, -1 if you don't want to specify it.
     * @return Returns the dataProvider.
     */
    private static ExcelExportDataProvider generateDataProvider(Object service, QueryParameter queryParameters,
            String dao, Map<Object, Object> keysValues, List<Object> attributesValues, List<ExportColumn> bodyColumns,
            int pageSize, List<SQLOrder> orderBy, boolean advancedQuery, int page) {
        return new ExcelExportDataProvider() {

            AdvancedEntityResult advEr;

            EntityResult eR;

            @Override
            public String getService() {
                return String.valueOf(service);
            }

            @Override
            public String getDao() {
                return dao;
            }

            @Override
            public QueryParameter getQueryParameters() {
                return queryParameters;
            }

            @Override
            public int getNumberOfRows() {
                if (advancedQuery) {
                    if (page == -1) {
                        return getAdvancedEntityResult().getTotalRecordCount();
                    } else {
                        return getUniquePage(page).calculateRecordNumber();
                    }
                } else {
                    return getEntityResult().calculateRecordNumber();
                }
            }

            @Override
            public int getNumberOfColumns() {
                return bodyColumns.size();
            }

            @Override
            public int getColumnIndex(final HeadExportColumn column) {
                return bodyColumns.indexOf(column);
            }

            @Override
            public Object getCellValue(int row, int column) {
                // devuelve los valores de la query de uno en uno
                if (column >= 0 && column < bodyColumns.size()) {
                    if (advancedQuery) {
                        if (page == -1) {
                            // Query avanzada que devuelve todas las paginas
                            AdvancedEntityResult data = getAdvancedEntityResult(row);
                            List cellValue = (List) data.get(String.valueOf(bodyColumns.get(column).getId()));
                            // Cuando el numero de fila es igual al tama�o de pagina, cambia el offset a la
                            // siguiente pagina
                            int nRow = row % pageSize;
                            if (data != null && nRow < cellValue.size()) {
                                return cellValue.get(nRow);
                            }
                        } else {
                            // Query avanzada que devuelve una pagina especifica
                            AdvancedEntityResult data = getUniquePage(page);
                            List cellValue = (List) data.get(String.valueOf(bodyColumns.get(column).getId()));
                            if (data != null && row < cellValue.size()) {
                                return cellValue.get(row);
                            }
                        }

                    } else {
                        // Query simple que devuelve todos los datos
                        EntityResult data = getEntityResult();
                        List cellValue = (List) data.get(String.valueOf(bodyColumns.get(column).getId()));
                        if (data != null && row < cellValue.size()) {
                            return cellValue.get(row);
                        }
                    }

                }
                return null;
            }

            public EntityResult getEntityResult() {
                if (eR == null) {
                    eR = doQuery();
                }

                return eR;
            }

            public AdvancedEntityResult getAdvancedEntityResult() {
                if (advEr == null) {
                    advEr = doAdvancedQuery(0);
                }
                return advEr;
            }

            public AdvancedEntityResult getAdvancedEntityResult(int row) {
                if (advEr == null) {
                    advEr = doAdvancedQuery(0);
                    return advEr;
                }

                if (row >= advEr.getStartRecordIndex() + pageSize) {
                    advEr = doAdvancedQuery(advEr.getStartRecordIndex() + pageSize);
                }

                return advEr;
            }

            public AdvancedEntityResult getUniquePage(int page) {
                if (advEr == null) {
                    advEr = doAdvancedQuery(page);
                    return advEr;
                }
                return advEr;
            }

            public EntityResult doQuery() {
                return (EntityResult) ReflectionTools.invoke(service, dao, keysValues, attributesValues);
            }

            public AdvancedEntityResult doAdvancedQuery(int offSet) {
                return (AdvancedEntityResult) ReflectionTools.invoke(service, dao, keysValues, attributesValues,
                        pageSize, offSet, orderBy);
            }

        };

    }

    /**
     * Creamos un columnProvider que extrae las columnas de la cabecera por una parte y las columnas de
     * datos por otra. Para ello usa el algoritmo recursivo addParentColumn, que agrega a la lista una
     * columna y todas sus hijas en profundidad.
     */
    private static ExportColumnProvider createColumnProvider(EntityResult entityResult) {
        List<HeadExportColumn> headerColumns = new ArrayList<>();
        List<ExportColumn> bodyColumns = new ArrayList<>();
        Map<String, Object> columns = ((Map<String, Object>) (entityResult.get("excelColumns")));
        Map<String, String> columnTitles = (Map<String, String>) entityResult.get("columnTitles");
        addChildrenColumns(bodyColumns, headerColumns, columns, columnTitles);
        ExportColumnProvider ret = new BaseExportColumnProvider(headerColumns, bodyColumns);
        return ret;
    }

    static void addParentColumn(List<ExportColumn> bodyColumns, List<HeadExportColumn> columns, String id, Object value,
            Map<String, String> columnTitles) {

        HeadExportColumn column = new DefaultHeadExportColumn(id, columnTitles.get(id));
        Map<String, Object> children = (Map<String, Object>) value;
        // Si la columna no tiene hijos, la agregamos directamente
        if (value == null || ((Map<String, Object>) value).size() == 0) {
            bodyColumns.add(new ExportColumn(id, columnTitles.get(id), 0, null));
        } else {
            // Si la columna tiene hijos, le agregamos todos sus hijos
            column.getColumns().addAll(addChildrenColumns(bodyColumns, new ArrayList(), children, columnTitles));
        }
        columns.add(column);
    }

    static List<HeadExportColumn> addChildrenColumns(List<ExportColumn> bodyColumns, List<HeadExportColumn> columns,
            Map<String, Object> children, Map<String, String> columnTitles) {
        children.entrySet().forEach(entry -> {
            addParentColumn(bodyColumns, columns, entry.getKey(), entry.getValue(), columnTitles);
        });
        return columns;
    }

    private static ExportStyleProvider<XSSFCellStyle, DataFormat> createStyleProvider(EntityResult entityResult) {

        return new DefaultExcelExportStyleProvider<XSSFCellStyle>() {
            Map<String, ExportColumnStyle> styles = createStyles(entityResult);

            Map<String, List<String>> columnStyles = createColumnStyles(entityResult);

            Map<RowSelectionRule, List<String>> rowStyles = createRowStyles(entityResult);

            Map<CellSelectionRule, List<String>> cellStyles = createCellStyles(entityResult);

            Map<String, String> columnTypes = (Map<String, String>) entityResult.get("columnTypes");

            Map<String, XSSFCellStyle> poiCellStyles = null;

            @Override
            public XSSFCellStyle getHeaderCellStyle(CellStyleContext<XSSFCellStyle, DataFormat> context) {
                if (poiCellStyles == null) {
                    poiCellStyles = new HashMap<>();
                    styles.forEach((name, s) -> {
                        XSSFCellStyle style = context.getCellStyleCreator().get();
                        applyExportStyleToPoiStyle(context, s, style);
                        poiCellStyles.put(name, style);
                    });
                }
                List<String> finalCombinedStyleNames = new ArrayList<>();
                for (Entry<String, List<String>> entry : columnStyles.entrySet()) {
                    if (entry.getKey().equals(context.getColumnId())) {
                        List<String> stylesForColumn = entry.getValue();
                        String combinedStyleName = stylesForColumn.stream().collect(Collectors.joining("+"));
                        // Si no existe el estilo combinado como estilo poi lo agregamos ahora
                        if (!poiCellStyles.containsKey(combinedStyleName)) {
                            XSSFCellStyle combined = context.getCellStyleCreator().get();
                            for (String style : stylesForColumn) {
                                applyExportStyleToPoiStyle(context, styles.get(style), combined);
                            }
                            poiCellStyles.put(combinedStyleName, combined);
                        }
                        finalCombinedStyleNames.add(combinedStyleName);
                    }
                }
                if (!finalCombinedStyleNames.isEmpty()) {
                    String combinedStyleName = finalCombinedStyleNames.stream().collect(Collectors.joining("+"));
                    if (!poiCellStyles.containsKey(combinedStyleName)) {
                        for (String styleName : finalCombinedStyleNames) {
                            XSSFCellStyle combined = context.getCellStyleCreator().get();
                            for (String style : finalCombinedStyleNames) {
                                applyExportStyleToPoiStyle(context, styles.get(style), combined);
                            }
                            poiCellStyles.put(combinedStyleName, combined);
                        }
                    }
                    return poiCellStyles.get(combinedStyleName);
                }
                return null;
            }

            @Override
            public ExportColumnStyle getColumnStyle(String columnId) {
                List<String> stylesOfThisColumn = columnStyles.get(columnId);
                if (stylesOfThisColumn != null) {
                    DefaultExportColumnStyle ret = new DefaultExportColumnStyle();
                    stylesOfThisColumn.forEach(t -> ret.set(styles.get(t)));
                    return ret;
                }
                return super.getColumnStyle(columnId);
            }

            @Override
            public XSSFCellStyle getCellStyle(CellStyleContext<XSSFCellStyle, DataFormat> context) {
                List<String> finalCombinedStyleNames = new ArrayList<>();
                // La primera vez, creamos todos los estilos poi usados y los guardamos
                if (poiCellStyles == null) {
                    poiCellStyles = new HashMap<>();
                    styles.forEach((name, s) -> {
                        XSSFCellStyle style = context.getCellStyleCreator().get();
                        applyExportStyleToPoiStyle(context, s, style);
                        poiCellStyles.put(name, style);
                    });
                }

                // Si existe un grupo de estilos para esa fila, primero los agregamos a
                // poiCellStyles combinados en uno solo con el nombre a+b+c. Luego lo usamos
                for (Entry<RowSelectionRule, List<String>> entry : rowStyles.entrySet()) {
                    if (entry.getKey().match(context.getRow())) {
                        List<String> stylesForRow = entry.getValue();
                        String combinedStyleName = stylesForRow.stream().collect(Collectors.joining("+"));
                        // Si no existe el estilo combinado como estilo poi lo agregamos ahora
                        if (!poiCellStyles.containsKey(combinedStyleName)) {
                            XSSFCellStyle combined = context.getCellStyleCreator().get();
                            for (String style : stylesForRow) {
                                applyExportStyleToPoiStyle(context, styles.get(style), combined);
                            }
                            poiCellStyles.put(combinedStyleName, combined);
                        }
                        finalCombinedStyleNames.add(combinedStyleName);
                    }
                }

                // Si existe un grupo de estilos para esa columna, primero los agregamos a
                // poiCellStyles combinados en uno solo con el nombre a+b+c. Luego lo usamos
                for (Entry<String, List<String>> entry : columnStyles.entrySet()) {
                    if (entry.getKey().equals(context.getColumnId())) {
                        List<String> stylesForColumn = entry.getValue();
                        String combinedStyleName = stylesForColumn.stream().collect(Collectors.joining("+"));
                        // Si no existe el estilo combinado como estilo poi lo agregamos ahora
                        if (!poiCellStyles.containsKey(combinedStyleName)) {
                            XSSFCellStyle combined = context.getCellStyleCreator().get();
                            for (String style : stylesForColumn) {
                                applyExportStyleToPoiStyle(context, styles.get(style), combined);
                            }
                            poiCellStyles.put(combinedStyleName, combined);
                        }
                        finalCombinedStyleNames.add(combinedStyleName);
                    }
                }

                // Si existe un grupo de estilos para esa celda en particular,
                for (Entry<CellSelectionRule, List<String>> cellStyleEntrySet : cellStyles.entrySet()) {
                    if (cellStyleEntrySet.getKey().match(context.getRow(), context.getCol())) {
                        List<String> cellStyleNamesOfCell = cellStyleEntrySet.getValue();
                        String combinedStyleName = cellStyleNamesOfCell.stream().collect(Collectors.joining("+"));
                        // Si no existe el estilo combinado como estilo poi lo agregamos ahora
                        if (!poiCellStyles.containsKey(combinedStyleName)) {
                            XSSFCellStyle combined = context.getCellStyleCreator().get();
                            for (String style : cellStyleNamesOfCell) {
                                applyExportStyleToPoiStyle(context, styles.get(style), combined);
                            }
                            poiCellStyles.put(combinedStyleName, combined);
                        }
                        finalCombinedStyleNames.add(combinedStyleName);
                    }
                }

                // Si se han encontrado varios estilos se vuelven a combinar en uno solo y se
                // agrega a poiCellStyles
                if (!finalCombinedStyleNames.isEmpty()) {
                    String combinedStyleName = finalCombinedStyleNames.stream().collect(Collectors.joining("+"));
                    if (!poiCellStyles.containsKey(combinedStyleName)) {
                        for (String styleName : finalCombinedStyleNames) {
                            XSSFCellStyle combined = context.getCellStyleCreator().get();
                            for (String style : finalCombinedStyleNames) {
                                applyExportStyleToPoiStyle(context, styles.get(style), combined);
                            }
                            poiCellStyles.put(combinedStyleName, combined);
                        }

                    }
                    return poiCellStyles.get(combinedStyleName);
                }
                return null;
            }
        };
    }

    private static Map<CellSelectionRule, List<String>> createCellStyles(EntityResult entityResult) {
        Map<String, Object> styles = (Map<String, Object>) entityResult.get("cellStyles");
        Map<CellSelectionRule, List<String>> exportCellStyles = new HashMap<>();
        styles.entrySet().stream().forEach(m -> {
            try {
                CellSelectionRule rule = CellSelectionRuleFactory.create(m.getKey());
                Object value = m.getValue();
                if (List.class.isAssignableFrom(value.getClass())) {
                    List<String> styleNames = (List<String>) value;
                    exportCellStyles.put(rule, styleNames);
                } else {
                    List<String> styleNames = new ArrayList<>();
                    styleNames.add((String) value);
                    exportCellStyles.put(rule, styleNames);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return exportCellStyles;
    }

    private static Map<String, List<String>> createColumnHeaderStyles(EntityResult entityResult) {
        Map<String, Object> styles = (Map<String, Object>) entityResult.get("columnHeaderStyles");
        Map<String, List<String>> exportColumnStyles = new HashMap<>();
        styles.entrySet().stream().forEach(m -> {
            Object value = m.getValue();
            if (List.class.isAssignableFrom(value.getClass())) {
                List<String> styleNames = (List<String>) value;
                exportColumnStyles.put(m.getKey(), styleNames);
            } else {
                List<String> styleNames = new ArrayList<>();
                styleNames.add((String) value);
                exportColumnStyles.put(m.getKey(), styleNames);
            }
        });
        return exportColumnStyles;
    }

    private static Map<RowSelectionRule, List<String>> createRowStyles(EntityResult entityResult) {
        Map<String, Object> styles = (Map<String, Object>) entityResult.get("rowStyles");
        Map<RowSelectionRule, List<String>> exportColumnStyles = new HashMap<>();
        styles.entrySet().stream().forEach(m -> {
            try {
                RowSelectionRule rule = RowSelectionRuleFactory.create(m.getKey());
                Object value = m.getValue();
                if (List.class.isAssignableFrom(value.getClass())) {
                    List<String> styleNames = (List<String>) value;
                    exportColumnStyles.put(rule, styleNames);
                } else {
                    List<String> styleNames = new ArrayList<>();
                    styleNames.add((String) value);
                    exportColumnStyles.put(rule, styleNames);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return exportColumnStyles;

    }

    private static Map<String, List<String>> createColumnStyles(EntityResult entityResult) {
        Map<String, Object> styles = (Map<String, Object>) entityResult.get("columnStyles");
        Map<String, List<String>> exportColumnStyles = new HashMap<>();
        styles.entrySet().stream().forEach(m -> {
            Object value = m.getValue();
            if (List.class.isAssignableFrom(value.getClass())) {
                List<String> styleNames = (List<String>) value;
                exportColumnStyles.put(m.getKey(), styleNames);
            } else {

                List<String> styleNames = new ArrayList<>();
                styleNames.add((String) value);
                exportColumnStyles.put(m.getKey(), styleNames);
            }
        });
        return exportColumnStyles;
    }

    private static Map<String, ExportColumnStyle> createStyles(EntityResult entityResult) {
        Map<String, Map<String, String>> styles = (Map<String, Map<String, String>>) entityResult.get("styles");
        Map<String, ExportColumnStyle> exportColumnStyles = new HashMap<>();
        styles.entrySet().stream().forEach(m -> {
            Set<Entry<String, String>> entries = m.getValue().entrySet();
            exportColumnStyles.put(m.getKey(), createColumnStyle(entries));
        });
        return exportColumnStyles;
    }

    private static void applyExportStyleToPoiStyle(CellStyleContext<XSSFCellStyle, DataFormat> context,
            ExportColumnStyle s, XSSFCellStyle style) {
        if (s == null) {
            return;
        }
        if (s.getHorizontalAlignment() != null) {
            style.setAlignment(
                    org.apache.poi.ss.usermodel.HorizontalAlignment.forInt(s.getHorizontalAlignment().getCode()));
        }
        if (s.getVerticalAlignment() != null) {
            style.setVerticalAlignment(
                    org.apache.poi.ss.usermodel.VerticalAlignment.forInt(s.getVerticalAlignment().getCode()));
        }
        if (s.getDataFormatString() != null) {
            DataFormat dataFormat = context.getDataFormatCreator().get();
            style.setDataFormat(dataFormat.getFormat(s.getDataFormatString()));
        }
        if (s.getFillBackgroundColor() != null) {
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            String name = s.getFillBackgroundColor().name();
            IndexedColors indexedColor = IndexedColors.valueOf(name);
            short index = indexedColor.getIndex();
            style.setFillForegroundColor(index);
        }
    }

    private static ExportColumnStyle createColumnStyle(Set<Entry<String, String>> entries) {
        ExportColumnStyle style = new DefaultExportColumnStyle();
        Iterator<Entry<String, String>> i = entries.iterator();
        while (i.hasNext()) {
            Entry<String, String> entry = i.next();
            switch (entry.getKey()) {
                case "dataFormatString":
                    style.setDataFormatString(entry.getValue());
                    break;
                case "alignment":
                    style.setAlignment(HorizontalAlignment.valueOf(entry.getValue()));
                    break;
                case "verticalAlignment":
                    style.setVerticalAlignment(VerticalAlignment.valueOf(entry.getValue()));
                    break;
                case "fillBackgroundColor":
                    style.setFillBackgroundColor(CellColor.valueOf(entry.getValue()));
                    break;
                case "width":
                    style.setWidth(Integer.valueOf(entry.getValue()));
                    break;
            }
        }
        return style;
    }

    // Creamos el fichero temporal y llamamos al creador de providers
    @Override
    public File queryParameters(EntityResult data, List<String> orderColumns, Map<Object, Object> keysValues,
            List<Object> attributesValues, int pageSize, boolean advQuery, int offSet)
            throws OntimizeJEERuntimeException, IOException {
        System.out.println(keysValues);

        File xlsxFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".xlsx");
//        generateExcel(data, orderColumns, xlsxFile, keysValues, attributesValues, pageSize, advQuery, offSet);
        return xlsxFile;

    }

    @Override
    public File generateFile(final ExportQueryParameters exportParam) throws ExportException {
        File xlsxFile = null;
        try {
            ExcelExportQueryParameters excelExportParam = (ExcelExportQueryParameters)exportParam; 
            
            EntityResult entityResult = new EntityResultMapImpl();
            entityResult.put("cellStyles", excelExportParam.getCellStyles());
            entityResult.put("excelColumns", excelExportParam.getExcelColumns());
            entityResult.put("columnStyles", excelExportParam.getColumnStyles());
            entityResult.put("columnTypes", excelExportParam.getColumnTypes());
            entityResult.put("dao", excelExportParam.getDao());
            entityResult.put("path", excelExportParam.getPath());
            entityResult.put("rowStyles", excelExportParam.getRowStyles());
            entityResult.put("service", excelExportParam.getService());
            entityResult.put("columnTitles", excelExportParam.getColumnTitles());
            entityResult.put("styles", excelExportParam.getStyles());
            
            xlsxFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".xlsx");
            generateExcel(entityResult, xlsxFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return xlsxFile;
    }

    /**
     * Create all the providers and generate the book
     * @param entityResult The entity result with all data
     * @param xlsxFile The tempfile
     */
    public void generateExcel(EntityResult entityResult, File xlsxFile) {
        try {
            // ColumnProvider
            ExportColumnProvider columnProvider = createColumnProvider(entityResult);

            // DataProvider
//            String service = String.valueOf(entityResult.get("service"));
//            String path = String.valueOf(entityResult.get("path"));
//            Object serviceBean = this.getApplicationContextUtils().getServiceBean(service, path);
//            
//            StringBuffer buffer = new StringBuffer();
//            buffer.append(entityResult.get("dao")).append(ORestController.QUERY);
//
//            List<SQLOrder> orderBy = new ArrayList<SQLOrder>();
//            orderBy.add(new SQLOrder("NAME"));
//
//            ExcelExportDataProvider dataProvider = generateDataProvider(
//                    serviceBean,
//                    (QueryParameter) entityResult.get("queryParameters"), buffer.toString(), keysValues,
//                    attributesValues, columnProvider.getBodyColumns(), pageSize, orderBy, advQuery, offSet);
            ExportDataProvider dataProvider = getDataProvider();

            // StyleProvider
            ExportStyleProvider styleProvider = createStyleProvider(entityResult);

            // SheetNameProvider
            SheetNameProvider sheetNameProvider = createDefaultSheetNameProvider();

            // ExportOptions
            ExportOptions exportOptions = this.createExportOptions(entityResult, new ArrayList<>() /*orderColumns*/);

            final Workbook book = generateBook(columnProvider, dataProvider, styleProvider, sheetNameProvider,
                    exportOptions);

            final FileOutputStream fileOutputStream = new FileOutputStream(xlsxFile);
            book.write(fileOutputStream);
            fileOutputStream.close();
        } catch (final Exception e) {
            logger.error("{}", e.getMessage(), e);
        }
    }

    /**
     * Calls the exporter and send it the providers to build the excel file
     * @param columnProvider The column provider
     * @param dataProvider The data provider
     * @param styleProvider The styles provider
     * @param sheetNameProvider The sheet name provider
     * @param exportOptions Is null, it creates it in the BaseExcelExporter
     * @return
     * @throws Exception
     */
    public Workbook generateBook(ExportColumnProvider columnProvider, ExportDataProvider dataProvider,
            ExportStyleProvider<XSSFCellStyle, DataFormat> styleProvider, SheetNameProvider sheetNameProvider,
            ExportOptions exportOptions) throws Exception {
        return new DefaultXSSFExcelExporter().export(columnProvider, dataProvider, styleProvider, sheetNameProvider,
                exportOptions);
    }

    private ExportOptions createExportOptions(EntityResult data, List<String> columns) {
        // TODO: de momento no hay opciones
        return null;
    }

    private SheetNameProvider createSheetNameProvider(int pageSize) {
        return new PaginatedSheetNameProvider(pageSize);
    }

    private SheetNameProvider createDefaultSheetNameProvider() {
        return new DefaultSheetNameProvider();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }

    public ApplicationContext getContext() {
        return appContext;
    }

    public ApplicationContextUtils getApplicationContextUtils() {
        if(this.applicationContextUtils == null){
            this.applicationContextUtils = new ApplicationContextUtils();
            ((ApplicationContextAware) this.applicationContextUtils).setApplicationContext(this.appContext);
        }
        return applicationContextUtils;
    }
}
