package export.entityresult;

import org.junit.Assert;
import com.ontimize.jee.webclient.export.CellStyleContext;
import com.ontimize.jee.webclient.export.ExportColumnStyle;
import com.ontimize.jee.webclient.export.ExportColumnStyle.CellColor;
import com.ontimize.jee.webclient.export.ExportColumnStyle.HorizontalAlignment;
import com.ontimize.jee.webclient.export.SheetContext;
import com.ontimize.jee.webclient.export.executor.support.DefaultExportColumnStyle;
import com.ontimize.jee.webclient.export.executor.support.dataprovider.DefaultEntityResultExcelExportDataProvider;
import com.ontimize.jee.webclient.export.executor.support.exporter.DefaultXSSFExcelExporter;
import com.ontimize.jee.webclient.export.executor.support.styleprovider.DefaultExcelExportStyleProvider;
import com.ontimize.jee.webclient.export.helpers.EntityResultExportHelper;
import com.ontimize.jee.webclient.export.providers.ExportColumnProvider;
import com.ontimize.jee.webclient.export.providers.SheetNameProvider;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import export.barebean.BareExportBean;
import javafx.collections.ObservableList;
import javafx.util.Callback;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class EntityResultExcelExportTest {

    public static void main(String[] args) {
        // pruebaEntityResults();
        export();

        // testPoi();
    }

    public static void testPoi() {
        ClassLoader classloader = org.apache.poi.poifs.filesystem.POIFSFileSystem.class.getClassLoader();
        URL res = classloader.getResource(
                "org/apache/poi/poifs/filesystem/POIFSFileSystem.class");
        String path = res.getPath();
        System.out.println("POI Core came from " + path);

        classloader = org.apache.poi.ooxml.POIXMLDocument.class.getClassLoader();
        res = classloader.getResource("org/apache/poi/POIXMLDocument.class");
        if (res != null) {
            path = res.getPath();
            System.out.println("POI OOXML came from " + path);
        } else {
            System.out.println("No POI OOXML!");
        }


        classloader = org.apache.poi.hssf.usermodel.HSSFSheet.class.getClassLoader();
        res = classloader.getResource("org/apache/poi/hssf/usermodel/HSSFSheet.class");
        path = res.getPath();
        System.out.println("POI Scratchpad came from " + path);
    }
    // private static void pruebaEntityResults() {
    // final ObservableList<BareExportBean> personList = BareExportBean.getBareExportBeanList(1000);
    //
    // EntityResult entityResult = EntityResultUtils.createEmptyEntityResult(new ArrayList());
    //
    // Hashtable types = extractExportBeanFieldTypes();
    // // Ponemos a la entityResult los tipos de columnas extraidos del BareExportBean
    // entityResult.setColumnSQLTypes(types);
    //
    // // Ahora le ponemos el orden de los campos
    // List order = new ArrayList();
    // Enumeration i = types.keys();
    // while (i.hasMoreElements()) {
    // order.add(i.nextElement());
    // }
    // entityResult.setColumnOrder(order);
    //
    // // Agregamos todos los registros
    // for (BareExportBean bean : personList) {
    // entityResult.addRecord(bareExportBeanToRecord(bean));
    // }
    //
    // Vector fieldsVector = new Vector();
    // fieldsVector.add("country");
    // fieldsVector.add("id");
    //
    // // Extraer esos campos pero solo del registro 0
    // Hashtable ee = entityResult.getRecordValues(0, fieldsVector);
    //
    // Enumeration registros = ee.keys();
    // int index = 0;
    // while(registros.hasMoreElements()){
    // Object registro = registros.nextElement();
    // Vector column = (Vector) entityResult.get(registro);
    // System.out.println(" Field: "+ registro + ": " + column.get(index++));
    // }
    // System.out.println("Extraer todos los campos de todos los registros");
    // for(int n=0; n<entityResult.size(); n++) {
    // Hashtable record = entityResult.getRecordValues(n);
    // Enumeration fieldsEnumeration = record.keys();
    // while (fieldsEnumeration.hasMoreElements()) {
    // Object field = fieldsEnumeration.nextElement();
    // System.out.print(" " + field + ":"+ record.get(field)+ " ");
    // }
    // System.out.println();
    // }
    // }


    public static void export() {

        final ObservableList<BareExportBean> personList = BareExportBean.getBareExportBeanList(1000);

        EntityResult entityResult = new EntityResultMapImpl(new ArrayList());

        Hashtable types = extractExportBeanFieldTypes();
        // Ponemos a la entityResult los tipos de columnas extraidos del BareExportBean
        entityResult.setColumnSQLTypes(types);

        // Ahora le ponemos el orden de los campos
        List order = new ArrayList();
        Enumeration i = types.keys();
        while (i.hasMoreElements()) {
            order.add(i.nextElement());
        }
        entityResult.setColumnOrder(order);

        // Agregamos todos los registros
        for (BareExportBean bean : personList) {
            entityResult.addRecord(bareExportBeanToRecord(bean));
        }


        final ExportColumnProvider exportColumnProvider = EntityResultExportHelper
            .getExportContextFromEntityResult(entityResult);
        final DefaultXSSFExcelExporter exporter = new DefaultXSSFExcelExporter();
        try {
            final Workbook workBook = exporter
                .export(
                        exportColumnProvider,
                        new DefaultEntityResultExcelExportDataProvider(entityResult),
                        new DefaultExcelExportStyleProvider<CellStyle>() {
                            private CellStyle style1;

                            private CellStyle style2;

                            // @Override
                            // public ExportColumnStyle getHeaderStyle(final String columnId) {
                            // if ("percentage".equals(columnId)) {
                            // final ExportColumnStyle ret = new DefaultExportColumnStyle();
                            // ret.setFillBackgroundColor(ExportColumnStyle.CellColor.LIGHT_GREEN);
                            // ret.setAlignment(ExportColumnStyle.HorizontalAlignment.CENTER);
                            // ret.setVerticalAlignment(ExportColumnStyle.VerticalAlignment.CENTER);
                            // return ret;
                            // }
                            // return null;
                            // }

                            @Override
                            public ExportColumnStyle getColumnStyle(final String columnId) {
                                final ExportColumnStyle ret = new DefaultExportColumnStyle();
                                if (columnId.equals("percentage")) {
                                    ret.setDataFormatString("0.00%");
                                    ret.setFillBackgroundColor(CellColor.CORAL);
                                    ret.setAlignment(HorizontalAlignment.RIGHT);
                                } else if (columnId.equals("money")) {
                                    ret.setDataFormatString("#.##0,00 €;-#.##0,00 €");
                                    ret.setFillBackgroundColor(CellColor.AQUA);
                                    ret.setAlignment(HorizontalAlignment.RIGHT);
                                }
                                return ret;
                            }

                            @Override
                            public CellStyle getCellStyle(final CellStyleContext<CellStyle, DataFormat> context) {
                                if (context.getRow() % 3 == 0 && context.getCol() == 6) {
                                    if (this.style1 == null) {
                                        this.style1 = context.getCellStyleCreator().get();
                                        this.style1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                                        this.style1.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
                                        this.style1.setBorderLeft(BorderStyle.THICK);
                                        this.style1.setLeftBorderColor(IndexedColors.DARK_RED.getIndex());
                                    }
                                    return this.style1;
                                } else if (context.getRow() % 4 == 0 && context.getCol() == 6) {
                                    if (this.style2 == null) {
                                        this.style2 = context.getCellStyleCreator().get();
                                        this.style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                                        this.style2.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                                        this.style2.setBorderLeft(BorderStyle.THICK);
                                        this.style2.setLeftBorderColor(IndexedColors.GREEN.getIndex());
                                    }
                                    return this.style2;
                                }
                                return null;
                            }

                        },
                        new SheetNameProvider() {
                            @Override
                            public String getDefaultSheetName() {
                                return "Resultados";
                            }

                            @Override
                            public Callback<SheetContext, String> getSheetName() {
                                return context -> {
                                    if (context.getNumRows() > 100) {
                                        return this.getDefaultSheetName() + "_" + context.getActualSheetIndex();
                                    }
                                    return context.getActualSheetName();
                                };
                            }
                        },
                        null);

            final File file = new File("EntityResult.xlsx");
            final FileOutputStream fileOutputStream = new FileOutputStream(file);
            workBook.write(fileOutputStream);
            fileOutputStream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        System.exit(0);

    }


    static Hashtable extractExportBeanFieldTypes() {
        Hashtable ret = new Hashtable();
        ret.put("id", JDBCType.INTEGER.getVendorTypeNumber());
        ret.put("firstName", JDBCType.VARCHAR.getVendorTypeNumber());
        ret.put("lastName", JDBCType.VARCHAR.getVendorTypeNumber());
        ret.put("gender", JDBCType.VARCHAR.getVendorTypeNumber());
        ret.put("age", JDBCType.INTEGER.getVendorTypeNumber());
        ret.put("single", JDBCType.BIT.getVendorTypeNumber());
        ret.put("birthDate", JDBCType.DATE.getVendorTypeNumber());
        ret.put("localBirthDate", JDBCType.DATE.getVendorTypeNumber());
        ret.put("percentage", JDBCType.DECIMAL.getVendorTypeNumber());
        ret.put("doublePercentage", JDBCType.DECIMAL.getVendorTypeNumber());
        ret.put("money", JDBCType.DOUBLE.getVendorTypeNumber());
        ret.put("street", JDBCType.VARCHAR.getVendorTypeNumber());
        ret.put("zipCode", JDBCType.INTEGER.getVendorTypeNumber());
        ret.put("city", JDBCType.VARCHAR.getVendorTypeNumber());
        ret.put("country", JDBCType.VARCHAR.getVendorTypeNumber());
        return ret;
    }

    static Hashtable bareExportBeanToRecord(BareExportBean bean) {
        Hashtable ret = new Hashtable();
        ret.put("id", bean.getId());
        ret.put("firstName", bean.getFirstName());
        ret.put("lastName", bean.getLastName());
        ret.put("gender", bean.getGender());
        ret.put("age", bean.getAge());
        ret.put("single", bean.isSingle());
        ret.put("birthDate", bean.getBirthDate());
        ret.put("localBirthDate", bean.getLocalBirthDate());
        ret.put("percentage", bean.getPercentage());
        ret.put("doublePercentage", bean.getDoublePercentage());
        ret.put("money", bean.getMoney());
        ret.put("street", bean.getStreet());
        ret.put("zipCode", bean.getZipCode());
        ret.put("city", bean.getCity());
        ret.put("country", bean.getCountry());

        return ret;
    }

    // @Test
    public void addColumnToEntityResult() {
        EntityResult entityResult = new EntityResultMapImpl(new ArrayList());
        Hashtable record = new Hashtable();
        record.put("col1", "1-1");
        record.put("col2", "2-1");
        entityResult.addRecord(record);

        record = new Hashtable();
        record.put("col1", "1-2");
        record.put("col2", "2-2");
        entityResult.addRecord(record);
        Assert.assertTrue(entityResult.calculateRecordNumber() == 2);

        Hashtable sqlTypes = new Hashtable<>();
        sqlTypes.put("col1", JDBCType.VARCHAR);
        sqlTypes.put("col2", JDBCType.VARCHAR);
        entityResult.setColumnSQLTypes(sqlTypes);
        List order = new ArrayList();
        Enumeration i = sqlTypes.keys();
        while (i.hasMoreElements()) {
            order.add(i.nextElement());
        }
        entityResult.setColumnOrder(order);
        entityResult.getOrderColumns().stream().forEach(t -> {
            System.out.println(t);
        });
        Map x = entityResult.getRecordValues(0);
        entityResult.getRecordValues(0).entrySet().stream().forEach(t -> {
            System.out.println(t);
        });
    }

}
