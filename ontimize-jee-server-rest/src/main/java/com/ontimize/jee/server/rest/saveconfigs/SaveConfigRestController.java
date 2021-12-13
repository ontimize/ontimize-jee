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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.server.services.saveconfigs.ISaveConfiguration;

@RestController
@RequestMapping("/configuration")
public class SaveConfigRestController {

	@Autowired
	private ISaveConfiguration saveConfiguration;

	@RequestMapping(value = { "/{type}" }, method = RequestMethod.POST, 
			consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public void setConfig(@PathVariable(name = "type", required = true) final String configType,
			@RequestBody SaveConfigParameter insertParam) throws Exception {

		EntityResult eR = (EntityResult) ReflectionTools.invoke(saveConfiguration, "setConfigPreferences",
				insertParam.getUser(), configType, insertParam.getComponents());
	}

	@RequestMapping(value = { "/{type}" }, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityResult> getConfig(@PathVariable(name = "type", required = true) final String configType,
			@RequestParam(name = "user", required = true) String user) throws Exception {

		EntityResult eR = (EntityResult) ReflectionTools.invoke(saveConfiguration, "getConfigPreferences", user,
				configType);

		return new ResponseEntity<>(eR, HttpStatus.OK);
	}
}
