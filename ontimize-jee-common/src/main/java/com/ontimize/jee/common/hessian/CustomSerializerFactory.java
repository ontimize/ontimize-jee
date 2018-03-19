/**
 * CustomSerializerFactory.java 18-jul-2013
 *
 *
 *
 */
package com.ontimize.jee.common.hessian;

import java.io.InputStream;

import com.caucho.hessian.io.OntimizeSerializerFactory;
import com.caucho.hessian.io.SerializerFactory;
import com.caucho.hessian.io.deserializer.CustomJavaDeserializer;
import com.caucho.hessian.io.deserializer.CustomUnsafeDeserializer;
import com.caucho.hessian.io.deserializer.Deserializer;
import com.caucho.hessian.io.deserializer.InputStreamDeserializer;
import com.caucho.hessian.io.deserializer.UnsafeDeserializer;
import com.caucho.hessian.io.serializer.UnsafeSerializer;

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