/**
 * ObjectTools.java 31/07/2013
 *
 *
 *
 */
package com.ontimize.jee.common.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * Utilidades de objetos.
 *
 * @author <a href=""></a>
 */
public final class MapTools {

	/**
	 * Instantiates a new map tools.
	 */
	private MapTools() {
		super();
	}

	/**
	 * Clona un {@link Hashtable} escribiendolo y leyendolo de un stream.
	 *
	 * @param <U>
	 *            the generic type
	 * @param <V>
	 *            the value type
	 * @param map
	 *            the map
	 * @return the cloned map
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 */
	public static <U, V> Map<U, V> clone(Map<U, V> map) throws IOException, ClassNotFoundException {
		return (Map<U, V>) MapTools.cloneSerializableObject((Serializable) map);
	}

	/**
	 * Clona un objeto serializable escribiendolo y leyendolo de un stream.
	 *
	 * @param paraclonar
	 *            the paraclonar
	 * @return the object
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 */
	public static Object cloneSerializableObject(Serializable paraclonar) throws IOException, ClassNotFoundException {
		if (paraclonar == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(paraclonar);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		return ois.readObject();
	}

	/**
	 * Devuelve un hashtable con las claves de filtro que estan en origen.
	 *
	 * @param <U>
	 *            the generic type
	 * @param <V>
	 *            the value type
	 * @param origen
	 *            the origen
	 * @param filtro
	 *            the filtro
	 * @return the hashtable
	 */
	public static <U, V> Map<U, V> hashtableFilter(Map<U, V> origen, List<U> filtro) {
		Map<U, V> filtrado;
		try {
			filtrado = origen.getClass().newInstance();
		} catch (Exception ex) {
			throw new OntimizeJEERuntimeException(ex);
		}
		if (filtro != null) {
			for (U clave : filtro) {
				if ((clave != null) && origen.containsKey(clave)) {
					filtrado.put(clave, origen.get(clave));
				}
			}
		}
		return filtrado;
	}

	/**
	 * Une los dos hastable en uno nuevo que contiene los valores del primero y los del segundo, si ambos contiene claves iguales predomina el valor del segundo hashtable.
	 *
	 * @param <U>
	 *            the generic type
	 * @param <V>
	 *            the value type
	 * @param first
	 *            the origen
	 * @param second
	 *            the destino
	 * @return the hashtable
	 */
	public static <U, V> Map<U, ? extends V> union(Map<U, V> first, Map<U, ? extends V> second) {
		if ((first == null) && (second == null)) {
			return new Hashtable<U, V>();
		}
		Map<U, V> res = null;
		try {
			if (first != null) {
				res = first.getClass().newInstance();
			} else {
				res = second.getClass().newInstance();
			}
		} catch (Exception ex) {
			throw new OntimizeJEERuntimeException(ex);
		}
		res.putAll(first);
		res.putAll(second);
		return res;
	}

	/**
	 * Introduce el park key/value en el hashtable siempre que sean distintos de null.
	 *
	 * @param <T>
	 *            the generic type
	 * @param <Q>
	 *            the generic type
	 * @param map
	 *            the h
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return true, if successful
	 */
	public static <T, Q> boolean safePut(Map<T, Q> map, T key, Q value) {
		if ((map != null) && (key != null) && (value != null)) {
			map.put(key, value);
			return true;
		}
		return false;

	}

	/**
	 * New map.
	 *
	 * @param <P>
	 *            the generic type
	 * @param <Q>
	 *            the generic type
	 * @param map
	 *            the map
	 * @param keysValues
	 *            the keys values
	 * @return the map
	 */
	public static <R extends Map<P, Q>, P, Q> R newMap(R map, Object... keysValues) {
		if (keysValues == null) {
			return map;
		}
		if ((keysValues.length % 2) != 0) {
			throw new IllegalArgumentException("keys values length must be even");
		}
		for (int i = 0; i < keysValues.length; i += 2) {
			map.put((P) keysValues[i], (Q) keysValues[i + 1]);
		}
		return map;
	}

	/**
	 * Introduce el park key/value en el hashtable siempre que sean distintos de null, y si no existe ya esa clave.
	 *
	 * @param <T>
	 *            the generic type
	 * @param <Q>
	 *            the generic type
	 * @param h
	 *            the h
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param abortWhenExist
	 *            the abort when exist
	 * @return true, if successful
	 */
	public static <T, Q> boolean safePut(Map<T, Q> h, T key, Q value, boolean abortWhenExist) {
		if ((h != null) && h.containsKey(key) && abortWhenExist) {
			return false;
		}
		return MapTools.safePut(h, key, value);
	}

	/**
	 * Clear null values.
	 *
	 * @param <P>
	 *            the generic type
	 * @param <Q>
	 *            the generic type
	 * @param input
	 *            the input
	 * @return the map
	 */
	public static <P, Q> Map<P, Q> clearNullValues(Map<P, Q> input) {
		if (input == null) {
			return null;
		}
		Map<P, Q> res;
		try {
			res = input.getClass().newInstance();
		} catch (Exception error) {
			throw new RuntimeException(error);
		}
		for (Entry<P, Q> entry : input.entrySet()) {
			if (entry.getValue() != null) {
				res.put(entry.getKey(), entry.getValue());
			}
		}
		return res;
	}

	/**
	 * Keysvalues.
	 *
	 * @param <T>
	 *            the generic type
	 * @param <Q>
	 *            the generic type
	 * @param objects
	 *            the objects
	 * @return the map
	 */
	public static <T, Q> Map<T, Q> keysvalues(Object... objects) {
		if (objects == null) {
			return new HashMap<T, Q>();
		}
		if ((objects.length % 2) != 0) {
			throw new RuntimeException("Review filters, it is mandatory to set dual <key><value>.");
		}
		for (Object o : objects) {
			if (o == null) {
				throw new RuntimeException("Review filters, it is not acceptable null <key> or null <value>.");
			}
		}

		HashMap<T, Q> res = new HashMap<T, Q>();
		int i = 0;
		while (i < objects.length) {
			res.put((T) objects[i++], (Q) objects[i++]);
		}
		return res;
	}

	/**
	 * To string.
	 *
	 * @param map
	 *            the map
	 * @param indent
	 *            the indent
	 * @return the string
	 */
	public static String toString(Map<?, ?> map, String indent) {
		if (map == null) {
			return "";
		}
		if (indent == null) {
			indent = "";
		}
		StringBuilder sb = new StringBuilder();
		for (Entry<Object, Object> entry : ((Map<Object, Object>) map).entrySet()) {
			if (entry.getValue() instanceof Map) {
				sb.append(indent).append(entry.getKey()).append(":\n");
				sb.append(MapTools.toString((Map<Object, Object>) entry.getValue(), indent + "\t"));
			} else {
				sb.append(indent).append(entry.getKey()).append(": ").append(entry.getValue());
			}
		}
		return sb.toString();
	}

}
