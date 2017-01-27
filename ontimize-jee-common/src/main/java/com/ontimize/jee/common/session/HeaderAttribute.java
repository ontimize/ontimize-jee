/**
 *
 */
package com.ontimize.jee.common.session;

/**
 * The Class HeaderAttribute.
 */
public class HeaderAttribute {

    /** The name. */
    private String name;

    /** The value. */
    private String value;

	public HeaderAttribute() {
		super();
	}

	public HeaderAttribute(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value.
     *
     * @param value
     *            the new value
     */
    public void setValue(String value) {
        this.value = value;
    }

}
