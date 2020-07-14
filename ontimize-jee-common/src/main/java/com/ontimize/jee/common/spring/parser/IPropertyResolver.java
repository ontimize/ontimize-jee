package com.ontimize.jee.common.spring.parser;

import org.springframework.beans.factory.FactoryBean;

/**
 * The Interface IPropertyResolver.
 */
public interface IPropertyResolver<T> extends FactoryBean<T> {

    /**
     * Gets the value.
     * @return the value
     */
    public T getValue();

    public void setClassType(Class<T> cl);

}
