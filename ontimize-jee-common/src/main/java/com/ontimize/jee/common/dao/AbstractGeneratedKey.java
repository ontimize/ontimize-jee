package com.ontimize.jee.common.dao;

import java.io.Serializable;

/**
 * The Class GeneratedKey.
 */
public abstract class AbstractGeneratedKey<T> implements Serializable {

    /** The generated key. */
    private T generatedKey;

    /**
     * Simple constructor to support as bean.
     */
    public AbstractGeneratedKey() {
        // Do nothing
    }

    /**
     * Instantiates a new generated key.
     * @param generatedKey the generated key
     */
    protected AbstractGeneratedKey(T generatedKey) {
        super();
        this.generatedKey = generatedKey;
    }


    /**
     * Gets the generated key.
     * @return the generated key
     */
    public T getGeneratedKey() {
        return this.generatedKey;
    }

    /**
     * Sets the generated key.
     * @param generatedKey the new generated key
     */
    public void setGeneratedKey(T generatedKey) {
        this.generatedKey = generatedKey;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (this.generatedKey == null ? 0 : this.generatedKey.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        AbstractGeneratedKey<T> other = AbstractGeneratedKey.class.cast(obj);
        if (this.generatedKey == null) {
            if (other.generatedKey != null) {
                return false;
            }
        } else if (!this.generatedKey.equals(other.generatedKey)) {
            return false;
        }
        return true;
    }

}
