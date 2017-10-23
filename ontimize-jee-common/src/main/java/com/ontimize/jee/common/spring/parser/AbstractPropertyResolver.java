package com.ontimize.jee.common.spring.parser;

/**
 * The Class AbstractPropertyResolver.
 *
 * @param <T>
 *            the generic type
 */
public abstract class AbstractPropertyResolver<T> implements IPropertyResolver<T> {

	/** The object type. */
	private Class<T>	objectType;

	/** permite inyectar este objeto a través del motor de spring (en el getObject se devuelve this). */
	private boolean		useMyselfInSpringContext;

	/**
	 * Instantiates a new abstract property resolver.
	 */
	public AbstractPropertyResolver() {
		super();
		this.useMyselfInSpringContext = false;
	}

	/**
	 * Sets the use myself in spring context.
	 *
	 * @param useMyselfInSpringContext
	 *            the new use myself in spring context
	 */
	public void setUseMyselfInSpringContext(boolean useMyselfInSpringContext) {
		this.useMyselfInSpringContext = useMyselfInSpringContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	@Override
	public T getObject() throws Exception {
		if (this.useMyselfInSpringContext) {
			return (T) this;
		}
		return this.getValue();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	@Override
	public Class<?> getObjectType() {
		return this.objectType;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	@Override
	public boolean isSingleton() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.common.spring.parser.IPropertyResolver#setClassType(java.lang.Class)
	 */
	@Override
	public void setClassType(Class<T> cl) {
		this.objectType = cl;
	}

}
