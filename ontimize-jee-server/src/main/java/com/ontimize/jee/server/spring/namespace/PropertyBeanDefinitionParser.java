package com.ontimize.jee.server.spring.namespace;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.ontimize.jee.common.spring.parser.DefinitionParserUtil;
import com.ontimize.jee.server.spring.PropertyResolver;

/**
 * The Class PropertyBeanDefinitionParser.
 */
public class PropertyBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    /**
     * The Constructor.
     */
    public PropertyBeanDefinitionParser() {
        super();
    }

    /**
     * The bean that is created for this tag element.
     * @param element The tag element
     * @return A FileListFactoryBean
     */
    @Override
    protected Class<?> getBeanClass(final Element element) {
        return PropertyResolver.class;
    }

    /**
     * Called when the fileList tag is to be parsed.
     * @param element The tag element
     * @param ctx The context in which the parsing is occuring
     * @param builder The bean definitions build to use
     */
    @Override
    protected void doParse(final Element element, final ParserContext ctx, final BeanDefinitionBuilder builder) {
        builder.addPropertyReference("properties",
                DefinitionParserUtil.nullIfEmpty(element.getAttribute("properties")));
        builder.addPropertyValue("property", DefinitionParserUtil.nullIfEmpty(element.getAttribute("property")));
        builder.setLazyInit(true);
    }

}
