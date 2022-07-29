package com.ontimize.jee.webclient.export.providers;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.server.rest.QueryParameter;

/**
 * Provider que proporciona los datos de la exportación.
 *
 * @author antonio.vazquez@imatia.com Antonio Vázquez Araújo
 */

public interface ExportDataProvider<T> {

    String getService();
    
    void setServiceBean(Object serviceBean);
    
    Object getServiceBean();

    String getDao();

    QueryParameter getQueryParameters();

    int getNumberOfRows();

    int getNumberOfColumns();

    Object getCellValue(int row, String colId);

    T getData();

    T doQuery();

}
