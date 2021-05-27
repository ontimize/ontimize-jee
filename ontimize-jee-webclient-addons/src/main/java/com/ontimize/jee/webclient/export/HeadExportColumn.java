package com.ontimize.jee.webclient.export;

import java.util.List;

/**
 * Columna parent, que contiene otras columnas
 *
 * @author antonio.vazquez@imatia.com Antonio Vázquez Araújo
 */

public interface HeadExportColumn {

    String getId();

    String getTitle();

    int getHeadExportColumnCount();

    List<HeadExportColumn> getColumns();

    HeadExportColumn getHeadExportColumn(int index);

    void addHeadExportColumn(HeadExportColumn headExportColumn);

    boolean contains(HeadExportColumn headExportColumn);

}
