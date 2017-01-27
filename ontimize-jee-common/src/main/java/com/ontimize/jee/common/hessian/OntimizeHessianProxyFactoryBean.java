package com.ontimize.jee.common.hessian;

import org.springframework.remoting.caucho.HessianProxyFactoryBean;

/**
 * Se encarga de anteponer el host del servidor donde se va a desplegar el servidor en función de una propiedad del sistema. La propiedad será:
 * com.ontimize.services.baseUrl Es decir, la url del servicio será: <pre> serviceUrl =
 * System.getProperty("com.ontimize.services.baseUrl")+serviceRelativeUrl </pre>
 *
 * @author joaquin.romero
 *
 */
public class OntimizeHessianProxyFactoryBean extends HessianProxyFactoryBean {

	public static final String	SERVICES_BASE_URL	= "com.ontimize.services.baseUrl";
	/** The service relative url. */
	protected String	serviceRelativeUrl;

	/**
	 * Instantiates a new ontimize hessian proxy factory bean.
	 */
	public OntimizeHessianProxyFactoryBean() {
		super();
	}

	/**
	 * Sets the service relative url.
	 *
	 * @param serviceRelativeUrl
	 *            the new service relative url
	 */
	public void setServiceRelativeUrl(String serviceRelativeUrl) {
		this.serviceRelativeUrl = serviceRelativeUrl;
	}

	/**
	 * Gets the service relative url.
	 *
	 * @return the service relative url
	 */
	public String getServiceRelativeUrl() {
		return this.serviceRelativeUrl;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.remoting.caucho.HessianProxyFactoryBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		this.setHessian2Reply(true);
		this.setHessian2Request(true);
		if ((this.getServiceRelativeUrl() != null) && (this.getServiceRelativeUrl().length() > 0)) {
			String base = System.getProperty(OntimizeHessianProxyFactoryBean.SERVICES_BASE_URL);
			if (base != null) {
				if ((base.charAt(base.length() - 1) != '/') && (this.getServiceRelativeUrl().charAt(0) != '/')) {
					base = base + '/';
				}
				this.setServiceUrl(base + this.getServiceRelativeUrl());
			}
		}
		super.afterPropertiesSet();
	}
}
