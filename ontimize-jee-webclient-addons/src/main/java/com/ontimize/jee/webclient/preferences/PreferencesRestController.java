package com.ontimize.jee.webclient.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;

@RestController
@RequestMapping("${ontimize.report.preferences.url:/preferences}")
public class PreferencesRestController {
    @Qualifier("PreferencesService")
    @Autowired
    private IPreferencesService preferencesService;

    public IPreferencesService getService() {
        return this.preferencesService;
    }

    public enum PreferencesType {
        REPORT, CHART
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityResult> savePreferences(@RequestBody PreferencesParamsDto param) {

        EntityResult res = new EntityResultMapImpl();
        if (param != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                mapper.setSerializationInclusion(Include.NON_NULL);
                String serializedParams = mapper.writeValueAsString(param.getParams());
                int type;

                Map<String, Object> attrMap = new HashMap<>();
                attrMap.put("NAME", param.getName());
                attrMap.put("DESCRIPTION", param.getDescription());
                attrMap.put("ENTITY", param.getEntity() + "-" + param.getService());
                attrMap.put("PREFERENCES", serializedParams);
                attrMap.put("TYPE", param.getType().ordinal());

                res = preferencesService.preferenceInsert(attrMap);
                return new ResponseEntity<EntityResult>(res, HttpStatus.OK);
            } catch (Exception ex) {
                res.setMessage(ex.getMessage());
                res.setCode(EntityResult.OPERATION_WRONG);
                return new ResponseEntity<EntityResult>(res, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            res.setCode(EntityResult.OPERATION_WRONG);
            res.setMessage("Report configuration parameters value is empty.");
            return new ResponseEntity<EntityResult>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/preferences", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityResult getPreferences(@RequestParam("entity") Optional<String> entity,
            @RequestParam("service") Optional<String> service, @RequestParam("type") Optional<String> type) {
        Map<String, Object> map = new HashMap<>();
        if (!entity.isEmpty() && !service.isEmpty()) {
            String entityService = entity.get() + "-" + service.get();
            map.put("ENTITY", entityService);
        }
        if (!type.isEmpty()) {

            map.put("TYPE", PreferencesType.valueOf(type.get()).ordinal());
        }
        List<String> attrList = new ArrayList<>();
        attrList.add("ID");
        attrList.add("NAME");
        attrList.add("DESCRIPTION");
        attrList.add("ENTITY");
        attrList.add("TYPE");
        attrList.add("PREFERENCES");
        EntityResult res = preferencesService.preferenceQuery(map, attrList);
        return res;
    }

    @RequestMapping(value = "/remove/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<EntityResult> removePreferences(@PathVariable("id") Long id) {
        EntityResult res = new EntityResultMapImpl();
        Map<String, Object> attrMap = new HashMap<>();
        try {
            attrMap.put("ID", id);
            this.preferencesService.preferenceDelete(attrMap);
            res.setCode(EntityResult.OPERATION_SUCCESSFUL);
            return new ResponseEntity<EntityResult>(res, HttpStatus.OK);
        } catch (Exception e) {
            res.setCode(EntityResult.OPERATION_WRONG);
            res.setMessage(e.getMessage());
            return new ResponseEntity<EntityResult>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/update/{id}", method = RequestMethod.PUT)
    public ResponseEntity<EntityResult> updatePreferences(@PathVariable("id") Long id,
            @RequestBody PreferencesParamsDto param) {
        EntityResult res = new EntityResultMapImpl();

        if (param != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                String serializedParams = mapper.writeValueAsString(param.getParams());

                Map<String, Object> attrMap = new HashMap<>();
                attrMap.put("NAME", param.getName());
                attrMap.put("DESCRIPTION", param.getDescription());
                attrMap.put("PREFERENCES", serializedParams);

                Map<String, Object> attrKey = new HashMap<>();
                attrKey.put("ID", id);
                this.preferencesService.preferenceUpdate(attrMap, attrKey);
                res.setCode(EntityResult.OPERATION_SUCCESSFUL);
                return new ResponseEntity<EntityResult>(res, HttpStatus.OK);
            } catch (Exception ex) {
                res.setMessage(ex.getMessage());
                res.setCode(EntityResult.OPERATION_WRONG);
                return new ResponseEntity<EntityResult>(res, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            res.setCode(EntityResult.OPERATION_WRONG);
            res.setMessage("Report configuration parameters value is empty.");
            return new ResponseEntity<EntityResult>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
