package com.ontimize.jee.server.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ontimize.jee.server.dao.common.INameConvention;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.dto.PreferencesParamsDto;
import com.ontimize.jee.common.dto.PreferencesParamsDto.PreferencesType;
import com.ontimize.jee.common.services.preferences.IPreferencesService;

@RestController
@RequestMapping("${ontimize.report.preferences.url:/preferences}")
public class PreferencesRestController {
    @Autowired
    private INameConvention nameConvention;
    @Qualifier("PreferencesService")
    @Autowired
    private IPreferencesService preferencesService;

    public IPreferencesService getService() {
        return this.preferencesService;
    }



    public static final String ID_QUERY = "ID";
    public static final String NAME_QUERY = "NAME";
    public static final String DESCRIPTION_QUERY = "DESCRIPTION";
    public static final String ENTITY_QUERY = "ENTITY";
    public static final String PREFERENCES_QUERY = "PREFERENCES";
    public static final String TYPE_QUERY = "TYPE";

    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityResult> savePreferences(@RequestBody PreferencesParamsDto param) {

        EntityResult res = new EntityResultMapImpl();
        if (param != null) {
            try {

                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                mapper.setSerializationInclusion(Include.NON_NULL);
                String serializedParams = mapper.writeValueAsString(param.getParams());

                Map<String, Object> attrMap = new HashMap<>();
                attrMap.put(this.nameConvention.convertName(NAME_QUERY), param.getName());
                attrMap.put(this.nameConvention.convertName(DESCRIPTION_QUERY), param.getDescription());
                attrMap.put(this.nameConvention.convertName(ENTITY_QUERY), param.getEntity() + "-" + param.getService());
                attrMap.put(this.nameConvention.convertName(PREFERENCES_QUERY), serializedParams);
                attrMap.put(this.nameConvention.convertName(TYPE_QUERY), param.getType().ordinal());

                res = preferencesService.preferenceInsert(attrMap);
                return new ResponseEntity<>(res, HttpStatus.OK);
            } catch (Exception ex) {
                res.setMessage(ex.getMessage());
                res.setCode(EntityResult.OPERATION_WRONG);
                return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            res.setCode(EntityResult.OPERATION_WRONG);
            res.setMessage("Report configuration parameters value is empty.");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/preferences", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityResult getPreferences(@RequestParam("entity") Optional<String> entity,
            @RequestParam("service") Optional<String> service, @RequestParam("type") Optional<String> type) {
        Map<String, Object> map = new HashMap<>();
        if (!entity.isEmpty() && !service.isEmpty()) {
            String entityService = entity.get() + "-" + service.get();
            map.put(this.nameConvention.convertName(ENTITY_QUERY), entityService);
        }
        if (!type.isEmpty()) {

            map.put(this.nameConvention.convertName("TYPE"), PreferencesType.valueOf(type.get()).ordinal());
        }
        List<String> attrList = new ArrayList<>();
        attrList.add(this.nameConvention.convertName(ID_QUERY));
        attrList.add(this.nameConvention.convertName(NAME_QUERY));
        attrList.add(this.nameConvention.convertName(DESCRIPTION_QUERY));
        attrList.add(this.nameConvention.convertName(ENTITY_QUERY));
        attrList.add(this.nameConvention.convertName(TYPE_QUERY));
        attrList.add(this.nameConvention.convertName(PREFERENCES_QUERY));
        return preferencesService.preferenceQuery(map, attrList);

    }

    @DeleteMapping(value = "/remove/{id}")
    public ResponseEntity<EntityResult> removePreferences(@PathVariable("id") Long id) {
        EntityResult res = new EntityResultMapImpl();
        Map<String, Object> attrMap = new HashMap<>();
        try {
            attrMap.put(this.nameConvention.convertName("ID"), id);
            this.preferencesService.preferenceDelete(attrMap);
            res.setCode(EntityResult.OPERATION_SUCCESSFUL);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            res.setCode(EntityResult.OPERATION_WRONG);
            res.setMessage(e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(value = "/update/{id}")
    public ResponseEntity<EntityResult> updatePreferences(@PathVariable("id") Long id,
            @RequestBody PreferencesParamsDto param) {
        EntityResult res = new EntityResultMapImpl();

        if (param != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                String serializedParams = mapper.writeValueAsString(param.getParams());

                Map<String, Object> attrMap = new HashMap<>();
                attrMap.put(this.nameConvention.convertName(NAME_QUERY), param.getName());
                attrMap.put(this.nameConvention.convertName(DESCRIPTION_QUERY), param.getDescription());
                attrMap.put(this.nameConvention.convertName(PREFERENCES_QUERY), serializedParams);

                Map<String, Object> attrKey = new HashMap<>();
                attrKey.put(this.nameConvention.convertName("ID"), id);
                this.preferencesService.preferenceUpdate(attrMap, attrKey);
                res.setCode(EntityResult.OPERATION_SUCCESSFUL);
                return new ResponseEntity<>(res, HttpStatus.OK);
            } catch (Exception ex) {
                res.setMessage(ex.getMessage());
                res.setCode(EntityResult.OPERATION_WRONG);
                return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            res.setCode(EntityResult.OPERATION_WRONG);
            res.setMessage("Report configuration parameters value is empty.");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
