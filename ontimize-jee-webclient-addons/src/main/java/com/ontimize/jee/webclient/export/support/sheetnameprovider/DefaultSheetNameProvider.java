package com.ontimize.jee.webclient.export.support.sheetnameprovider;

import java.util.function.Function;

import com.ontimize.jee.webclient.export.SheetContext;
import com.ontimize.jee.webclient.export.providers.SheetNameProvider;
import org.apache.poi.ss.SpreadsheetVersion;

/**
 * @author <a href="antonio.vazquez@imatia.com">Antonio V�zquez Ara�jo</a>
 */
public class DefaultSheetNameProvider implements SheetNameProvider {

    // Suponemos una altura m�xima de 20 lineas para la cabecera
    public static final int MAX_ROWS = SpreadsheetVersion.EXCEL2007.getMaxRows() - 20;

    @Override
    public String getDefaultSheetName() {
        // TODO I18N
        return "Hoja";
    }

    @Override
    public Function<SheetContext, String> getSheetName() {
        return param -> {
            if (param.getNumRows() >= MAX_ROWS) {
                return this.getDefaultSheetName()
                        + "_"
                        + (param.getActualSheetIndex() + 1);
            }
            return param.getActualSheetName();
        };
    }

}
