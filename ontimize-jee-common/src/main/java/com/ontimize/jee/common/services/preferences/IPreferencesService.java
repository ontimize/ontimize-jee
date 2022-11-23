package com.ontimize.jee.common.services.preferences;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

import java.util.List;
import java.util.Map;

public interface IPreferencesService {

    // PREFERENCES
    public EntityResult preferenceQuery(Map<String, Object> keyMap, List<String> attrList) throws OntimizeJEERuntimeException;

    public EntityResult preferenceInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;

    public EntityResult preferenceUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap) throws OntimizeJEERuntimeException;

    public EntityResult preferenceDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException;

}