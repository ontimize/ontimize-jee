package com.ontimize.jee.server.spring.namespace;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.ontimize.jee.common.spring.parser.DefinitionParserUtil;
import com.ontimize.jee.server.services.preferences.DatabaseRemoteApplicationPreferencesEngine;
import com.ontimize.jee.server.services.preferences.FileRemoteApplicationPreferencesEngine;
import com.ontimize.jee.server.services.preferences.RemotePreferencesConfiguration;

/**
 * The Class RemotePreferencesBeanDefinitionParser.
 */
public class RemotePreferencesBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	/** The Constant SCOPE. */
	private static final String	SCOPE	= "scope";
	private static final String ENGINE = "engine";
	private static final String PREFERENCES_FILE_RPE = "file-remote-preference-engine";
	private static final String PREFERENCES_DATABASE_RPE = "database-remote-preference-engine";
	private static final String PREFERENCES_PATH = "path";
	private static final String PREFERENCES_FILEPATH = "filePath";
	private static final String PREFERENCES_REF_REPOSITORY = "ref-repository";
	private static final String PREFERENCES_USER_COLUMN_NAME = "user-column-name";
	private static final String PREFERENCES_PREF_NAME_COLUMN_NAME = "preference-name-column-name";
	private static final String PREFERENCES_PREF_VALUE_COLUMN_NAME = "preference-value-column-name";
	private static final String PREFERENCES_QUERY_ID = "query-id";

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
	 */
	@Override
	protected Class<?> getBeanClass(Element element) {
		return RemotePreferencesConfiguration.class;
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
		Element child = DomUtils.getChildElements(element).get(0);
		Object engine = null;
		if (RemotePreferencesBeanDefinitionParser.PREFERENCES_FILE_RPE.equals(child.getLocalName())) {
			final ParserContext nestedCtx = new ParserContext(ctx.getReaderContext(), ctx.getDelegate(), builder.getBeanDefinition());
			engine = new FileRemotePreferencesParser().parse(child, nestedCtx);
		} else if (RemotePreferencesBeanDefinitionParser.PREFERENCES_DATABASE_RPE.equals(child.getLocalName())) {
			final ParserContext nestedCtx = new ParserContext(ctx.getReaderContext(), ctx.getDelegate(), builder.getBeanDefinition());
			engine = new DatabaseRemotePreferencesParser().parse(child, nestedCtx);
		} else {
			// construimos el bean que nos venga que deberia ser un IRemoteApplicationPreferencesEngine
			engine = DefinitionParserUtil.parseNode(child, ctx, builder.getBeanDefinition(),
					element.getAttribute(RemotePreferencesBeanDefinitionParser.SCOPE), false);
		}
		builder.addPropertyValue(RemotePreferencesBeanDefinitionParser.ENGINE, engine);
		builder.setLazyInit(true);
	}

	/**
	 * The Class FileRemotePreferencesParser.
	 */
	public static class FileRemotePreferencesParser extends AbstractSingleBeanDefinitionParser {

		/**
		 * The bean that is created for this tag element.
		 *
		 * @param element
		 *            The tag element
		 * @return A FileListFactoryBean
		 */
		@Override
		protected Class<?> getBeanClass(final Element element) {
			return FileRemoteApplicationPreferencesEngine.class;
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
			// DefinitionParserUtil.parsePropertyResolverProperty(ctx, builder, element, "path", "filePath");

			builder.addPropertyValue(RemotePreferencesBeanDefinitionParser.PREFERENCES_PATH,
					DefinitionParserUtil.nullIfEmpty(element.getAttribute(RemotePreferencesBeanDefinitionParser.PREFERENCES_FILEPATH)));
			builder.setLazyInit(true);
		}
	}

	public static class DatabaseRemotePreferencesParser extends AbstractSingleBeanDefinitionParser {

		/**
		 * The bean that is created for this tag element.
		 *
		 * @param element
		 *            The tag element
		 * @return A FileListFactoryBean
		 */
		@Override
		protected Class<?> getBeanClass(final Element element) {
			return DatabaseRemoteApplicationPreferencesEngine.class;
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
			builder.addPropertyReference("dao", DefinitionParserUtil.nullIfEmpty(element.getAttribute(RemotePreferencesBeanDefinitionParser.PREFERENCES_REF_REPOSITORY)));
			builder.addPropertyValue("userColumnName", DefinitionParserUtil.nullIfEmpty(element.getAttribute(RemotePreferencesBeanDefinitionParser.PREFERENCES_USER_COLUMN_NAME)));
			builder.addPropertyValue("preferenceNameColumnName",
					DefinitionParserUtil.nullIfEmpty(element.getAttribute(RemotePreferencesBeanDefinitionParser.PREFERENCES_PREF_NAME_COLUMN_NAME)));
			builder.addPropertyValue("preferenceValueColumnName",
					DefinitionParserUtil.nullIfEmpty(element.getAttribute(RemotePreferencesBeanDefinitionParser.PREFERENCES_PREF_VALUE_COLUMN_NAME)));
			builder.addPropertyValue("queryId", DefinitionParserUtil.nullIfEmpty(element.getAttribute(RemotePreferencesBeanDefinitionParser.PREFERENCES_QUERY_ID)));
			builder.setLazyInit(true);
		}
	}
}
