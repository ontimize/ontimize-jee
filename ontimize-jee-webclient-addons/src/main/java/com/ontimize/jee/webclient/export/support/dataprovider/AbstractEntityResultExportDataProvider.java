package com.ontimize.jee.webclient.export.support.dataprovider;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.webclient.export.base.ExportQueryParameters;
import com.ontimize.jee.webclient.export.providers.ExportDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class AbstractEntityResultExportDataProvider implements ExportDataProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEntityResultExportDataProvider.class);

    
    protected String service;

    protected Object serviceBean;
    protected String dao;

    protected String daoMethod;

    protected int numOfCols;

    public AbstractEntityResultExportDataProvider(final ExportQueryParameters exportParam) {
        this.service = exportParam.getService();
        this.dao = exportParam.getDao();
        this.initialize(exportParam);
    }
    
    protected void initialize(final ExportQueryParameters exportParam) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.getDao()).append(getDaoMethodSuffix());
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
    public int getNumberOfRows() {
        return getEntityResult().calculateRecordNumber();
    }

    @Override
    public int getNumberOfColumns() {
        return this.numOfCols;
    }

    public Object getCellValue(final int row, final String colId) {
        Map recordValues = this.getEntityResult().getRecordValues(row);
        return recordValues.get(colId);
    }

    protected abstract String getDaoMethodSuffix();
    public abstract EntityResult getEntityResult();
    public abstract EntityResult doQuery();


}
