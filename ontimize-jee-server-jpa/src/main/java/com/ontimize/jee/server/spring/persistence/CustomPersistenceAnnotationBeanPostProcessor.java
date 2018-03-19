/**
 *
 */
package com.ontimize.jee.server.spring.persistence;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor;

/**
 * The Class CustomPersistenceAnnotationBeanPostProcessor.
 */
public class CustomPersistenceAnnotationBeanPostProcessor extends PersistenceAnnotationBeanPostProcessor {

	/** The Constant serialVersionUID. */
	private static final long				serialVersionUID			= 5511211197171925507L;

	/** The beans to process. */
	private List<String>					beansToProcess				= new ArrayList<>();

	private transient ListableBeanFactory	beanFactory;

	private transient String				defaultPersistenceUnitName	= "";

	@Override
	public void setBeanFactory(final BeanFactory beanFactory) {
		if (beanFactory instanceof ListableBeanFactory) {
			this.beanFactory = (ListableBeanFactory) beanFactory;
		}
		super.setBeanFactory(beanFactory);
	}

	/**
	 * Specify the default persistence unit name, to be used in case of no unit name specified in an {@code @PersistenceUnit} / {@code @PersistenceContext} annotation. <p> This is
	 * mainly intended for lookups in the application context, indicating the target persistence unit name (typically matching the bean name), but also applies to lookups in the
	 * {@link #setPersistenceUnits "persistenceUnits"} / {@link #setPersistenceContexts "persistenceContexts"} / {@link #setExtendedPersistenceContexts
	 * "extendedPersistenceContexts"} map, avoiding the need for duplicated mappings for the empty String there. <p> Default is to check for a single EntityManagerFactory bean in
	 * the Spring application context, if any. If there are multiple such factories, either specify this default persistence unit name or explicitly refer to named persistence
	 * units in your annotations.
	 */
	@Override
	public void setDefaultPersistenceUnitName(final String unitName) {
		this.defaultPersistenceUnitName = unitName != null ? unitName : "";
		super.setDefaultPersistenceUnitName(unitName);
	}

	/**
	 * Sets the beans to process.
	 *
	 * @param beansToProcess
	 *            the new beans to process
	 */
	public void setBeansToProcess(final List<String> beansToProcess) {
		this.beansToProcess = beansToProcess;
	}

	/**
	 * @see org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor#postProcessMergedBeanDefinition(org.springframework.beans.factory.support.RootBeanDefinition,
	 *      java.lang.Class, java.lang.String)
	 */
	@Override
	public void postProcessMergedBeanDefinition(final RootBeanDefinition beanDefinition, final Class<?> beanType, final String beanName) {
		if ((beanType != null) && this.procesable(beanName)) {
			super.postProcessMergedBeanDefinition(beanDefinition, beanType, beanName);
		}
	}

	/**
	 * Procesable.
	 *
	 * @param beanName
	 *            the bean name
	 * @return true, if successful
	 */
	protected boolean procesable(final String beanName) {
		return this.beansToProcess.contains(beanName);
	}

	/**
	 * @see org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor#postProcessBeforeInstantiation(java.lang.Class, java.lang.String)
	 */
	@Override
	public Object postProcessBeforeInstantiation(final Class<?> beanClass, final String beanName) throws BeansException {
		return super.postProcessBeforeInstantiation(beanClass, beanName);
	}

	/**
	 * @see org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor#postProcessAfterInstantiation(java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean postProcessAfterInstantiation(final Object bean, final String beanName) throws BeansException {
		return super.postProcessAfterInstantiation(bean, beanName);
	}

	/**
	 * @see org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor#postProcessPropertyValues(org.springframework.beans.PropertyValues,
	 *      java.beans.PropertyDescriptor[], java.lang.Object, java.lang.String)
	 */
	@Override
	public PropertyValues postProcessPropertyValues(final PropertyValues pvs, final PropertyDescriptor[] pds, final Object bean, final String beanName) throws BeansException {
		PropertyValues pvsLocal = pvs;
		if (this.procesable(beanName)) {
			pvsLocal = super.postProcessPropertyValues(pvsLocal, pds, bean, beanName);
		}
		return pvsLocal;
	}

	/**
	 * @see org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
		return bean;
	}

	/**
	 * @see org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor#postProcessAfterInitialization(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
		return bean;
	}

	/**
	 * @see org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor#postProcessBeforeDestruction(java.lang.Object, java.lang.String)
	 */
	@Override
	public void postProcessBeforeDestruction(final Object bean, final String beanName) throws BeansException {
		if (this.procesable(beanName)) {
			super.postProcessBeforeDestruction(bean, beanName);
		}
	}

	@Override
	protected EntityManagerFactory findEntityManagerFactory(final String unitName, final String requestingBeanName) throws NoSuchBeanDefinitionException {
		if (this.beanFactory == null) {
			throw new IllegalStateException("ListableBeanFactory required for EntityManagerFactory bean lookup");
		}
		String unitNameForLookup = unitName != null ? unitName : "";
		if ("".equals(unitNameForLookup)) {
			unitNameForLookup = this.defaultPersistenceUnitName;
		}
		if (!"".equals(unitNameForLookup)) {
			return this.findNamedEntityManagerFactory(unitNameForLookup, requestingBeanName);
		} else {
			return this.findDefaultEntityManagerFactory(requestingBeanName);
		}
	}
}