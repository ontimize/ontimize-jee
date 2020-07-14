package com.ontimize.jee.server.spring.namespace;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.ontimize.jee.common.spring.parser.DefinitionParserUtil;
import com.ontimize.jee.server.services.mail.MailConfiguration;
import com.ontimize.jee.server.services.mail.SpringMailConfigurator;
import com.ontimize.jee.server.services.mail.SpringMailEngine;
import com.ontimize.jee.server.spring.DatabasePropertyResolver;

/**
 * The Class RemotePreferencesBeanDefinitionParser.
 */
public class MailBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    /** The Constant SCOPE. */
    private static final String SCOPE = "scope";

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom
     * .Element)
     */
    @Override
    protected Class<?> getBeanClass(Element element) {
        return MailConfiguration.class;
    }

    /**
     * Called when the remotePreferences tag is to be parsed.
     * @param element The tag element
     * @param ctx The context in which the parsing is occuring
     * @param builder The bean definitions build to use
     */
    @Override
    protected void doParse(Element element, ParserContext ctx, BeanDefinitionBuilder builder) {
        Element child = DomUtils.getChildElements(element).get(0);
        Object engine = null;
        if ("default-mail-engine".equals(child.getLocalName())) {
            final ParserContext nestedCtx = new ParserContext(ctx.getReaderContext(), ctx.getDelegate(),
                    builder.getBeanDefinition());
            engine = new SpringMailEngineParser().parse(child, nestedCtx);
        } else {
            // construimos el bean que nos venga que deberia ser un IMailEngine
            engine = DefinitionParserUtil.parseNode(child, ctx, builder.getBeanDefinition(),
                    element.getAttribute(MailBeanDefinitionParser.SCOPE), false);
        }
        builder.addPropertyValue("engine", engine);
        builder.setLazyInit(true);
    }

    public static class SpringMailEngineParser extends AbstractSingleBeanDefinitionParser {

        /**
         * The bean that is created for this tag element.
         * @param element The tag element
         * @return A FileListFactoryBean
         */
        @Override
        protected Class<?> getBeanClass(final Element element) {
            return SpringMailEngine.class;
        }

        /**
         * Called when the fileList tag is to be parsed.
         * @param element The tag element
         * @param ctx The context in which the parsing is occuring
         * @param builder The bean definitions build to use
         */
        @Override
        protected void doParse(final Element element, final ParserContext ctx, final BeanDefinitionBuilder builder) {
            Element child = DomUtils.getChildElements(element).get(0);
            Object configurator = null;
            if ("default-mail-configurator".equals(child.getLocalName())) {
                final ParserContext nestedCtx = new ParserContext(ctx.getReaderContext(), ctx.getDelegate(),
                        builder.getBeanDefinition());
                configurator = new SpringMailEngineConfiguratorParser().parse(child, nestedCtx);
            } else {
                // construimos el bean que nos venga que deberia ser un IRemoteApplicationPreferencesEngine
                configurator = DefinitionParserUtil.parseNode(child, ctx, builder.getBeanDefinition(),
                        element.getAttribute(MailBeanDefinitionParser.SCOPE), false);
            }
            builder.addPropertyValue("configurator", configurator);
            builder.setLazyInit(true);
        }

    }

    public static class SpringMailEngineConfiguratorParser extends AbstractSingleBeanDefinitionParser {

        /**
         * The bean that is created for this tag element.
         * @param element The tag element
         * @return A FileListFactoryBean
         */
        @Override
        protected Class<?> getBeanClass(final Element element) {
            return SpringMailConfigurator.class;
        }

        /**
         * Called when the fileList tag is to be parsed.
         * @param element The tag element
         * @param ctx The context in which the parsing is occuring
         * @param builder The bean definitions build to use
         */
        @Override
        protected void doParse(final Element element, final ParserContext ctx, final BeanDefinitionBuilder builder) {
            String filterColumnName = DefinitionParserUtil.nullIfEmpty(element.getAttribute("filter-column-name"));
            String valueColumnName = DefinitionParserUtil.nullIfEmpty(element.getAttribute("value-column-name"));
            String refRepository = DefinitionParserUtil.nullIfEmpty(element.getAttribute("ref-repository"));
            String queryId = DefinitionParserUtil.nullIfEmpty(element.getAttribute("query-id"));
            String filterColumnValueEncoding = DefinitionParserUtil
                .nullIfEmpty(element.getAttribute("filter-column-value-encoding"));
            String filterColumnValueHost = DefinitionParserUtil
                .nullIfEmpty(element.getAttribute("filter-column-value-host"));
            String filterColumnValuePort = DefinitionParserUtil
                .nullIfEmpty(element.getAttribute("filter-column-value-port"));
            String filterColumnValueProtocol = DefinitionParserUtil
                .nullIfEmpty(element.getAttribute("filter-column-value-protocol"));
            String filterColumnValueUser = DefinitionParserUtil
                .nullIfEmpty(element.getAttribute("filter-column-value-user"));
            String filterColumnValuePassword = DefinitionParserUtil
                .nullIfEmpty(element.getAttribute("filter-column-value-password"));
            String filterColumnValueJavaMailProperties = DefinitionParserUtil
                .nullIfEmpty(element.getAttribute("filter-column-value-javamail-properties"));

            Map<String, String> propertiesToSet = new HashMap<>();
            propertiesToSet.put("encodingResolver", filterColumnValueEncoding);
            propertiesToSet.put("hostResolver", filterColumnValueHost);
            propertiesToSet.put("portResolver", filterColumnValuePort);
            propertiesToSet.put("protocolResolver", filterColumnValueProtocol);
            propertiesToSet.put("userResolver", filterColumnValueUser);
            propertiesToSet.put("passwordResolver", filterColumnValuePassword);
            propertiesToSet.put("javaMailProperties", filterColumnValueJavaMailProperties);
            final ParserContext nestedCtx = new ParserContext(ctx.getReaderContext(), ctx.getDelegate(),
                    builder.getBeanDefinition());
            for (Entry<String, String> entry : propertiesToSet.entrySet()) {
                if ((entry.getKey() != null) && (entry.getValue() != null)) {
                    builder.addPropertyValue(entry.getKey(),
                            new DatabasePropertyResolverParser(filterColumnName, valueColumnName, refRepository,
                                    queryId, entry.getValue()).parse(element, nestedCtx));
                }
            }
            builder.setLazyInit(true);
        }

    }

    public static class DatabasePropertyResolverParser extends AbstractSingleBeanDefinitionParser {

        private final String filterColumnName;

        private final String valueColumnName;

        private final String refRepository;

        private final String queryId;

        private final String filterColumnValue;

        public DatabasePropertyResolverParser(String filterColumnName, String valueColumnName, String refRepository,
                String queryId, String filterColumnValue) {
            super();
            this.filterColumnName = filterColumnName;
            this.valueColumnName = valueColumnName;
            this.refRepository = refRepository;
            this.filterColumnValue = filterColumnValue;
            this.queryId = queryId;
        }

        /**
         * The bean that is created for this tag element.
         * @param element The tag element
         * @return A FileListFactoryBean
         */
        @Override
        protected Class<?> getBeanClass(final Element element) {
            return DatabasePropertyResolver.class;
        }

        /**
         * Called when the fileList tag is to be parsed.
         * @param element The tag element
         * @param ctx The context in which the parsing is occuring
         * @param builder The bean definitions build to use
         */
        @Override
        protected void doParse(final Element element, final ParserContext ctx, final BeanDefinitionBuilder builder) {
            builder.addPropertyReference("dao", DefinitionParserUtil.nullIfEmpty(this.refRepository));
            builder.addPropertyValue("valueColumnName", DefinitionParserUtil.nullIfEmpty(this.valueColumnName));
            builder.addPropertyValue("filterColumnName", DefinitionParserUtil.nullIfEmpty(this.filterColumnName));
            builder.addPropertyValue("filterColumnValue", DefinitionParserUtil.nullIfEmpty(this.filterColumnValue));
            builder.addPropertyValue("queryId", DefinitionParserUtil.nullIfEmpty(this.queryId));
            builder.addPropertyValue("useMyselfInSpringContext", true);
            builder.setLazyInit(true);
        }

    }

}
