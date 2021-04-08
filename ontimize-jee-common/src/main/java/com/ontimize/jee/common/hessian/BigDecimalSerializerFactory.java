package com.ontimize.jee.common.hessian;

import java.io.IOException;
import java.math.BigDecimal;

import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.AbstractSerializerFactory;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.deserializer.AbstractStringValueDeserializer;
import com.caucho.hessian.io.deserializer.Deserializer;
import com.caucho.hessian.io.serializer.AbstractSerializer;
import com.caucho.hessian.io.serializer.Serializer;

public class BigDecimalSerializerFactory extends AbstractSerializerFactory {

    private final BigDecimalSerializer bigDecimalSerializer = new BigDecimalSerializer();

    private final BigDecimalDeserializer bigDecimalDeserializer = new BigDecimalDeserializer();

    @Override
    public Serializer getSerializer(final Class cl) throws HessianProtocolException {
        if (BigDecimal.class.isAssignableFrom(cl)) {
            return this.bigDecimalSerializer;
        }
        return null;
    }

    @Override
    public Deserializer getDeserializer(final Class cl) throws HessianProtocolException {
        if (BigDecimal.class.isAssignableFrom(cl)) {
            return this.bigDecimalDeserializer;
        }
        return null;
    }

    public static class BigDecimalSerializer extends AbstractSerializer {

        @Override
        public void writeObject(final Object obj, final AbstractHessianOutput out) throws IOException {

            if (obj == null) {
                out.writeNull();
            } else {
                Class cl = obj.getClass();

                if (out.addRef(obj)) {
                    return;
                }

                int ref = out.writeObjectBegin(cl.getName());

                BigDecimal bi = (BigDecimal) obj;

                if (ref < -1) {
                    out.writeString("value");
                    out.writeString(bi.toString());
                    out.writeMapEnd();
                } else {
                    if (ref == -1) {
                        out.writeInt(1);
                        out.writeString("value");
                        out.writeObjectBegin(cl.getName());
                    }

                    out.writeString(bi.toString());
                }
            }
        }

    }

    public static class BigDecimalDeserializer extends AbstractStringValueDeserializer {

        @Override
        public Class getType() {
            return BigDecimal.class;
        }

        @Override
        protected Object create(final String value) {
            if (null != value) {
                return new BigDecimal(value);
            } else {
                return null;
            }
        }

    }

}
