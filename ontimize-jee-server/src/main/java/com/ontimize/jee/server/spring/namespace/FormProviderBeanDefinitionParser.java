package com.ontimize.jee.server.spring.namespace;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.ontimize.jee.common.spring.parser.DefinitionParserUtil;
import com.ontimize.jee.server.services.formprovider.FormProviderConfiguration;
import com.ontimize.jee.server.services.formprovider.FormProviderEngine;

public class FormProviderBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	/** The Constant SCOPE. */
	private static final String SCOPE = "scope";
	private static final String FORM_PROVIDER_ENGINE_PROPERTY = "form-provider-engine";
	private static final String FORM_PROVIDER_DATABASE_CONFIGURATION_PROPERTY = "database-form-provider";
	private static final String ENGINE = "engine";

	private static final String FORM_PROVIDER_REF_REPOSITORY = "ref-form-provider-repository";
	private static final String FORM_PROVIDER_ID_COLUMN = "form-provider-id-column";
	private static final String FORM_PROVIDER_FORM_NAME_COLUMN = "form-provider-form-name-column";
	private static final String FORM_PROVIDER_FORM_XML_COLUMN = "form-provider-form-xml-column";

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#
	 * getBeanClass(org.w3c.dom.Element)
	 */
	@Override
	protected Class<?> getBeanClass(Element element) {
		return FormProviderConfiguration.class;
	}

	/**
	 * Called when the remote-form-provider tag is to be parsed.
	 *
	 * @param element
	 *            {@link Element} The tag element
	 * @param ctx
	 *            {@link ParserContext} The context in which the parsing is
	 *            occuring
	 * @param builder
	 *            {@link BeanDefinitionBuilder} The bean definitions build to
	 *            use.
	 */
	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		Element item = DomUtils.getChildElementByTagName(element, FormProviderBeanDefinitionParser.FORM_PROVIDER_ENGINE_PROPERTY);
		Element child = DomUtils.getChildElements(item).get(0);
		Object engine = null;

		if (child.getLocalName().equals(FormProviderBeanDefinitionParser.FORM_PROVIDER_DATABASE_CONFIGURATION_PROPERTY)) {
			final ParserContext nestedCtx = new ParserContext(parserContext.getReaderContext(), parserContext.getDelegate(), builder.getBeanDefinition());
			engine = new FormProviderParser().parse(child, nestedCtx);
		} else {
			engine = DefinitionParserUtil.parseNode(child, parserContext, builder.getBeanDefinition(), element.getAttribute(FormProviderBeanDefinitionParser.SCOPE), false);
		}

		builder.addPropertyValue(FormProviderBeanDefinitionParser.ENGINE, engine);
		builder.setLazyInit(true);

		super.doParse(element, parserContext, builder);
	}

	/**
	 * The class {@link FormProviderParser}
	 *
	 */
	public static class FormProviderParser extends AbstractSingleBeanDefinitionParser {

		/**
		 * The bean that is created for this tag element.
		 *
		 * @param element
		 *            The tag element
		 * @return A FileListFactoryBean
		 */
		@Override
		protected Class<?> getBeanClass(Element element) {
			return FormProviderEngine.class;
		}

		/**
		 * Called when the fileList tag is to be parsed.
		 *
		 * @param element
		 *            The tag element
		 * @param ctx
		 *            The context in which the parsing is occuring
		 * @param builder
		 *            The bean definitions build to use
		 */
		@Override
		protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
			builder.addPropertyReference("daoFormProvider", DefinitionParserUtil.nullIfEmpty(element.getAttribute(FormProviderBeanDefinitionParser.FORM_PROVIDER_REF_REPOSITORY)));

			builder.addPropertyValue("formProviderIdColumn", DefinitionParserUtil.nullIfEmpty(element.getAttribute(FormProviderBeanDefinitionParser.FORM_PROVIDER_ID_COLUMN)));
			builder.addPropertyValue("formProviderFormNameColumn",
					DefinitionParserUtil.nullIfEmpty(element.getAttribute(FormProviderBeanDefinitionParser.FORM_PROVIDER_FORM_NAME_COLUMN)));
			builder.addPropertyValue("formProviderFormXMLColumn",
					DefinitionParserUtil.nullIfEmpty(element.getAttribute(FormProviderBeanDefinitionParser.FORM_PROVIDER_FORM_XML_COLUMN)));
			builder.setLazyInit(true);
		}

	}

}
