package com.ontimize.jee.server.spring.namespace;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.ontimize.jee.server.services.remoteoperation.RemoteOperationConfiguration;

/**
 * The Class RemoteOperationBeanDefinitionParser.
 */
public class RemoteOperationBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final String REMOTE_OP_MAX_PARALLEL_THREAD = "max-parallel-threads";

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom
     * .Element)
     */
    @Override
    protected Class<?> getBeanClass(Element element) {
        return RemoteOperationConfiguration.class;
    }

    /**
     * Called when the remoteOperation tag is to be parsed.
     * @param element The tag element
     * @param ctx The context in which the parsing is occuring
     * @param builder The bean definitions build to use
     */
    @Override
    protected void doParse(Element element, ParserContext ctx, BeanDefinitionBuilder builder) {
        // Set the directory property
        builder.addPropertyValue("maxParallelThreads",
                element.getAttribute(RemoteOperationBeanDefinitionParser.REMOTE_OP_MAX_PARALLEL_THREAD));
        builder.setLazyInit(true);
        // TODO permitir configurar el manager de operaciones remotas
    }

}
