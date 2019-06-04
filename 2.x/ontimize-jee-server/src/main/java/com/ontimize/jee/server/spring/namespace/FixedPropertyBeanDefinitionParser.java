package com.ontimize.jee.server.spring.namespace;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.ontimize.jee.common.spring.parser.DefinitionParserUtil;
import com.ontimize.jee.common.spring.parser.FixedPropertyResolver;

/**
 * The Class FixedPropertyBeanDefinitionParser.
 */
public class FixedPropertyBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	/**
	 * The Constructor.
	 */
	public FixedPropertyBeanDefinitionParser() {
		super();
	}

	/**
	 * The bean that is created for this tag element.
	 *
	 * @param element
	 *            The tag element
	 * @return A FileListFactoryBean
	 */
	@Override
	protected Class<?> getBeanClass(final Element element) {
		return FixedPropertyResolver.class;
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
		builder.addPropertyValue("value", DefinitionParserUtil.nullIfEmpty(element.getAttribute("value")));
		builder.setLazyInit(true);
	}
}
