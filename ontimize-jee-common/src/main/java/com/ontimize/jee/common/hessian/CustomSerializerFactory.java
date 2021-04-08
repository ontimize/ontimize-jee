/**
 * CustomSerializerFactory.java 18-jul-2013
 *
 *
 *
 */
package com.ontimize.jee.common.hessian;

import com.caucho.hessian.io.SerializerFactory;

/**
 * The Class CustomSerializerFactory.
 *
 * @author <a href="user@email.com">Author</a>
 */
public class CustomSerializerFactory extends SerializerFactory {

    /**
     *
     */
    public CustomSerializerFactory() {
        super();
        this.addFactory(new BigDecimalSerializerFactory());
        this.addFactory(new OntimizeSerializerFactory());
        this.addFactory(new LocaleSerializerFactory());
    }

    /**
     * {@inheritDoc}
     */
    /*
     * @Override protected Deserializer getDefaultDeserializer(final Class cl) { if
     * (InputStream.class.equals(cl)) { return InputStreamDeserializer.DESER; } if
     * (UnsafeSerializer.isEnabled() && UnsafeDeserializer.isEnabled()) { return new
     * UnsafeDeserializer(cl); } return new JavaDeserializer(cl); }
     */

}
