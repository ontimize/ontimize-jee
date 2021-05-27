package com.ontimize.jee.webclient.export.base;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * @author <a href="antonio.vazquez@imatia.com">Antonio V�zquez Ara�jo</a>
 */
public interface IExcelExportService {

    File queryParameters(EntityResult data, List<String> orderColumns, Map<Object, Object> keysValues,
            List<Object> attributesValues, int pageSize, boolean advQuery, int offSet)
            throws OntimizeJEERuntimeException, IOException;

}
