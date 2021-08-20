package com.ontimize.jee.webclient.remoteconfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.exceptiontranslator.IExceptionTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.server.rest.UpdateParameter;

public abstract class RemoteConfigurationRestController<T extends IRemoteConfigurationService, S extends IRemoteConfigurationNameConverter> {

    private static final Logger logger = LoggerFactory.getLogger(RemoteConfigurationRestController.class);

    public abstract T getService();

    @Autowired(required = false)
    protected S remoteConfigurationNameConverter;

    @Autowired(required = false)
    protected IExceptionTranslator exceptionTranslator;

    @RequestMapping(path = "/search", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityResult> getUserConfiguration(@RequestBody Map<?, ?> filter) {
        try {
            this.checkRemoteConfigurationParams(filter);
            String configColumn = this.remoteConfigurationNameConverter != null ? this.remoteConfigurationNameConverter
                .getConfigurationColumn() : IRemoteConfigurationDao.DEFAULT_COLUMN_CONFIG;
            List<String> attributes = Arrays.asList(configColumn);
            EntityResult result = (EntityResult) this.getService()
                .remoteConfigurationQuery(filter,
                        attributes);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception error) {
            return this.processError(error);
        }
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityResult> createUserConfiguration(@RequestBody Map<?, ?> values) {
        try {
            this.checkRemoteConfigurationParams(values);
            EntityResult result = (EntityResult) this.getService().remoteConfigurationInsert(values);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return this.processError(e);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityResult> updateUserConfiguration(@RequestBody UpdateParameter updateParameter) {
        try {
            Map<Object, Object> keysValues = updateParameter.getFilter();
            this.checkRemoteConfigurationParams(keysValues);
            Map<Object, Object> attributesValues = updateParameter.getData();

            // Insert the provided configuration in case there is no configuration persisted previously for the
            // provided keys
            String configColumn = this.remoteConfigurationNameConverter != null ? this.remoteConfigurationNameConverter
                .getConfigurationColumn() : IRemoteConfigurationDao.DEFAULT_COLUMN_CONFIG;
            List<String> attributes = Arrays.asList(configColumn);
            EntityResult result = (EntityResult) this.getService().remoteConfigurationQuery(keysValues, attributes);
            if (EntityResult.OPERATION_WRONG != result.getCode() && result.calculateRecordNumber() == 0) {
                attributesValues.putAll(keysValues);
                result = (EntityResult) this.getService().remoteConfigurationInsert(attributesValues);
            } else {
                result = (EntityResult) this.getService().remoteConfigurationUpdate(attributesValues, keysValues);
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return this.processError(e);
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityResult> delete(@RequestBody Map<?, ?> values) {
        try {
            this.checkRemoteConfigurationParams(values);
            EntityResult result = (EntityResult) this.getService().remoteConfigurationDelete(values);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return this.processError(e);
        }
    }

    protected void checkRemoteConfigurationParams(Map<?, ?> values) {
        String userColumn = this.remoteConfigurationNameConverter != null
                ? this.remoteConfigurationNameConverter.getUserColumn() : IRemoteConfigurationDao.DEFAULT_COLUMN_USER;
        CheckingTools.failIf(!values.containsKey(userColumn), OntimizeJEERuntimeException.class,
                "ERROR_PARAM_" + userColumn + "_NOT_FOUND");
        String appColumn = this.remoteConfigurationNameConverter != null
                ? this.remoteConfigurationNameConverter.getAppColumn() : IRemoteConfigurationDao.DEFAULT_COLUMN_APP;
        CheckingTools.failIf(!values.containsKey(appColumn), OntimizeJEERuntimeException.class,
                "ERROR_PARAM_" + appColumn + "_NOT_FOUND");
    }

    protected ResponseEntity<EntityResult> processError(Exception error) {
        RemoteConfigurationRestController.logger.error("{}", error.getMessage(), error);
        EntityResult entityResult = new EntityResultMapImpl(EntityResult.OPERATION_WRONG,
                EntityResult.BEST_COMPRESSION);
        entityResult.setMessage(this.getErrorMessage(error));
        return new ResponseEntity<>(entityResult, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected String getErrorMessage(Exception error) {
        if (this.exceptionTranslator != null) {
            if (error.getCause() != null) {
                return this.exceptionTranslator.translateException(error.getCause()).getMessage();
            }
            return this.exceptionTranslator.translateException(error).getMessage();
        }
        if (error.getCause() != null) {
            return error.getCause().getMessage();
        }
        return error.getMessage();
    }

}
