package export.barebean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import com.ontimize.jee.webclient.export.CellStyleContext;
import com.ontimize.jee.webclient.export.ExportColumnStyle;
import com.ontimize.jee.webclient.export.ExportColumnStyle.CellColor;
import com.ontimize.jee.webclient.export.SheetContext;
import com.ontimize.jee.webclient.export.providers.ExportColumnProvider;
import com.ontimize.jee.webclient.export.providers.SheetNameProvider;
import com.ontimize.jee.webclient.export.support.DefaultExportColumnStyle;
import com.ontimize.jee.webclient.export.support.exporter.DefaultXSSFExcelExporter;
import com.ontimize.jee.webclient.export.support.styleprovider.DefaultExcelExportStyleProvider;
import export.helpers.BareBeanExportHelper;
import export.support.dataprovider.DefaultBareBeanExcelExportDataProvider;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

public class BareBeanExcelExportTest {

    public static void main(final String[] args) {
        // FIXME
//        final List<BareExportBean> personList = BareExportBean.getBareExportBeanList(1000);
//        final ExportColumnProvider exportColumnProvider = BareBeanExportHelper
//            .getExportContextFromBean(BareExportBean.class);
//        final DefaultXSSFExcelExporter exporter = new DefaultXSSFExcelExporter();
//        try {
//            final Workbook workBook = exporter
//                .export(
//                        exportColumnProvider,
//                        new DefaultBareBeanExcelExportDataProvider(personList),
//                        new DefaultExcelExportStyleProvider<CellStyle>() {
//                            private CellStyle style1;
//
//                            private CellStyle style2;
//
//                            // @Override
//                            // public ExportColumnStyle getHeaderStyle(final String columnId) {
//                            // if ("percentage".equals(columnId)) {
//                            // final ExportColumnStyle ret = new DefaultExportColumnStyle();
//                            // ret.setFillBackgroundColor(ExportColumnStyle.CellColor.LIGHT_GREEN);
//                            // ret.setAlignment(ExportColumnStyle.HorizontalAlignment.CENTER);
//                            // ret.setVerticalAlignment(ExportColumnStyle.VerticalAlignment.CENTER);
//                            // return ret;
//                            // }
//                            // return null;
//                            // }
//
//                            @Override
//                            public ExportColumnStyle getColumnStyle(final String columnId) {
//                                final ExportColumnStyle ret = new DefaultExportColumnStyle();
//                                if (columnId.equals("percentage")) {
//                                    ret.setDataFormatString("0.00%");
//                                    ret.setFillBackgroundColor(CellColor.CORAL);
//                                } else if (columnId.equals("money")) {
//                                    ret.setDataFormatString("#.##0,00 €;-#.##0,00 €");
//                                    ret.setFillBackgroundColor(CellColor.AQUA);
//                                }
//                                return ret;
//                            }
//
//                            @Override
//                            public CellStyle getCellStyle(final CellStyleContext<CellStyle, DataFormat> context) {
//                                if (context.getRow() % 3 == 0 && context.getCol() == 6) {
//                                    if (this.style1 == null) {
//                                        this.style1 = context.getCellStyleCreator().get();
//                                        this.style1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//                                        this.style1.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
//                                        this.style1.setBorderLeft(BorderStyle.THICK);
//                                        this.style1.setLeftBorderColor(IndexedColors.DARK_RED.getIndex());
//                                    }
//                                    return this.style1;
//                                } else if (context.getRow() % 4 == 0 && context.getCol() == 6) {
//                                    if (this.style2 == null) {
//                                        this.style2 = context.getCellStyleCreator().get();
//                                        this.style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//                                        this.style2.setFillForegroundColor(IndexedColors.CORAL.getIndex());
//                                        this.style2.setBorderLeft(BorderStyle.THICK);
//                                        this.style2.setLeftBorderColor(IndexedColors.GREEN.getIndex());
//                                    }
//                                    return this.style2;
//                                }
//                                return null;
//                            }
//
//                        },
//                        new SheetNameProvider() {
//                            @Override
//                            public String getDefaultSheetName() {
//                                return "Resultados";
//                            }
//
//                            @Override
//                            public Function<SheetContext, String> getSheetName() {
//                                return context -> {
//                                    if (context.getNumRows() > 100) {
//                                        return this.getDefaultSheetName() + "_" + context.getActualSheetIndex();
//                                    }
//                                    return context.getActualSheetName();
//                                };
//                            }
//                        },
//
//                        null);
//
//            final File file = new File("BareBean.xlsx");
//            final FileOutputStream fileOutputStream = new FileOutputStream(file);
//            workBook.write(fileOutputStream);
//            fileOutputStream.close();
//        } catch (final IOException e) {
//            e.printStackTrace();
//        }
//        System.exit(0);
    }

}
