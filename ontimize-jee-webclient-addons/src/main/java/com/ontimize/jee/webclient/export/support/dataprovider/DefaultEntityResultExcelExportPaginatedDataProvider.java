package com.ontimize.jee.webclient.export.support.dataprovider;

import java.beans.PropertyDescriptor;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.server.rest.QueryParameter;
import com.ontimize.jee.webclient.export.HeadExportColumn;
import com.ontimize.jee.webclient.export.pagination.PaginationRequest;
import com.ontimize.jee.webclient.export.pagination.PaginationResult;
import com.ontimize.jee.webclient.export.providers.ExcelExportDataProvider;
import org.apache.commons.collections4.iterators.IteratorEnumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEntityResultExcelExportPaginatedDataProvider<T> implements ExcelExportDataProvider {

    private static final Integer ROWS_PER_PAGE = 1000;

    List<PropertyDescriptor> descriptors;

    List<T> pageData;

    int pageIndex = -1;

    long totalSize = -1;

    private int rowsPerPage;

    private Function<PaginationRequest, PaginationResult<List<T>>> pageFactory;

    private static final Logger LOGGER = LoggerFactory
        .getLogger(DefaultEntityResultExcelExportPaginatedDataProvider.class);

    EntityResult entityResult;

    public DefaultEntityResultExcelExportPaginatedDataProvider(EntityResult entityResult) {
        this.entityResult = entityResult;
    }


    public void reset() {
        final PaginationRequest paginationRequest = new PaginationRequest(1, this.getRowsPerPage(),
                this.getRowsPerPage());
        final PaginationResult<List<T>> paginationResult = this.getPageFactory().apply(paginationRequest);
        if (paginationResult != null) {
            this.pageData = paginationResult.getResult();
            this.totalSize = paginationResult.getTotalSize();
            if (this.getRowsPerPage() > this.totalSize) {
                this.setRowsPerPage((int) this.totalSize);
            }
            this.pageIndex = 1;
        }
    }

    public T getData(final int row) {
        int pageOfRow = (row / this.getRowsPerPage()) + 1;
        if (this.pageData == null) {
            this.reset();
            pageOfRow = 0;
        }
        if (pageOfRow != this.pageIndex) {
            this.pageIndex = pageOfRow;
            this.loadPage(this.pageIndex);
        }
        final int localIndex = row % this.getRowsPerPage();
        return this.pageData.get(localIndex);
    }

    private void loadPage(final int pageIndex) {
        final PaginationResult<List<T>> paginationResult = this.getPageFactory()
            .apply(new PaginationRequest(pageIndex, this.getRowsPerPage(), 0));
        this.pageData = paginationResult.getResult();
    }


    @Override
    public int getNumberOfRows() {
        return this.entityResult.calculateRecordNumber();
    }

    @Override
    public int getNumberOfColumns() {
        return this.entityResult.getColumnSQLTypes().size();
    }


    @Override
    public Object getCellValue(final int row, final int column) {
        Map record = entityResult.getRecordValues(row);
        int n = 0;
        // TODO ver cual de los dos es m�s viable
        Enumeration i = new IteratorEnumeration(record.keySet().iterator());
        Enumeration e = entityResult.keys();
        Object ret = null;
        while (i.hasMoreElements()) {
            ret = record.get(i.nextElement());
            if (n == column) {
                return ret;
            }
            n++;
        }
        return ret;
    }


    @Override
    public int getColumnIndex(HeadExportColumn column) {
        return this.entityResult.getOrderColumns().indexOf(column.getId());
    }

    public Function<PaginationRequest, PaginationResult<List<T>>> getPageFactory() {
        return this.pageFactory;
    }

    public void setPageFactory(final Function<PaginationRequest, PaginationResult<List<T>>> pageFactory) {
        this.pageFactory = pageFactory;
        this.reset();
    }

    public int getRowsPerPage() {
        return this.rowsPerPage;
    }

    public void setRowsPerPage(final int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
        this.reset();
    }

    @Override
    public String getService() {
        return null;
    }

    @Override
    public String getDao() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QueryParameter getQueryParameters() {
        // TODO Auto-generated method stub
        return null;
    }

}
