package export.entityresult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

import com.ontimize.db.EntityResult;
import com.ontimize.db.EntityResultUtils;
import com.ontimize.jee.webclient.excelexport.BaseExcelExporter;
import com.ontimize.jee.webclient.excelexport.CellStyleContext;
import com.ontimize.jee.webclient.excelexport.ExportColumnStyle;
import com.ontimize.jee.webclient.excelexport.ExportColumnStyle.CellColor;
import com.ontimize.jee.webclient.excelexport.ExportColumnStyle.HorizontalAlignment;
import com.ontimize.jee.webclient.excelexport.SXSSFWorkbookExcelExporter;
import com.ontimize.jee.webclient.excelexport.SheetContext;
import com.ontimize.jee.webclient.excelexport.executor.support.DefaultExportColumnStyle;
import com.ontimize.jee.webclient.excelexport.executor.support.dataprovider.DefaultEntityResultExcelExportPaginatedDataProvider;
import com.ontimize.jee.webclient.excelexport.executor.support.styleprovider.DefaultExcelExportStyleProvider;
import com.ontimize.jee.webclient.excelexport.helpers.EntityResultExportHelper;
import com.ontimize.jee.webclient.excelexport.pagination.PaginationRequest;
import com.ontimize.jee.webclient.excelexport.pagination.PaginationResult;
import com.ontimize.jee.webclient.excelexport.providers.ExportColumnProvider;
import com.ontimize.jee.webclient.excelexport.providers.SheetNameProvider;

import export.barebean.BareExportBean;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.util.Callback;

public class EntityResultPaginatedExcelExportTest extends Application {

    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) {
        final ObservableList<BareExportBean> personList = BareExportBean.getBareExportBeanList(5000);
        EntityResult entityResult = EntityResultUtils.createEmptyEntityResult(new ArrayList());
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

        final ExportColumnProvider exportColumnProvider = EntityResultExportHelper.getExportContextFromEntityResult(entityResult);
        final BaseExcelExporter exporter = new SXSSFWorkbookExcelExporter();
        try {
            final DefaultEntityResultExcelExportPaginatedDataProvider dataProvider = new DefaultEntityResultExcelExportPaginatedDataProvider(entityResult);
            dataProvider.setRowsPerPage(10);

            dataProvider.setPageFactory((Callback<PaginationRequest, PaginationResult<List<BareExportBean>>>) param -> {
                int realEnd = param.getRowsRange().getEnd() + 1;
                if (realEnd > personList.size()) {
                    realEnd = personList.size();
                }
                System.out.println(
                        "Page:" + param.getCurrentPage() + " Start:" + param.getRowsRange().getStart() + " End: "
                                + realEnd);
                return new PaginationResult<>(
                        personList.size(),
                        param.getRowsRange().getStart(),
                        personList.subList(
                                param.getRowsRange().getStart(),
                                realEnd));
            });
            dataProvider.setRowsPerPage(100);
            final Workbook workBook = exporter
                .export(
                    exportColumnProvider,
                        dataProvider,
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
                                if(columnId.equals("percentage")){
                                    ret.setDataFormatString("0.00%");
                                    ret.setFillBackgroundColor(CellColor.CORAL);
                                    ret.setAlignment(HorizontalAlignment.RIGHT);
                                }else if (columnId.equals("money")){
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

            final File file = new File("PaginatedEntityResult.xlsx");
            final FileOutputStream fileOutputStream = new FileOutputStream(file);
            workBook.write(fileOutputStream);
            fileOutputStream.close();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
    static  Hashtable extractExportBeanFieldTypes() {
        Hashtable ret = new Hashtable();
        ret.put("id"               ,JDBCType.INTEGER.getVendorTypeNumber());
        ret.put("firstName"        ,JDBCType.VARCHAR.getVendorTypeNumber());
        ret.put("lastName"         ,JDBCType.VARCHAR.getVendorTypeNumber());
        ret.put("gender"           ,JDBCType.VARCHAR.getVendorTypeNumber());
        ret.put("age"              ,JDBCType.INTEGER.getVendorTypeNumber());
        ret.put("single"           ,JDBCType.BIT.getVendorTypeNumber());
        ret.put("birthDate"        ,JDBCType.DATE.getVendorTypeNumber());
        ret.put("localBirthDate"   ,JDBCType.DATE.getVendorTypeNumber());
        ret.put("percentage"       ,JDBCType.DECIMAL.getVendorTypeNumber());
        ret.put("doublePercentage" ,JDBCType.DECIMAL.getVendorTypeNumber());
        ret.put("money"            , JDBCType.DOUBLE.getVendorTypeNumber());
        ret.put("street"           ,JDBCType.VARCHAR.getVendorTypeNumber());
        ret.put("zipCode"          ,JDBCType.INTEGER.getVendorTypeNumber());
        ret.put("city"             ,JDBCType.VARCHAR.getVendorTypeNumber());
        ret.put("country"          ,JDBCType.VARCHAR.getVendorTypeNumber());
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
}
