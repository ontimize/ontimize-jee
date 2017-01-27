/**
 * 
 */
package com.ontimize.jee.server.dao.jpa.dataconversors;

/**
 * The Interface DataConversor.
 *
 * @param <T> the generic type
 * @param <S> the generic type
 */
public interface DataConversor<T,S> {

	/**
	 * Convert.
	 *
	 * @param input the input
	 * @param toType the to type
	 * @return the s
	 */
	S convert(T input, Class<S> toType);
	
	/**
	 * Can handle conversion.
	 *
	 * @param fromType the from type
	 * @param toType the to type
	 * @return true, if can handle conversion
	 */
	boolean canHandleConversion(Object object, Class<S> toType);
}
