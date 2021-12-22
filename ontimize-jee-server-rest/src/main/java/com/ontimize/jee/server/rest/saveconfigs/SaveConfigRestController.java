package com.ontimize.jee.server.rest.saveconfigs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.server.services.saveconfigs.ISaveConfiguration;

@RestController
@RequestMapping("/configuration")
public class SaveConfigRestController {

	@Autowired
	private ISaveConfiguration saveConfig;

	@PostMapping(value = { "/{type}" }, 
			consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityResult> setConfig(@PathVariable(name = "type", required = true) final String configType,
			@RequestBody SaveConfigParameter insertParam) {
		
		try {
			EntityResult eR = (EntityResult) ReflectionTools.invoke(saveConfig, "setConfigurations",
					insertParam.getUser(), configType, insertParam.getComponents());
			return new ResponseEntity<>(eR, HttpStatus.OK);
		} catch (OntimizeJEERuntimeException e) {
			return this.processError(e);
		}
	}

	@GetMapping(value = { "/{type}" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityResult> getConfig(@PathVariable(name = "type", required = true) final String configType,
			@RequestParam(name = "user", required = true) String user) {

		try {
			EntityResult eR = (EntityResult) ReflectionTools.invoke(saveConfig, "getConfigurations", user,
					configType);
			return new ResponseEntity<>(eR, HttpStatus.OK);
		} catch (OntimizeJEERuntimeException e) {
			return this.processError(e);
		}
	}

	protected ResponseEntity<EntityResult> processError(OntimizeJEERuntimeException error) {
		EntityResult entityResult = new EntityResultMapImpl(EntityResult.OPERATION_WRONG,
				EntityResult.BEST_COMPRESSION);
		entityResult.setMessage(error.getMessage());
		return new ResponseEntity<>(entityResult, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
