package com.ontimize.jee.common.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * A collection of utilities to workaround limitations of Java clone framework.
 *
 */
public final class CloneTools {

	/**
	 * This class should not be instantiated.
	 */
	private CloneTools() {}

	public static Object clone(final Object obj) throws OntimizeJEERuntimeException {
		if (obj == null) {
			return null;
		}
		try {
			if (obj instanceof Cloneable) {
				return ReflectionTools.invoke(obj, "clone");
			}
		} catch (Exception ex) {
			// do nothing
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream os = new ObjectOutputStream(baos);
			os.writeObject(obj);
			os.close();
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
			return ois.readObject();

		} catch (Exception e) {
			throw new OntimizeJEERuntimeException(e);
		}
	}
}