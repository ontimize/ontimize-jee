package com.ontimize.jee.server.spring.namespace;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ontimize.jee.common.spring.parser.DefinitionParserUtil;
import com.ontimize.jee.server.security.DatabaseRoleInformationService;
import com.ontimize.jee.server.security.DatabaseUserInformationService;
import com.ontimize.jee.server.security.DatabaseUserRoleInformationService;
import com.ontimize.jee.server.security.SecurityConfiguration;
import com.ontimize.jee.server.security.authorization.DefaultOntimizeAuthorizator;

/**
 * The Class SecurityBeanDefinitionParser.
 */
public class SecurityBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	private static final String AUTHORIZATION_PROPERTY = "authorizator";

	private static final String AUTHORIZATION = "authorization";
	private static final String USER_INFORMATION_SERVICE = "user-information-service";
	private static final String USER_ROLE_INFORMATION_SERVICE = "user-role-information-service";
	private static final String ROLE_INFORMATION_SERVICE = "role-information-service";
	private static final String SCOPE = "scope";

	private static final String DATABASE_USER_INFORMATION_SERVICE = "database-user-information-service";
	private static final String DATABASE_USER_ROLE_INFORMATION_SERVICE = "database-user-role-information-service";
	private static final String DATABASE_ROLE_INFORMATION_SERVICE = "database-role-information-service";

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#
	 * getBeanClass(org.w3c.dom.Element)
	 */
	@Override
	protected Class<?> getBeanClass(final Element element) {
		return SecurityConfiguration.class;
	}

	/**
	 * Called when the remoteOperation tag is to be parsed.
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
		final NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node item = childNodes.item(i);
			if (item instanceof Element) {
				if (SecurityBeanDefinitionParser.USER_INFORMATION_SERVICE.equals(item.getLocalName())) {
					this.doParseUserInformationService((Element) item, ctx, builder);
				} else if (SecurityBeanDefinitionParser.USER_ROLE_INFORMATION_SERVICE.equals(item.getLocalName())) {
					this.doParseUserRoleInformationService((Element) item, ctx, builder);
				} else if (SecurityBeanDefinitionParser.ROLE_INFORMATION_SERVICE.equals(item.getLocalName())) {
					this.doParseRoleInformationService((Element) item, ctx, builder);
				} else if ((SecurityBeanDefinitionParser.AUTHORIZATION.equals(item.getLocalName()))) {
					this.doParseAuthorization((Element) item, ctx, builder);
				}
			}
		}
	}

	/**
	 * Do parse user information.
	 *
	 * @param parent
	 *            the parent
	 * @param ctx
	 *            the ctx
	 * @param builder
	 *            the builder
	 */
	protected void doParseUserInformationService(final Element parent, final ParserContext ctx, final BeanDefinitionBuilder builder) {
		Object engine = null;
		final NodeList childNodes = parent.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				if (SecurityBeanDefinitionParser.DATABASE_USER_INFORMATION_SERVICE.equals(item.getLocalName())) {
					final ParserContext nestedCtx = new ParserContext(ctx.getReaderContext(), ctx.getDelegate(), builder.getBeanDefinition());
					// construimos un databaseauthenticator
					engine = new DatabaseUserInformationServiceDefinitionParser().parse((Element) item, nestedCtx);

				} else {
					// construimos el bean que nos venga que deberia ser un
					// ISecurityAuthorizator
					final Object ob = DefinitionParserUtil.parseNode(item, ctx, builder.getRawBeanDefinition(), parent.getAttribute(SecurityBeanDefinitionParser.SCOPE), false);
					if (ob != null) {
						engine = ob;
					}
				}
			}
		}
		if (engine != null) {
			ctx.getRegistry().registerBeanDefinition(DatabaseUserInformationService.BEAN_NAME, (BeanDefinition) engine);
		}
		builder.addPropertyValue(SecurityBeanDefinitionParser.USER_INFORMATION_SERVICE, engine);
	}

	/**
	 * Do parse user role information.
	 *
	 * @param parent
	 *            the parent
	 * @param ctx
	 *            the ctx
	 * @param builder
	 *            the builder
	 */
	protected void doParseUserRoleInformationService(final Element parent, final ParserContext ctx, final BeanDefinitionBuilder builder) {
		Object engine = null;
		final NodeList childNodes = parent.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				if (SecurityBeanDefinitionParser.DATABASE_USER_ROLE_INFORMATION_SERVICE.equals(item.getLocalName())) {
					final ParserContext nestedCtx = new ParserContext(ctx.getReaderContext(), ctx.getDelegate(), builder.getBeanDefinition());
					// construimos un databaseauthenticator
					engine = new DatabaseUserRoleInformationServiceDefinitionParser().parse((Element) item, nestedCtx);
				} else {
					// construimos el bean que nos venga que debería ser un
					// ISecurityAuthorizator
					final Object ob = DefinitionParserUtil.parseNode(item, ctx, builder.getRawBeanDefinition(), parent.getAttribute(SecurityBeanDefinitionParser.SCOPE), false);
					if (ob != null) {
						engine = ob;
					}
				}
			}
		}
		if (engine != null) {
			ctx.getRegistry().registerBeanDefinition(DatabaseUserRoleInformationService.BEAN_NAME, (BeanDefinition) engine);
		}
		builder.addPropertyValue(SecurityBeanDefinitionParser.USER_ROLE_INFORMATION_SERVICE, engine);
	}

	/**
	 * Do parse role information.
	 *
	 * @param parent
	 *            the parent
	 * @param ctx
	 *            the ctx
	 * @param builder
	 *            the builder
	 */
	protected void doParseRoleInformationService(final Element parent, final ParserContext ctx, final BeanDefinitionBuilder builder) {
		Object engine = null;
		final NodeList childNodes = parent.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				if (SecurityBeanDefinitionParser.DATABASE_ROLE_INFORMATION_SERVICE.equals(item.getLocalName())) {
					final ParserContext nestedCtx = new ParserContext(ctx.getReaderContext(), ctx.getDelegate(), builder.getBeanDefinition());
					// construimos un databaseauthenticator
					engine = new DatabaseRoleInformationServiceDefinitionParser().parse((Element) item, nestedCtx);
				} else {
					// construimos el bean que nos venga que debería ser un
					// ISecurityAuthorizator
					final Object ob = DefinitionParserUtil.parseNode(item, ctx, builder.getRawBeanDefinition(), parent.getAttribute(SecurityBeanDefinitionParser.SCOPE), false);
					if (ob != null) {
						engine = ob;
					}
				}
			}
		}
		if (engine != null) {
			ctx.getRegistry().registerBeanDefinition(DatabaseRoleInformationService.BEAN_NAME, (BeanDefinition) engine);
		}
		builder.addPropertyValue(SecurityBeanDefinitionParser.ROLE_INFORMATION_SERVICE, engine);
	}

	/**
	 * Do parse authorization.
	 *
	 * @param item
	 *            the item
	 * @param ctx
	 *            the ctx
	 * @param builder
	 *            the builder
	 */
	protected void doParseAuthorization(final Element parent, final ParserContext ctx, final BeanDefinitionBuilder builder) {
		Object authorizator = null;
		final NodeList childNodes = parent.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				if ("defaultAuthorizator".equals(item.getLocalName())) {
					final ParserContext nestedCtx = new ParserContext(ctx.getReaderContext(), ctx.getDelegate(), builder.getBeanDefinition());
					// construimos un databaseauthenticator
					authorizator = new DefaultAuthorizatorDefinitionParser().parse((Element) item, nestedCtx);
					break;
				} else {
					// construimos el bean que nos venga que debería ser un
					// ISecurityAuthorizator
					authorizator = DefinitionParserUtil.parseNode(item, ctx, builder.getRawBeanDefinition(), parent.getAttribute(SecurityBeanDefinitionParser.SCOPE), false);
					if (authorizator != null) {
						break;
					}
				}
			}
		}
		builder.addPropertyValue(SecurityBeanDefinitionParser.AUTHORIZATION_PROPERTY, authorizator);
	}

	/**
	 * The Class DatabaseUserInformationServiceDefinitionParser.
	 */
	public static class DatabaseUserInformationServiceDefinitionParser extends AbstractSingleBeanDefinitionParser {

		private static final String REF_USER_REPOSITORY_ATTR = "ref-user-repository";
		private static final String USER_LOGIN_COLUMN_ATTR = "user-login-column";
		private static final String USER_PASSWORD_COLUMN_ATTR = "user-password-column";
		private static final String OTHER_DATA_ATTR = "other-data";
		private static final String QUERY_ID_ATTR = "query-id";

		/**
		 * The bean that is created for this tag element.
		 *
		 * @param element
		 *            The tag element
		 * @return A FileListFactoryBean
		 */
		@Override
		protected Class<?> getBeanClass(final Element element) {
			return DatabaseUserInformationService.class;
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

			final String refUserRepository = element.getAttribute(DatabaseUserInformationServiceDefinitionParser.REF_USER_REPOSITORY_ATTR);

			builder.getRawBeanDefinition().setDependsOn(new String[] { refUserRepository });
			// builder.getRawBeanDefinition().setDependencyCheck(AbstractBeanDefinition.DEPENDENCY_CHECK_ALL);

			// Set the user repository property
			final Object val = DefinitionParserUtil.parseReferenceValue(ctx, element, DatabaseUserInformationServiceDefinitionParser.REF_USER_REPOSITORY_ATTR);
			final PropertyValue pvUserRepository = new PropertyValue("userRepository", val);
			pvUserRepository.setSource(ctx.extractSource(element));
			builder.getBeanDefinition().getPropertyValues().addPropertyValue(pvUserRepository);

			builder.addPropertyValue("userLoginColumn",
					DefinitionParserUtil.nullIfEmpty(element.getAttribute(DatabaseUserInformationServiceDefinitionParser.USER_LOGIN_COLUMN_ATTR)));
			builder.addPropertyValue("userPasswordColumn",
					DefinitionParserUtil.nullIfEmpty(element.getAttribute(DatabaseUserInformationServiceDefinitionParser.USER_PASSWORD_COLUMN_ATTR)));
			builder.addPropertyValue("userNeedCheckPassColumn", DefinitionParserUtil.nullIfEmpty(element.getAttribute("userNeedCheckPassColumn")));
			builder.addPropertyValue("userOtherDataColumns",
					DefinitionParserUtil.nullIfEmpty(element.getAttribute(DatabaseUserInformationServiceDefinitionParser.OTHER_DATA_ATTR)));
			builder.addPropertyValue("userQueryId", DefinitionParserUtil.nullIfEmpty(element.getAttribute(DatabaseUserInformationServiceDefinitionParser.QUERY_ID_ATTR)));

			// Set the scope
			builder.setScope(element.getAttribute(SecurityBeanDefinitionParser.SCOPE));
		}
	}

	/**
	 * The Class DatabaseUserInformationServiceDefinitionParser.
	 */
	public static class DatabaseUserRoleInformationServiceDefinitionParser extends AbstractSingleBeanDefinitionParser {

		private static final String REF_USER_ROLE_REPOSITORY_ATTR = "ref-user-role-repository";
		private static final String QUERY_ID_ATTR = "query-id";
		private static final String USER_LOGIN_COLUMN_ATTR = "user-login-column";
		private static final String ROLE_NAME_COLUMN_ATTR = "role-name-column";

		/**
		 * The bean that is created for this tag element.
		 *
		 * @param element
		 *            The tag element
		 * @return A FileListFactoryBean
		 */
		@Override
		protected Class<?> getBeanClass(final Element element) {
			return DatabaseUserRoleInformationService.class;
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
			final String refUserRolesRepository = element.getAttribute(DatabaseUserRoleInformationServiceDefinitionParser.REF_USER_ROLE_REPOSITORY_ATTR);

			builder.getRawBeanDefinition().setDependsOn(new String[] { refUserRolesRepository });
			builder.getRawBeanDefinition().setDependencyCheck(AbstractBeanDefinition.DEPENDENCY_CHECK_ALL);

			// Set the user roles repository property
			final Object val2 = DefinitionParserUtil.parseReferenceValue(ctx, element, DatabaseUserRoleInformationServiceDefinitionParser.REF_USER_ROLE_REPOSITORY_ATTR);
			final PropertyValue pvUserRoleRepository = new PropertyValue("userRolesRepository", val2);
			pvUserRoleRepository.setSource(ctx.extractSource(element));
			builder.getBeanDefinition().getPropertyValues().addPropertyValue(pvUserRoleRepository);

			builder.addPropertyValue("roleLoginColumn",
					DefinitionParserUtil.nullIfEmpty(element.getAttribute(DatabaseUserRoleInformationServiceDefinitionParser.USER_LOGIN_COLUMN_ATTR)));
			builder.addPropertyValue("roleNameColumn",
					DefinitionParserUtil.nullIfEmpty(element.getAttribute(DatabaseUserRoleInformationServiceDefinitionParser.ROLE_NAME_COLUMN_ATTR)));
			builder.addPropertyValue("roleQueryId", DefinitionParserUtil.nullIfEmpty(element.getAttribute(DatabaseUserRoleInformationServiceDefinitionParser.QUERY_ID_ATTR)));

			// Set the scope
			builder.setScope(element.getAttribute(SecurityBeanDefinitionParser.SCOPE));
		}

	}

	/**
	 * The Class DatabaseUserInformationServiceDefinitionParser.
	 */
	public static class DatabaseRoleInformationServiceDefinitionParser extends AbstractSingleBeanDefinitionParser {

		private static final String REF_ROLE_REPOSITORY_ATTR = "ref-role-repository";
		private static final String ROLE_NAME_COLUMN_ATTR = "role-name-column";
		private static final String SERVER_PERMISSION_QUERY_ID_ATTR = "server-permission-query-id";
		private static final String SERVER_PERMISSION_NAME_COLUMN_ATTR = "server-permission-name-column";
		private static final String CLIENT_PERMISSION_QUERY_ID_ATTR = "client-permission-query-id";
		private static final String CLIENT_PERMISSION_COLUMN_ATTR = "client-permission-column";

		/**
		 * The bean that is created for this tag element.
		 *
		 * @param element
		 *            The tag element
		 * @return A FileListFactoryBean
		 */
		@Override
		protected Class<?> getBeanClass(final Element element) {
			return DatabaseRoleInformationService.class;
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
			final String refRolesRepository = element.getAttribute(DatabaseRoleInformationServiceDefinitionParser.REF_ROLE_REPOSITORY_ATTR);
			builder.getRawBeanDefinition().setDependsOn(new String[] { refRolesRepository });

			// Set the user repository property
			final Object val = DefinitionParserUtil.parseReferenceValue(ctx, element, DatabaseRoleInformationServiceDefinitionParser.REF_ROLE_REPOSITORY_ATTR);
			final PropertyValue pvRolesRepository = new PropertyValue("profileRepository", val);
			pvRolesRepository.setSource(ctx.extractSource(element));
			builder.getBeanDefinition().getPropertyValues().addPropertyValue(pvRolesRepository);

			// No podemos coger directamente la referencia porque podemos estar
			// en otro contexto //TODO check this (we need the same reference)
			builder.addPropertyValue("roleNameColumn",
					DefinitionParserUtil.nullIfEmpty(element.getAttribute(DatabaseRoleInformationServiceDefinitionParser.ROLE_NAME_COLUMN_ATTR)));
			builder.addPropertyValue("serverPermissionKeyColumn",
					DefinitionParserUtil.nullIfEmpty(element.getAttribute(DatabaseRoleInformationServiceDefinitionParser.SERVER_PERMISSION_NAME_COLUMN_ATTR)));
			builder.addPropertyValue("clientPermissionColumn",
					DefinitionParserUtil.nullIfEmpty(element.getAttribute(DatabaseRoleInformationServiceDefinitionParser.CLIENT_PERMISSION_COLUMN_ATTR)));
			builder.addPropertyValue("serverPermissionQueryId",
					DefinitionParserUtil.nullIfEmpty(element.getAttribute(DatabaseRoleInformationServiceDefinitionParser.SERVER_PERMISSION_QUERY_ID_ATTR)));
			builder.addPropertyValue("clientPermissionQueryId",
					DefinitionParserUtil.nullIfEmpty(element.getAttribute(DatabaseRoleInformationServiceDefinitionParser.CLIENT_PERMISSION_COLUMN_ATTR)));

			// Set the scope
			builder.setScope(element.getAttribute(SecurityBeanDefinitionParser.SCOPE));
		}

	}

	public static class DefaultAuthorizatorDefinitionParser extends AbstractSingleBeanDefinitionParser {

		/**
		 * The bean that is created for this tag element.
		 *
		 * @param element
		 *            The tag element
		 * @return A FileListFactoryBean
		 */
		@Override
		protected Class<?> getBeanClass(final Element element) {
			return DefaultOntimizeAuthorizator.class;
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

			// Set the scope
			builder.setScope(element.getAttribute(SecurityBeanDefinitionParser.SCOPE));
		}

	}
}
