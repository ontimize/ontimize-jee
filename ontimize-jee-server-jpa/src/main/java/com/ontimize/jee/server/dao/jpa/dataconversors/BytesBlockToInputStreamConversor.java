/**
 *
 */
package com.ontimize.jee.server.dao.jpa.dataconversors;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.ontimize.jee.common.util.remote.BytesBlock;

/**
 * The Class BytesBlockToInputStreamConversor.
 */
public class BytesBlockToInputStreamConversor implements DataConversor<BytesBlock, InputStream> {

    /**
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.dao.jpa.dataconversors.DataConversor#convert(java.lang.Object,
     *      java.lang.Class)
     */
    @Override
    public InputStream convert(BytesBlock input, Class<InputStream> toType) {
        if ((input != null) && (input.getBytes() != null)) {
            return new ByteArrayInputStream(input.getBytes());
        }
        return null;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.dao.jpa.dataconversors.DataConversor#canHandleConversion(java.lang.Object,
     *      java.lang.Class)
     */
    @Override
    public boolean canHandleConversion(Object object, Class<InputStream> toType) {
        return (object instanceof BytesBlock) && InputStream.class.isAssignableFrom(toType);
    }

}
