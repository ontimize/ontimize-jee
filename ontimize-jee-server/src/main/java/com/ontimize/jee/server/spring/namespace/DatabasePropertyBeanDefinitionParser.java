package com.ontimize.jee.server.spring.namespace;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.ontimize.jee.common.spring.parser.DefinitionParserUtil;
import com.ontimize.jee.server.spring.DatabasePropertyResolver;

/**
 * The Class DatabasePropertyBeanDefinitionParser.
 */
public class DatabasePropertyBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	/**
	 * The Constructor.
	 */
	public DatabasePropertyBeanDefinitionParser() {
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
		return DatabasePropertyResolver.class;
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
		builder.addPropertyReference("dao", DefinitionParserUtil.nullIfEmpty(element.getAttribute("refRepository")));
		builder.addPropertyValue("valueColumnName", DefinitionParserUtil.nullIfEmpty(element.getAttribute("valueColumnName")));
		builder.addPropertyValue("filterColumnName", DefinitionParserUtil.nullIfEmpty(element.getAttribute("filterColumnName")));
		builder.addPropertyValue("filterColumnValue", DefinitionParserUtil.nullIfEmpty(element.getAttribute("filterColumnValue")));
		builder.addPropertyValue("queryId", DefinitionParserUtil.nullIfEmpty(element.getAttribute("queryId")));
	}
}
