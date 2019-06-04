package com.ontimize.jee.server.services.mail;

import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.spring.parser.AbstractPropertyResolver;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.common.tools.ParseUtilsExtended;

/**
 * The Class SpringMailConfigurator.
 */
public class SpringMailConfigurator implements IMailConfigurator {

	/** The Constant logger. */
	private static final Logger					logger	= LoggerFactory.getLogger(SpringMailConfigurator.class);

	/** The encoding resolver. */
	private AbstractPropertyResolver<String>	encodingResolver;

	/** The host resolver. */
	private AbstractPropertyResolver<String>	hostResolver;

	/** The port resolver. */
	private AbstractPropertyResolver<String>	portResolver;

	/** The protocol resolver. */
	private AbstractPropertyResolver<String>	protocolResolver;

	/** The user resolver. */
	private AbstractPropertyResolver<String>	userResolver;

	/** The password resolver. */
	private AbstractPropertyResolver<String>	passwordResolver;

	/** The java mail properties. */
	private AbstractPropertyResolver<String>	javaMailProperties;

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.mail.IMailConfigurator#configure(com.ontimize.jee.server.services.mail.IMailEngine)
	 */
	@Override
	public void configure(IMailEngine engine) {
		CheckingTools.failIf(!(engine instanceof SpringMailEngine), "Engine is not instanceof %s", SpringMailEngine.class.getName());
		String encoding = this.getResolverValue(this.encodingResolver);
		String host = this.getResolverValue(this.hostResolver);
		String port = this.getResolverValue(this.portResolver);
		String protocol = this.getResolverValue(this.protocolResolver);
		String user = this.getResolverValue(this.userResolver);
		String pass = this.getResolverValue(this.passwordResolver);
		Properties javaMailProperties = this.resolveJavaMailProperties();

		((SpringMailEngine) engine).getMailSender().setDefaultEncoding(encoding);
		((SpringMailEngine) engine).getMailSender().setHost(host);
		((SpringMailEngine) engine).getMailSender().setPort(Integer.valueOf(port));
		((SpringMailEngine) engine).getMailSender().setProtocol(protocol);
		((SpringMailEngine) engine).getMailSender().setUsername(user);
		((SpringMailEngine) engine).getMailSender().setPassword(pass);
		((SpringMailEngine) engine).getMailSender().setJavaMailProperties(javaMailProperties);
	}

	/**
	 * Resolve java mail properties.
	 *
	 * @return the properties
	 */
	protected Properties resolveJavaMailProperties() {
		String prop = this.getResolverValue(this.javaMailProperties);
		if (prop == null) {
			return new Properties();
		}
		Map<String, String> map = ParseUtilsExtended.getMap(prop, null);
		if (map == null) {
			return new Properties();
		}
		Properties res = new Properties();
		res.putAll(map);
		return res;
	}

	/**
	 * Gets the resolver value.
	 *
	 * @param resolver
	 *            the resolver
	 * @return the resolver value
	 */
	protected String getResolverValue(AbstractPropertyResolver<String> resolver) {
		if (resolver != null) {
			try {
				return resolver.getValue();
			} catch (Exception ex) {
				SpringMailConfigurator.logger.error(null, ex);
			}
		}
		return null;
	}

	/**
	 * Gets the encoding resolver.
	 *
	 * @return the encodingResolver
	 */
	public AbstractPropertyResolver<String> getEncodingResolver() {
		return this.encodingResolver;
	}

	/**
	 * Sets the encoding resolver.
	 *
	 * @param encodingResolver
	 *            the encodingResolver to set
	 */
	public void setEncodingResolver(AbstractPropertyResolver<String> encodingResolver) {
		this.encodingResolver = encodingResolver;
	}

	/**
	 * Gets the host resolver.
	 *
	 * @return the hostResolver
	 */
	public AbstractPropertyResolver<String> getHostResolver() {
		return this.hostResolver;
	}

	/**
	 * Sets the host resolver.
	 *
	 * @param hostResolver
	 *            the hostResolver to set
	 */
	public void setHostResolver(AbstractPropertyResolver<String> hostResolver) {
		this.hostResolver = hostResolver;
	}

	/**
	 * Gets the port resolver.
	 *
	 * @return the portResolver
	 */
	public AbstractPropertyResolver<String> getPortResolver() {
		return this.portResolver;
	}

	/**
	 * Sets the port resolver.
	 *
	 * @param portResolver
	 *            the portResolver to set
	 */
	public void setPortResolver(AbstractPropertyResolver<String> portResolver) {
		this.portResolver = portResolver;
	}

	/**
	 * Gets the protocol resolver.
	 *
	 * @return the protocolResolver
	 */
	public AbstractPropertyResolver<String> getProtocolResolver() {
		return this.protocolResolver;
	}

	/**
	 * Sets the protocol resolver.
	 *
	 * @param protocolResolver
	 *            the protocolResolver to set
	 */
	public void setProtocolResolver(AbstractPropertyResolver<String> protocolResolver) {
		this.protocolResolver = protocolResolver;
	}

	/**
	 * Gets the user resolver.
	 *
	 * @return the userResolver
	 */
	public AbstractPropertyResolver<String> getUserResolver() {
		return this.userResolver;
	}

	/**
	 * Sets the user resolver.
	 *
	 * @param userResolver
	 *            the userResolver to set
	 */
	public void setUserResolver(AbstractPropertyResolver<String> userResolver) {
		this.userResolver = userResolver;
	}

	/**
	 * Gets the password resolver.
	 *
	 * @return the passwordResolver
	 */
	public AbstractPropertyResolver<String> getPasswordResolver() {
		return this.passwordResolver;
	}

	/**
	 * Sets the password resolver.
	 *
	 * @param passwordResolver
	 *            the passwordResolver to set
	 */
	public void setPasswordResolver(AbstractPropertyResolver<String> passwordResolver) {
		this.passwordResolver = passwordResolver;
	}

	/**
	 * Sets the java mail properties.
	 *
	 * @param javaMailProperties
	 *            the new java mail properties
	 */
	public void setJavaMailProperties(AbstractPropertyResolver<String> javaMailProperties) {
		this.javaMailProperties = javaMailProperties;
	}

	/**
	 * Gets the java mail properties.
	 *
	 * @return the java mail properties
	 */
	public AbstractPropertyResolver<String> getJavaMailProperties() {
		return this.javaMailProperties;
	}
}
