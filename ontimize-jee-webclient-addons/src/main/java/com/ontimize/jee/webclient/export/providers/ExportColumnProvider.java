package com.ontimize.jee.webclient.export.providers;

import java.util.List;

import com.ontimize.jee.webclient.export.ExportColumn;
import com.ontimize.jee.webclient.export.HeadExportColumn;

/**
 * Contiene la información de columnas en dos grupos: columnas parent, que contienen otras columnas
 * anidadas y columnas hijas
 *
 * @author <a href="antonio.vazquez@imatia.com">antoniova</a>
 */
public interface ExportColumnProvider {

    List<HeadExportColumn> getHeaderColumns();

    List<ExportColumn> getBodyColumns();

}
