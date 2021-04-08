package com.ontimize.jee.common.hessian;

import java.io.IOException;
import java.util.Locale;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.AbstractSerializerFactory;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.deserializer.AbstractDeserializer;
import com.caucho.hessian.io.deserializer.Deserializer;
import com.caucho.hessian.io.serializer.AbstractSerializer;
import com.caucho.hessian.io.serializer.Serializer;

/**
 * The Class LocaleSerializerFactory.
 */
public class LocaleSerializerFactory extends AbstractSerializerFactory {

    /*
     * (non-Javadoc)
     *
     * @see com.caucho.hessian.io.AbstractSerializerFactory#getSerializer(java.lang.Class)
     */
    @Override
    public Serializer getSerializer(Class cl) throws HessianProtocolException {
        if (Locale.class.isAssignableFrom(cl)) {
            return new LocaleSerializer();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.caucho.hessian.io.AbstractSerializerFactory#getDeserializer(java.lang.Class)
     */
    @Override
    public Deserializer getDeserializer(Class cl) throws HessianProtocolException {
        if (Locale.class.isAssignableFrom(cl)) {
            return new LocaleDeserializer();
        }
        return null;
    }

    /**
     * The Class LocaleDeserializer.
     */
    public static class LocaleDeserializer extends AbstractDeserializer {

        /*
         * (non-Javadoc)
         *
         * @see com.caucho.hessian.io.AbstractDeserializer#getType()
         */
        @Override
        public Class getType() {
            return Locale.class;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.caucho.hessian.io.AbstractDeserializer#readMap(com.caucho.hessian.io.AbstractHessianInput)
         */
        @Override
        public Object readMap(AbstractHessianInput in) throws IOException {
            int ref = in.addRef(null);

            String languageValue = null;
            String countryValue = null;
            String variantValue = null;

            while (!in.isEnd()) {
                String key = in.readString();

                if ("language".equals(key)) {
                    languageValue = in.readString();
                } else if ("country".equals(key)) {
                    countryValue = in.readString();
                } else if ("variant".equals(key)) {
                    variantValue = in.readString();
                } else {
                    in.readString();
                }
            }

            in.readMapEnd();

            Object value = this.getObject(languageValue, countryValue, variantValue);

            in.setRef(ref, value);

            return value;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.caucho.hessian.io.AbstractDeserializer#readObject(com.caucho.hessian.io.AbstractHessianInput,
         * java.lang.Object[])
         */
        @Override
        public Object readObject(AbstractHessianInput in, Object[] fields) throws IOException {
            int ref = in.addRef(null);

            String languageValue = null;
            String countryValue = null;
            String variantValue = null;

            for (Object key : fields) {
                if ("language".equals(key)) {
                    languageValue = in.readString();
                } else if ("country".equals(key)) {
                    countryValue = in.readString();
                } else if ("variant".equals(key)) {
                    variantValue = in.readString();
                } else {
                    in.readObject();
                }
            }

            Object value = this.getObject(languageValue, countryValue, variantValue);

            in.setRef(ref, value);

            return value;
        }

        /**
         * Gets the object.
         * @param languageValue the language value
         * @param countryValue the country value
         * @param variantValue the variant value
         * @return the object
         */
        private Object getObject(String languageValue, String countryValue, String variantValue) {
            Object value = null;
            if ((languageValue != null) && (countryValue != null) && (variantValue != null)) {
                value = new Locale(languageValue, countryValue, variantValue);
            } else if ((languageValue != null) && (countryValue != null)) {
                value = new Locale(languageValue, countryValue);
            } else if (languageValue != null) {
                value = new Locale(languageValue);
            } else {
                value = Locale.getDefault();
            }
            return value;
        }

    }

    /**
     * The Class LocaleSerializer.
     */
    public class LocaleSerializer extends AbstractSerializer {

        /*
         * (non-Javadoc)
         *
         * @see com.caucho.hessian.io.AbstractSerializer#writeObject(java.lang.Object,
         * com.caucho.hessian.io.AbstractHessianOutput)
         */
        @Override
        public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {

            if (obj == null) {
                out.writeNull();
            } else {
                Class cl = obj.getClass();

                if (out.addRef(obj)) {
                    return;
                }

                int ref = out.writeObjectBegin(cl.getName());

                Locale loc = (Locale) obj;

                if (ref < -1) {
                    if (loc.getLanguage() != null) {
                        out.writeString("language");
                        out.writeString(loc.getLanguage());
                    }
                    if (loc.getCountry() != null) {
                        out.writeString("country");
                        out.writeString(loc.getCountry());
                    }
                    if (loc.getVariant() != null) {
                        out.writeString("variant");
                        out.writeString(loc.getVariant());
                    }

                    out.writeMapEnd();
                } else {
                    if (ref == -1) {
                        out.writeInt(3);
                        out.writeString("language");
                        out.writeString("country");
                        out.writeString("variant");
                        out.writeObjectBegin(cl.getName());
                    }
                    out.writeString(loc.getLanguage());
                    out.writeString(loc.getCountry());
                    out.writeString(loc.getVariant());
                }
            }
        }

    }

}
