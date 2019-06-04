package com.ontimize.jee.server.services.formprovider;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ontimize.jee.common.security.PermissionsProviderSecured;
import com.ontimize.jee.common.services.formprovider.IFormProviderService;
import com.ontimize.jee.server.configuration.OntimizeConfiguration;

@Service("FormProviderService")
public class FormProviderServiceImpl implements IFormProviderService, ApplicationContextAware {

	/** The engine. */
	private IFormProviderService engine;

	/**
	 * The Constructor.
	 */
	public FormProviderServiceImpl() {
		super();
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public String getXMLForm(String form) throws Exception {
		return this.getEngine().getXMLForm(form);
	}

	/**
	 * Gets the {@link IFormProviderService} engine
	 *
	 * @return the {@link IFormProviderService} engine
	 */
	public IFormProviderService getEngine() {
		return this.engine;
	}

	/**
	 * Sets the engine
	 *
	 * @param engine
	 *            {@link IFormProviderService} the engine
	 */
	public void setEngine(IFormProviderService engine) {
		this.engine = engine;
	}

	/**
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.setEngine(applicationContext.getBean(OntimizeConfiguration.class).getFormProviderConfiguration().getEngine());
	}

}
