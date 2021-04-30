package com.ontimize.jee.webclient.export.executor.support.sheetnameprovider;

import com.ontimize.jee.webclient.export.SheetContext;
import com.ontimize.jee.webclient.export.providers.SheetNameProvider;

import javafx.util.Callback;

/**
 * @author <a href="enrique.alvarez@imatia.com">Enrique Álvarez Pereira</a>
 *
 *         En caso de querer que de una query paginada se devuelva cada página en una hoja de excel,
 *         se utilizaría este provider.
 */
public class PaginatedSheetNameProvider implements SheetNameProvider {

    public int maxRows = 0;

    public PaginatedSheetNameProvider(int maxRows) {
        this.maxRows = maxRows;
    }

    @Override
    public String getDefaultSheetName() {
        return "Hoja";
    }

    @Override
    public Callback<SheetContext, String> getSheetName() {
        return param -> {
            if (param.getNumRows() >= maxRows) {
                return this.getDefaultSheetName()
                        + "_"
                        + (param.getActualSheetIndex() + 1);
            }
            return param.getActualSheetName();
        };
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

}
