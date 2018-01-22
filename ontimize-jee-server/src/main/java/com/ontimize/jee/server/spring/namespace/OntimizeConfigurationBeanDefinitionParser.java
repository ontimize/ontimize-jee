package com.ontimize.jee.server.spring.namespace;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.ontimize.jee.server.configuration.OntimizeConfiguration;

public class OntimizeConfigurationBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	private static final String	REMOTE_I18N				= "remote-i18n";
	private static final String	REMOTE_MAIL				= "mail";
	private static final String REMOTE_FORM_PROVIDER = "form-provider";
	private static final String	REMOTE_OPERATION_ATTR	= "remote-operation";
	private static final String	REMOTE_PREFERENCES_ATTR	= "remote-preferences";
	private static final String	SECURITY_ATTR			= "security";

	/**
	 * The bean that is created for this tag element
	 *
	 * @param element
	 *            The tag element
	 * @return A FileListFactoryBean
	 */
	@Override
	protected Class<?> getBeanClass(Element element) {
		return OntimizeConfiguration.class;
	}

	/**
	 * Called when the fileList tag is to be parsed
	 *
	 * @param element
	 *            The tag element
	 * @param ctx
	 *            The context in which the parsing is occuring
	 * @param builder
	 *            The bean definitions build to use
	 */
	@Override
	protected void doParse(Element element, ParserContext ctx, BeanDefinitionBuilder builder) {

		// We want any parsing to occur as a child of this tag so we need to make
		// a new one that has this as it's owner/parent
		ParserContext nestedCtx = new ParserContext(ctx.getReaderContext(), ctx.getDelegate(), builder.getBeanDefinition());

		// Support for remote operation
		Element remoteOperation = DomUtils.getChildElementByTagName(element, OntimizeConfigurationBeanDefinitionParser.REMOTE_OPERATION_ATTR);
		if (remoteOperation != null) {
			// Just make a new Parser for each one and let the parser do the work
			RemoteOperationBeanDefinitionParser ro = new RemoteOperationBeanDefinitionParser();
			builder.addPropertyValue("remoteOperationConfiguration", ro.parse(remoteOperation, nestedCtx));
		}

		// // Support for dms
		// Element dms = DomUtils.getChildElementByTagName(element, "dms");
		// if (dms != null) {
		// // Just make a new Parser for each one and let the parser do the work
		// DMSBeanDefinitionParser ro = new DMSBeanDefinitionParser();
		// builder.addPropertyValue("dmsConfiguration", ro.parse(dms,
		// nestedCtx));
		// }

		// Support for authentication and authorization
		Element security = DomUtils.getChildElementByTagName(element, OntimizeConfigurationBeanDefinitionParser.SECURITY_ATTR);
		if (security != null) {
			// Just make a new Parser for each one and let the parser do the work
			SecurityBeanDefinitionParser ro = new SecurityBeanDefinitionParser();
			builder.addPropertyValue("securityConfiguration", ro.parse(security, nestedCtx));
		}

		// Support for remote preferences
		Element remotePreferences = DomUtils.getChildElementByTagName(element, OntimizeConfigurationBeanDefinitionParser.REMOTE_PREFERENCES_ATTR);
		if (remotePreferences != null) {
			// Just make a new Parser for each one and let the parser do the work
			RemotePreferencesBeanDefinitionParser ro = new RemotePreferencesBeanDefinitionParser();
			builder.addPropertyValue("remotePreferencesConfiguration", ro.parse(remotePreferences, nestedCtx));
		}

		// Support for i18n
		Element remoteI18n = DomUtils.getChildElementByTagName(element, OntimizeConfigurationBeanDefinitionParser.REMOTE_I18N);
		if (remoteI18n != null) {
			// Just make a new Parser for each one and let the parser do the work
			I18nBeanDefinitionParser ro = new I18nBeanDefinitionParser();
			builder.addPropertyValue("i18nConfiguration", ro.parse(remoteI18n, nestedCtx));
		}

		// Support form provider
		Element formProvider = DomUtils.getChildElementByTagName(element, OntimizeConfigurationBeanDefinitionParser.REMOTE_FORM_PROVIDER);
		if (formProvider != null) {
			// Just make a new Parser for each one and let the parser do the
			// work
			FormProviderBeanDefinitionParser ro = new FormProviderBeanDefinitionParser();
			builder.addPropertyValue("formProviderConfiguration", ro.parse(formProvider, nestedCtx));
		}

		// Support for mail
		Element mail = DomUtils.getChildElementByTagName(element, OntimizeConfigurationBeanDefinitionParser.REMOTE_MAIL);
		if (mail != null) {
			// Just make a new Parser for each one and let the parser do the work
			MailBeanDefinitionParser ro = new MailBeanDefinitionParser();
			builder.addPropertyValue("mailConfiguration", ro.parse(mail, nestedCtx));
		}
		builder.setLazyInit(true);

		// // Support for report
		// Element report = DomUtils.getChildElementByTagName(element,
		// "report");
		// if (report != null) {
		// // Just make a new Parser for each one and let the parser do the work
		// ReportBeanDefinitionParser ro = new ReportBeanDefinitionParser();
		// builder.addPropertyValue("reportStoreConfiguration", ro.parse(report,
		// nestedCtx));
		// }
	}
}
