package com.ontimize.jee.webclient.export.support.dataprovider;

import java.util.Enumeration;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.server.rest.ORestController;
import com.ontimize.jee.server.rest.QueryParameter;
import com.ontimize.jee.webclient.export.HeadExportColumn;
import com.ontimize.jee.webclient.export.base.BaseExportQueryParameters;
import com.ontimize.jee.webclient.export.base.ExportQueryParameters;
import com.ontimize.jee.webclient.export.providers.ExportDataProvider;
import org.apache.commons.collections4.iterators.IteratorEnumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEntityResultExportDataProvider implements ExportDataProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEntityResultExportDataProvider.class);

    EntityResult entityResult;
    
    private String service;
    
    private Object serviceBean;
    private String dao;
    
    private String daoMethod;
    
    private int numOfCols;

    private final QueryParameter queryParameters;

    public DefaultEntityResultExportDataProvider(final ExportQueryParameters exportParam) {
        this.service = exportParam.getService();
        this.dao = exportParam.getDao();
        this.queryParameters = exportParam.getQueryParam();
        this.numOfCols = exportParam.getQueryParam().getColumns().size();
        this.initialize(exportParam);
    }
    
    protected void initialize(final ExportQueryParameters exportParam) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.getDao()).append(ORestController.QUERY);
        this.daoMethod = buffer.toString();
    }

    @Override
    public String getService() {
        return this.service;
    }

    @Override
    public void setServiceBean(Object serviceBean) {
        this.serviceBean = serviceBean;
    }

    @Override
    public Object getServiceBean() {
        return this.serviceBean;
    }

    @Override
    public String getDao() {
        return this.dao;
    }

    protected String getDaoMethod() {
        return this.daoMethod;
    }

    @Override
    public QueryParameter getQueryParameters() {
        return this.queryParameters;
    }

    public EntityResult getEntityResult() {
        if (entityResult == null) {
            entityResult = doQuery();
        }

        return entityResult;
    }

    @Override
    public int getNumberOfRows() {
        return getEntityResult().calculateRecordNumber();
    }

    @Override
    public int getNumberOfColumns() {
        return this.numOfCols;
    }

    @Override
    public int getColumnIndex(HeadExportColumn column) {
        return getEntityResult().getRecordValues(0).size();
//        return this.entityResult.getOrderColumns().indexOf(column.getId());
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
    
    public Object getCellValue(final int row, final String colId) {
        Map recordValues = this.getEntityResult().getRecordValues(row);
        return recordValues.get(colId);
    }


    public EntityResult doQuery() {
        return (EntityResult) ReflectionTools.invoke(this.getServiceBean(), 
            this.getDaoMethod(),
            this.getQueryParameters().getFilter(),
            this.getQueryParameters().getColumns());
    }


}
