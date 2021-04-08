package com.ontimize.jee.common.spring.parser;

/**
 * The Class FixedPropertyResolver.
 */
public class FixedPropertyResolver<T> extends AbstractPropertyResolver<T> {

    /** The value. */
    private T value;

    /**
     * The Constructor.
     */
    public FixedPropertyResolver() {
        super();
    }

    /**
     * Instantiates a new fixed property resolver.
     * @param value the value
     */
    public FixedPropertyResolver(T value) {
        this();
        this.setValue(value);
    }

    /**
     * Gets the value.
     * @return the value
     */
    @Override
    public T getValue() {
        return this.value;
    }

    /**
     * Sets the value.
     * @param value the value
     */
    public void setValue(T value) {
        this.value = value;
    }

}
