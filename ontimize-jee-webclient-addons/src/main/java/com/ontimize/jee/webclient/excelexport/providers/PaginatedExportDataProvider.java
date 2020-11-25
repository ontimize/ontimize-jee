package com.ontimize.jee.webclient.excelexport.providers;

import java.util.List;

import com.ontimize.jee.webclient.excelexport.pagination.PaginationRequest;
import com.ontimize.jee.webclient.excelexport.pagination.PaginationResult;

import javafx.util.Callback;

/**
 * Provider de datos que utiliza paginación.
 *
 * @author <a href="antonio.vazquez@imatia.com">antoniova</a>
 */
public interface PaginatedExportDataProvider<T> {

    void reset();

    T getData(int row);

    Callback<PaginationRequest, PaginationResult<List<T>>> getPageFactory();

    void setPageFactory(Callback<PaginationRequest, PaginationResult<List<T>>> pageFactory);

    int getRowsPerPage();

    void setRowsPerPage(int rowsPerPage);

}
