package com.ontimize.jee.common.hessian;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Map;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.AbstractSerializerFactory;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.deserializer.Deserializer;
import com.caucho.hessian.io.deserializer.JavaDeserializer;
import com.caucho.hessian.io.serializer.JavaSerializer;
import com.caucho.hessian.io.serializer.Serializer;
import com.ontimize.db.EntityResult;
import com.ontimize.gui.table.TableAttribute;
import com.ontimize.jee.common.tools.ReflectionTools;

/**
 * A factory for creating OntimizeSerializer objects.
 */
public class OntimizeSerializerFactory extends AbstractSerializerFactory {

    private static final String INNER_ONTIMIZE_MAP = "innerontimizemap";

    /**
     * Instantiates a new ontimize serializer factory.
     */
    public OntimizeSerializerFactory() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.caucho.hessian.io.AbstractSerializerFactory#getSerializer(java.lang .Class)
     */
    @Override
    public Serializer getSerializer(Class cl) throws HessianProtocolException {
        if (TableAttribute.class.isAssignableFrom(cl) || EntityResult.class.isAssignableFrom(cl)) {
            return this.createSerializer(cl);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.caucho.hessian.io.AbstractSerializerFactory#getDeserializer(java. lang.Class)
     */
    @Override
    public Deserializer getDeserializer(Class cl) throws HessianProtocolException {
        if (TableAttribute.class.isAssignableFrom(cl) || EntityResult.class.isAssignableFrom(cl)) {
            return this.createDeserializer(cl);
        }
        return null;
    }

    /**
     * Creates a new OntimizeSerializer object.
     * @param cl the cl
     * @return the serializer
     */
    protected Serializer createSerializer(Class cl) {
        return new MapExtendedClassSerializer(cl);
    }

    /**
     * Creates a new OntimizeSerializer object.
     * @param cl the cl
     * @return the deserializer
     */
    protected Deserializer createDeserializer(Class cl) {
        return new MapExtendedClassDeserializer(cl);
    }

    /**
     * Serializer for classes extending {@link HashMap} or {@link Map}. Lo que hacemos es no
     * permitir explorar las propiedades de la parte del Map (igual que hace el map serializer) y crear
     * una variable ficticia para meter el contenido del map.
     *
     */
    public static class MapExtendedClassSerializer extends JavaSerializer {

        public MapExtendedClassSerializer(Class<?> cl) {
            super(cl);
        }

        @Override
        protected void introspect(final Class<?> cl) {
            ArrayList<Field> primitiveFields = new ArrayList<>();
            ArrayList<Field> compoundFields = new ArrayList<>();

            for (Class<?> theClass = cl; theClass != null; theClass = theClass.getSuperclass()) {
                if (theClass.equals(Map.class) || theClass.equals(HashMap.class)) {
                    break;
                }
                Field[] fields = theClass.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];

                    if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }

                    // XXX: could parameterize the handler to only deal with
                    // public
                    field.setAccessible(true);

                    if (field.getType().isPrimitive() || (field.getType().getName().startsWith("java.lang.")
                            && !field.getType().equals(Object.class))) {
                        primitiveFields.add(field);
                    } else {
                        compoundFields.add(field);
                    }
                }
            }

            Object ob = ReflectionTools.newInstance("java.lang.reflect.ReflectAccess");
            Field field = (Field) ReflectionTools.invoke(ob, "newField", cl,
                    OntimizeSerializerFactory.INNER_ONTIMIZE_MAP, Map.class, Member.PUBLIC, 0, "", null);
            compoundFields.add(field);

            ArrayList<Field> fields = new ArrayList<>();
            fields.addAll(primitiveFields);
            fields.addAll(compoundFields);

            Field[] theFields = new Field[fields.size()];
            fields.toArray(theFields);

            this.setFields(theFields);

            FieldSerializer[] theFieldSerializers = (FieldSerializer[]) Array.newInstance(FieldSerializer.class,
                    theFields.length);

            for (int i = 0; i < (theFields.length - 1); i++) {
                theFieldSerializers[i] = JavaSerializer.getFieldSerializer(theFields[i].getType());
            }

            theFieldSerializers[theFieldSerializers.length - 1] = new FieldSerializer() {

                @Override
                protected void serialize(AbstractHessianOutput out, Object obj, Field field) throws IOException {
                    // write the map values
                    if (out.addRef(new HashMap<>())) {
                        return;
                    }
                    out.writeMapBegin(null);
                    Iterator iter = ((Map) obj).entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = (Map.Entry) iter.next();

                        out.writeObject(entry.getKey());
                        out.writeObject(entry.getValue());
                    }
                    out.writeMapEnd();
                }
            };
            this.setFieldSerializers(theFieldSerializers);
        }

        @Override
        public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
            super.writeObject(obj, out);
        }

    }

    /**
     * Deserializer for classes extending {@link HashMap} or {@link Map}
     *
     */
    public static class MapExtendedClassDeserializer extends JavaDeserializer {

        public MapExtendedClassDeserializer(Class cl) {
            super(cl);
        }

        /**
         * Creates a map of the classes fields.
         */
        @Override
        protected HashMap<String, FieldDeserializer> getFieldMap(Class cl) {
            HashMap<String, FieldDeserializer> fieldMap = super.getFieldMap(cl);
            fieldMap.put(OntimizeSerializerFactory.INNER_ONTIMIZE_MAP, new FieldDeserializer() {

                @Override
                protected void deserialize(AbstractHessianInput in, Object obj) throws IOException {
                    Map<?, ?> map = (Map<?, ?>) in.readObject();
                    ((Map) obj).putAll(map);
                }
            });

            return fieldMap;
        }

    }

}
