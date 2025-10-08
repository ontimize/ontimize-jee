package com.ontimize.jee.server.spring.namespace;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.ontimize.jee.common.spring.parser.DefinitionParserUtil;
import com.ontimize.jee.server.services.sharepreferences.SharePreferencesConfiguration;
import com.ontimize.jee.server.services.sharepreferences.SharePreferencesEngine;

public class SharePreferencesBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    /** The Constant SCOPE. */
    private static final String SCOPE = "scope";

    private static final String SHARE_PREF_ENGINE_PROPERTY = "share-pref-engine";

    private static final String SHARE_PREF_DATABASE_CONFIGURATION_PROPERTY = "database-share-pref";

    private static final String ENGINE = "engine";

    private static final String SHARE_PREF_REPOSITORY = "ref-share-repository";

    private static final String SHARE_PREF_TARGET_REPOSITORY = "ref-share-target-repository";

    private static final String SHARE_PREF_KEY_COLUMN = "share-key-column";

    private static final String SHARE_PREF_TARGET_KEY_COLUMN = "share-target-key-column";

    private static final String SHARE_PREF_USER_COLUMN = "share-user-column";

    private static final String SHARE_PREF_TARGET_USER_COLUMN = "share-target-user-column";

    private static final String SHARE_PREF_TYPE_COLUMN = "share-type-column";

    private static final String SHARE_PREF_CONTENT_COLUMN = "share-content-column";

    private static final String SHARE_PREF_MESSAGE_COLUMN = "share-message-column";

    private static final String SHARE_PREF_NAME_COLUMN = "share-name-column";


    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#
     * getBeanClass(org.w3c.dom.Element)
     */
    @Override
    protected Class<?> getBeanClass(Element element) {
        return SharePreferencesConfiguration.class;
    }

    /**
     * Called when the remotePreferences tag is to be parsed.
     * @param element The tag element
     * @param ctx The context in which the parsing is occuring
     * @param builder The bean definitions build to use
     */
    @Override
    protected void doParse(Element element, ParserContext ctx, BeanDefinitionBuilder builder) {
        // Set the directory property
        Element item = DomUtils.getChildElementByTagName(element,
                SharePreferencesBeanDefinitionParser.SHARE_PREF_ENGINE_PROPERTY);
        Object engine = null;
        if (item != null) {
            Element child = DomUtils.getChildElements(item).get(0);
            if (SharePreferencesBeanDefinitionParser.SHARE_PREF_DATABASE_CONFIGURATION_PROPERTY
                .equals(child.getLocalName())) {
                final ParserContext nestedCtx = new ParserContext(ctx.getReaderContext(), ctx.getDelegate(),
                        builder.getBeanDefinition());
                engine = new DatabaseSharePreferencesParser().parse(child, nestedCtx);
            } else {
                engine = DefinitionParserUtil.parseNode(child, ctx, builder.getBeanDefinition(),
                        element.getAttribute(SharePreferencesBeanDefinitionParser.SCOPE), false);
            }
        }
        builder.addPropertyValue(SharePreferencesBeanDefinitionParser.ENGINE, engine);
        builder.setLazyInit(true);
    }

    /**
     * The Class DatabaseSharePreferencesParser.
     */
    public static class DatabaseSharePreferencesParser extends AbstractSingleBeanDefinitionParser {

        /**
         * The bean that is created for this tag element.
         * @param element The tag element
         * @return A FileListFactoryBean
         */
        @Override
        protected Class<?> getBeanClass(final Element element) {
            return SharePreferencesEngine.class;
        }

        /**
         * Called when the fileList tag is to be parsed.
         * @param element The tag element
         * @param ctx The context in which the parsing is occuring
         * @param builder The bean definitions build to use
         */
        @Override
        protected void doParse(final Element element, final ParserContext ctx, final BeanDefinitionBuilder builder) {
            builder.addPropertyReference("daoSharePref", DefinitionParserUtil
                .nullIfEmpty(element.getAttribute(SharePreferencesBeanDefinitionParser.SHARE_PREF_REPOSITORY)));
            builder.addPropertyReference("daoSharePrefTarget",
                    DefinitionParserUtil.nullIfEmpty(
                            element.getAttribute(SharePreferencesBeanDefinitionParser.SHARE_PREF_TARGET_REPOSITORY)));

            builder.addPropertyValue("shareKeyColumn", DefinitionParserUtil
                .nullIfEmpty(element.getAttribute(SharePreferencesBeanDefinitionParser.SHARE_PREF_KEY_COLUMN)));
            builder.addPropertyValue("shareTargetKeyColumn",
                    DefinitionParserUtil.nullIfEmpty(
                            element.getAttribute(SharePreferencesBeanDefinitionParser.SHARE_PREF_TARGET_KEY_COLUMN)));
            builder.addPropertyValue("shareUserColumn", DefinitionParserUtil
                .nullIfEmpty(element.getAttribute(SharePreferencesBeanDefinitionParser.SHARE_PREF_USER_COLUMN)));
            builder.addPropertyValue("shareTargetUserColumn",
                    DefinitionParserUtil.nullIfEmpty(
                            element.getAttribute(SharePreferencesBeanDefinitionParser.SHARE_PREF_TARGET_USER_COLUMN)));
            builder.addPropertyValue("shareTypeColumn", DefinitionParserUtil
                .nullIfEmpty(element.getAttribute(SharePreferencesBeanDefinitionParser.SHARE_PREF_TYPE_COLUMN)));
            builder.addPropertyValue("shareContentColumn", DefinitionParserUtil
                .nullIfEmpty(element.getAttribute(SharePreferencesBeanDefinitionParser.SHARE_PREF_CONTENT_COLUMN)));
            builder.addPropertyValue("shareMessageColumn", DefinitionParserUtil
                .nullIfEmpty(element.getAttribute(SharePreferencesBeanDefinitionParser.SHARE_PREF_MESSAGE_COLUMN)));
            builder.addPropertyValue("shareNameColumn", DefinitionParserUtil
                .nullIfEmpty(element.getAttribute(SharePreferencesBeanDefinitionParser.SHARE_PREF_NAME_COLUMN)));
            builder.setLazyInit(true);
        }

    }

}
