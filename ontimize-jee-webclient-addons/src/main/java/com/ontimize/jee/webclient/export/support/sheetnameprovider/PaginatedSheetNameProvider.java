package com.ontimize.jee.webclient.export.support.sheetnameprovider;

import java.util.function.Function;

import com.ontimize.jee.webclient.export.SheetContext;
import com.ontimize.jee.webclient.export.providers.SheetNameProvider;

/**
 * @author <a href="enrique.alvarez@imatia.com">Enrique �lvarez Pereira</a>
 *
 *         En caso de querer que de una query paginada se devuelva cada p�gina en una hoja de excel,
 *         se utilizar�a este provider.
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
    public Function<SheetContext, String> getSheetName() {
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
