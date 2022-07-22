package com.ontimize.jee.webclient.export.support.dataprovider;

import com.ontimize.jee.common.db.AdvancedEntityResult;
import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.server.rest.AdvancedQueryParameter;
import com.ontimize.jee.server.rest.ORestController;
import com.ontimize.jee.webclient.export.base.ExportQueryParameters;
import com.ontimize.jee.webclient.export.providers.ExportDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DefaultAdvancedEntityResultExportDataProvider implements ExportDataProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAdvancedEntityResultExportDataProvider.class);

    AdvancedEntityResult entityResult;
    
    private String service;
    
    private Object serviceBean;
    private String dao;
    
    private String daoMethod;
    
    private int numOfCols;
    
    private int pageSize;

    private int page;
    
    private List<SQLStatementBuilder.SQLOrder> orderBy;

    private AdvancedQueryParameter queryParameters;

    public DefaultAdvancedEntityResultExportDataProvider(final ExportQueryParameters exportParam) {
        this.service = exportParam.getService();
        this.dao = exportParam.getDao();
        
        this.numOfCols = exportParam.getQueryParam().getColumns().size();
        
        this.initialize(exportParam);
    }
    
    protected void initialize(final ExportQueryParameters exportParam) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.getDao()).append(ORestController.PAGINATION_QUERY);
        this.daoMethod = buffer.toString();
        
        if(exportParam.getQueryParam() instanceof AdvancedQueryParameter) {
            this.queryParameters = (AdvancedQueryParameter) exportParam.getQueryParam();
            this.pageSize = this.queryParameters.getPageSize();
            this.page = this.queryParameters.getOffset();
            this.orderBy = this.queryParameters.getOrderBy();
        }
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
    public AdvancedQueryParameter getQueryParameters() {
        return this.queryParameters;
    }
    
    public int getPage() {
        return this.page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public List<SQLStatementBuilder.SQLOrder> getOrderBy() {
        return orderBy;
    }

    public EntityResult getEntityResult() {
        if (entityResult == null) {
            entityResult = doQuery();
        }

        return entityResult;
    }

    public AdvancedEntityResult getAdvancedEntityResult(int row) {
        if (entityResult == null) {
            entityResult = doQuery(0);
        }

        if (row >= entityResult.getStartRecordIndex() + getPageSize()) {
            entityResult = doQuery(entityResult.getStartRecordIndex() + getPageSize());
        }

        return entityResult;
    }

    @Override
    public int getNumberOfRows() {
        int page0 = getPage();
        if (page0 == -1) {
            return ((AdvancedEntityResult)getEntityResult()).getTotalRecordCount();
        } else {
            return getUniquePage(page0).calculateRecordNumber();
        }
    }

    @Override
    public int getNumberOfColumns() {
        return this.numOfCols;
    }

    public Object getCellValue(final int row, final String colId) {

        if (getPage() == -1) {
            // Query avanzada que devuelve todas las paginas
            AdvancedEntityResult data = getAdvancedEntityResult(row);
            // Cuando el numero de fila es igual al tama�o de pagina, cambia el offset a la
            // siguiente pagina
            int nRow = row % getPageSize();
            if (data != null && nRow < data.getCurrentRecordCount()) {
                return data.getRecordValues(nRow).get(colId);
            }
        } else {
            // Query avanzada que devuelve una pagina especifica
            AdvancedEntityResult data = getUniquePage(getPage());
            if (data != null && row < data.getCurrentRecordCount()) {
                return data.getRecordValues(row).get(colId);
            }
        }
        return null;
    }

    public AdvancedEntityResult getUniquePage(final int page) {
        if (this.entityResult == null) {
            entityResult = doQuery(page);
        }
        return entityResult;
    }
    
    public AdvancedEntityResult doQuery(){
        return doQuery(getPage());
    }

    public AdvancedEntityResult doQuery(final int page) {
        return (AdvancedEntityResult) ReflectionTools.invoke(this.getServiceBean(), 
            this.getDaoMethod(),
            this.getQueryParameters().getFilter(),
            this.getQueryParameters().getColumns(),
            this.getPageSize(),
            page,
            this.getOrderBy());
    }


}
