/**
 * CustomSerializerFactory.java 18-jul-2013
 *
 *
 *
 */
package com.ontimize.jee.common.hessian;

import java.io.InputStream;

import com.caucho.hessian.io.CustomJavaDeserializer;
import com.caucho.hessian.io.CustomUnsafeDeserializer;
import com.caucho.hessian.io.Deserializer;
import com.caucho.hessian.io.InputStreamDeserializer;
import com.caucho.hessian.io.OntimizeSerializerFactory;
import com.caucho.hessian.io.SerializerFactory;
import com.caucho.hessian.io.UnsafeDeserializer;
import com.caucho.hessian.io.UnsafeSerializer;

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
	@Override
	protected Deserializer getDefaultDeserializer(final Class cl) {
		if (InputStream.class.equals(cl)) {
			return InputStreamDeserializer.DESER;
		}

		if (UnsafeSerializer.isEnabled() && UnsafeDeserializer.isEnabled()) {
			return new CustomUnsafeDeserializer(cl);
		}
		return new CustomJavaDeserializer(cl);
	}
}