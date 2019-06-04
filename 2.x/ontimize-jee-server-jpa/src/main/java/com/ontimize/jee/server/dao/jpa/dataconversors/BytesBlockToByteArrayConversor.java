/**
 *
 */
package com.ontimize.jee.server.dao.jpa.dataconversors;

import com.ontimize.util.remote.BytesBlock;

/**
 * The Class BytesBlockToByteArrayConversor.
 */
public class BytesBlockToByteArrayConversor implements DataConversor<BytesBlock, byte[]> {

	/**
	 * (non-Javadoc)
	 *
	 * @see com.ontimize.jee.server.dao.jpa.dataconversors.DataConversor#convert(java.lang.Object, java.lang.Class)
	 */
	@Override
	public byte[] convert(BytesBlock input, Class<byte[]> toType) {
		if (input != null) {
			return input.getBytes();
		}
		return null;
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see com.ontimize.jee.server.dao.jpa.dataconversors.DataConversor#canHandleConversion(java.lang.Object, java.lang.Class)
	 */
	@Override
	public boolean canHandleConversion(Object object, Class<byte[]> toType) {
		return (object instanceof BytesBlock) && byte[].class.isAssignableFrom(toType);
	}

}
