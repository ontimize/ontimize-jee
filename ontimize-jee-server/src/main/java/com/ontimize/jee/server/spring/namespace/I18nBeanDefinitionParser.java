package com.ontimize.jee.server.spring.namespace;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.ontimize.jee.common.spring.parser.DefinitionParserUtil;
import com.ontimize.jee.server.services.i18n.DatabaseI18nEngine;
import com.ontimize.jee.server.services.i18n.I18nConfiguration;

/**
 * The Class I18nBeanDefinitionParser.
 */
public class I18nBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	/** The Constant SCOPE. */
	private static final String	SCOPE	= "scope";

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
	 */
	@Override
	protected Class<?> getBeanClass(Element element) {
		return I18nConfiguration.class;
	}

	/**
	 * Called when the remotePreferences tag is to be parsed.
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
		// Set the directory property
		Element item = DomUtils.getChildElementByTagName(element, "i18nEngine");
		Element child = DomUtils.getChildElements(item).get(0);
		Object engine = null;
		if ("databaseI18n".equals(child.getLocalName())) {
			final ParserContext nestedCtx = new ParserContext(ctx.getReaderContext(), ctx.getDelegate(), builder.getBeanDefinition());
			engine = new DatabaseI18nParser().parse(child, nestedCtx);
		} else {
			// construimos el bean que nos venga que deberia ser un II18nService
			engine = DefinitionParserUtil.parseNode(child, ctx, builder.getBeanDefinition(),
					element.getAttribute(I18nBeanDefinitionParser.SCOPE), false);
		}
		builder.addPropertyValue("engine", engine);
		builder.setLazyInit(true);
	}

	/**
	 * The Class DatabaseI18nParser.
	 */
	public static class DatabaseI18nParser extends AbstractSingleBeanDefinitionParser {

		/**
		 * The bean that is created for this tag element.
		 *
		 * @param element
		 *            The tag element
		 * @return A FileListFactoryBean
		 */
		@Override
		protected Class<?> getBeanClass(final Element element) {
			return DatabaseI18nEngine.class;
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
		protected void doParse(final Element element, final ParserContext ctx, final BeanDefinitionBuilder builder) {
			builder.addPropertyReference("daoBundles", DefinitionParserUtil.nullIfEmpty(element.getAttribute("refBundleRepository")));
			builder.addPropertyReference("daoBundleValues", DefinitionParserUtil.nullIfEmpty(element.getAttribute("refBundleValuesRepository")));

			builder.addPropertyValue("bundleKeyColumn", DefinitionParserUtil.nullIfEmpty(element.getAttribute("bundleKeyColumn")));
			builder.addPropertyValue("bundleClassNameColumn", DefinitionParserUtil.nullIfEmpty(element.getAttribute("bundleClassNameColumn")));
			builder.addPropertyValue("bundleDescriptionColumn", DefinitionParserUtil.nullIfEmpty(element.getAttribute("bundleDescriptionColumn")));
			builder.addPropertyValue("bundleValuesKeyColumn", DefinitionParserUtil.nullIfEmpty(element.getAttribute("bundleValuesKeyColumn")));
			builder.addPropertyValue("bundleValuesTextKeyColumn", DefinitionParserUtil.nullIfEmpty(element.getAttribute("bundleValuesTextKeyColumn")));
			builder.setLazyInit(true);
		}
	}
}
