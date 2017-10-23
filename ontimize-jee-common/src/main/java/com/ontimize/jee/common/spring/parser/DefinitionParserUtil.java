package com.ontimize.jee.common.spring.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The Class DefinitionParserUtil.
 */
public final class DefinitionParserUtil {

	/**
	 * Instantiates a new definition parser util.
	 */
	private DefinitionParserUtil() {
		super();
	}

	/**
	 * Parses the children of the passed in ParentNode for the following tags: <br/> value ref idref bean property *custom* <p/>
	 *
	 * The value tag works with Spring EL even in a Spring Batch scope="step".
	 *
	 * @param node
	 *            the node
	 * @param ctx
	 *            The ParserContext to use
	 * @param parentBean
	 *            The BeanDefinition of the bean who is the parent of the parsed bean (i.e. the Bean that is the parentNode)
	 * @param scope
	 *            The scope to execute in. Checked if 'step' to provide Spring EL support in a Spring Batch env
	 * @return the object
	 */
	public static Object parseNode(Node node, ParserContext ctx, AbstractBeanDefinition parentBean, String scope) {
		return DefinitionParserUtil.parseNode(node, ctx, parentBean, scope, true);
	}

	/**
	 * returns null on empty string.
	 *
	 * @param value
	 *            the value
	 * @return the string
	 */
	public static String nullIfEmpty(String value) {
		return "".equals(value) ? null : value;
	}

	/**
	 * Parses the children of the passed in ParentNode for the following tags: <br/> value ref idref bean property *custom* <p/>
	 *
	 * The value tag works with Spring EL even in a Spring Batch scope="step".
	 *
	 * @param node
	 *            the node
	 * @param ctx
	 *            The ParserContext to use
	 * @param parentBean
	 *            The BeanDefinition of the bean who is the parent of the parsed bean (i.e. the Bean that is the parentNode)
	 * @param scope
	 *            The scope to execute in. Checked if 'step' to provide Spring EL support in a Spring Batch env
	 * @param supportCustomTags
	 *            Should we support custom tags within our tags?
	 * @return the object
	 */
	public static Object parseNode(Node node, ParserContext ctx, AbstractBeanDefinition parentBean, String scope, boolean supportCustomTags) {
		// Only worry about element nodes
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			parentBean.setDependencyCheck(AbstractBeanDefinition.DEPENDENCY_CHECK_ALL);

			Element elem = (Element) node;
			String tagName = node.getLocalName();

			if (tagName.equals("value")) {
				String val = node.getTextContent();
				// to get around an issue with Spring Batch not parsing Spring EL
				// we will do it for them
				if (scope.equals("step") && val.startsWith("#{") && val.endsWith("}") && !val.startsWith("#{jobParameters")) {
					// Set up a new EL parser
					ExpressionParser parser = new SpelExpressionParser();
					// Parse the value
					Expression exp = parser.parseExpression(val.substring(2, val.length() - 1));
					// Place the results in the list of created objects
					return exp.getValue();
				} else {
					// Otherwise, just treat it as a normal value tag
					return val;
				}
			}
			// Either of these is a just a lookup of an existing bean
			else if (tagName.equals("ref") || tagName.equals("idref")) {
				List<String> deps = new ArrayList<>();
				if (parentBean.getDependsOn() != null) {
					deps.addAll(Arrays.asList(parentBean.getDependsOn()));
				}
				deps.add(((Element) node).getAttribute("bean"));
				parentBean.setDependsOn(deps.toArray(new String[deps.size()]));
				return DefinitionParserUtil.parseReferenceValue(ctx, (Element) node, "bean");
				// return ctx.getRegistry().getBeanDefinition(node.getAttributes().getNamedItem("bean").getTextContent());
			}
			// We need to create the bean
			else if (tagName.equals("bean")) {
				BeanDefinitionHolder bdHolder = ctx.getDelegate().parseBeanDefinitionElement(elem);
				if (bdHolder != null) {
					bdHolder = ctx.getDelegate().decorateBeanDefinitionIfRequired(elem, bdHolder);
					try {
						// Register the final decorated instance.
						BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, ctx.getReaderContext().getRegistry());
					} catch (BeanDefinitionStoreException ex) {
						ctx.getReaderContext().error("Failed to register bean definition with name '" + bdHolder.getBeanName() + "'", elem, ex);
					}
					// Send registration event.
					ctx.getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
				}
				return bdHolder.getBeanDefinition();

				// // There is no quick little util I could find to create a bean
				// // on the fly programmatically in Spring and still support all
				// // Spring functionality so basically I mimic what Spring actually
				// // does but on a smaller scale. Everything Spring allows is
				// // still supported
				//
				// // Create a factory to make the bean
				// DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
				// // Set up a parser for the bean
				// BeanDefinitionParserDelegate pd = new BeanDefinitionParserDelegate(ctx.getReaderContext());
				// // Parse the bean get its information, now in a DefintionHolder
				// BeanDefinitionHolder bh = pd.parseBeanDefinitionElement(elem, parentBean);
				// // Register the bean will all the other beans Spring is aware of
				// BeanDefinitionReaderUtils.registerBeanDefinition(bh, beanFactory);
				// // Get the bean from the factory. This will allows Spring
				// // to do all its work (EL processing, scope, etc) and give us
				// // the actual bean itself
				// return beanFactory.getBean(bh.getBeanName());
			}
			/*
			 * This is handled a bit differently in that it actually sets the property on the parent bean for us based on the property
			 */
			else if (tagName.equals("property")) {
				BeanDefinitionParserDelegate pd = new BeanDefinitionParserDelegate(ctx.getReaderContext());
				// This method actually set eh property on the parentBean for us so
				// we don't have to add anything to the objects object
				pd.parsePropertyElement(elem, parentBean);
			} else if (supportCustomTags) {
				// handle custom tag
				BeanDefinitionParserDelegate pd = new BeanDefinitionParserDelegate(ctx.getReaderContext());
				return pd.parseCustomElement(elem, parentBean);
			}
		}
		return null;
	}

	/**
	 * Parses the reference value.
	 *
	 * @param ctx
	 *            the ctx
	 * @param element
	 *            the element
	 * @param referenceAttribute
	 *            the reference attribute
	 * @return the object
	 */
	public static Object parseReferenceValue(final ParserContext ctx, final Element element, final String referenceAttribute) {
		final String refName = element.getAttribute(referenceAttribute);
		if (!StringUtils.hasText(refName)) {
			ctx.getReaderContext().error(element.getNodeName() + " contains empty '" + referenceAttribute + "' attribute", element);
		}
		final RuntimeBeanReference ref = new RuntimeBeanReference(refName);
		ref.setSource(ctx.extractSource(element));
		return ref;
	}
}
