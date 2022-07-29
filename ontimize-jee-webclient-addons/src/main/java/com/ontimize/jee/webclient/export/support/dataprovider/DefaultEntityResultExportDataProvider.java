package com.ontimize.jee.webclient.export.support.dataprovider;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.server.rest.ORestController;
import com.ontimize.jee.server.rest.QueryParameter;
import com.ontimize.jee.webclient.export.base.ExportQueryParameters;
import com.ontimize.jee.webclient.export.providers.ExportDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DefaultEntityResultExportDataProvider extends AbstractEntityResultExportDataProvider<EntityResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEntityResultExportDataProvider.class);

    EntityResult entityResult;

    private QueryParameter queryParameters;

    public DefaultEntityResultExportDataProvider(final ExportQueryParameters exportParam) {
        super(exportParam);
    }
    
    protected void initialize(final ExportQueryParameters exportParam) {
        super.initialize(exportParam);
        if(exportParam.getQueryParam() instanceof QueryParameter) {
            this.queryParameters = (QueryParameter) exportParam.getQueryParam();
            this.numOfCols = this.queryParameters.getColumns().size();
        }
    }

    @Override
    protected String getDaoMethodSuffix() {
        return ORestController.QUERY;
    }

    @Override
    public QueryParameter getQueryParameters() {
        return this.queryParameters;
    }

    @Override
    public int getNumberOfRows() {
        return getData().calculateRecordNumber();
    }

    public Object getCellValue(final int row, final String colId) {
        Map recordValues = this.getData().getRecordValues(row);
        return recordValues.get(colId);
    }
    
    @Override
    public EntityResult getData() {
        if (entityResult == null) {
            entityResult = doQuery();
        }
        return entityResult;
    }

    public EntityResult doQuery() {
        return (EntityResult) ReflectionTools.invoke(this.getServiceBean(), 
            this.getDaoMethod(),
            this.getQueryParameters().getFilter(),
            this.getQueryParameters().getColumns());
    }


}
