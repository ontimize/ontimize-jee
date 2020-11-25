package com.ontimize.jee.webclient.excelexport.executor.support.dataprovider;

import java.util.Enumeration;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.db.EntityResult;
import com.ontimize.jee.server.rest.QueryParameter;
import com.ontimize.jee.webclient.excelexport.HeadExportColumn;
import com.ontimize.jee.webclient.excelexport.providers.ExcelExportDataProvider;

public class DefaultEntityResultExcelExportDataProvider  implements ExcelExportDataProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEntityResultExcelExportDataProvider.class);
    EntityResult entityResult;

    public DefaultEntityResultExcelExportDataProvider(EntityResult entityResult) {
        this.entityResult = entityResult;
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
    public int getColumnIndex(HeadExportColumn column) {
        return this.entityResult.getOrderColumns().indexOf(column.getId());
    }

    @Override
    public Object getCellValue(final int row, final int column) {
        Hashtable record = entityResult.getRecordValues(row);
        int n = 0;
        Enumeration i = record.keys();
        Object ret= null;
        while(i.hasMoreElements()){
            ret = record.get(i.nextElement());
            if(n==column){
                return ret;
            }
            n++;
        }
        return ret;
    }

	@Override
	public String getService() {
		return String.valueOf(entityResult.get("service"));
	}

	@Override
	public String getDao() {
		return String.valueOf(entityResult.get("dao"));
	}

	@Override
	public QueryParameter getQueryParameters() {
		return (QueryParameter)entityResult.get("queryParameters");
	}

}
