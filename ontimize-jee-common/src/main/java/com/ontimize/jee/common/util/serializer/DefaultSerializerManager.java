package com.ontimize.jee.common.util.serializer;

import com.ontimize.jee.common.util.Base64Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;


public class DefaultSerializerManager implements ISerializerManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSerializerManager.class);

    @Override
    public String serializeMapToString(Map<String, Object> data) throws Exception {
        return this.convertFilterDataToString((Map) data);
    }

    @Override
    public Map<String, Object> deserializeStringToMap(String data) throws Exception {
        return this.convertFilterDataToMap(data);
    }

    protected String convertFilterDataToString(Map data) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(out);
            output.writeObject(data);
            output.flush();
            String sOut = new String(Base64Utils.encode(out.toByteArray()));
            output.close();
            return sOut;
        } catch (Exception ex) {
            DefaultSerializerManager.logger.error(null, ex);
        }
        return null;
    }

    protected Map convertFilterDataToMap(String data) {
        try {
            byte[] bytes = Base64Utils.decode(data.toCharArray());
            ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
            ObjectInputStream in = new ObjectInputStream(bIn);
            Object o = in.readObject();
            in.close();
            return (Map) o;
        } catch (Exception ex) {
            DefaultSerializerManager.logger.error(null, ex);
        }
        return null;
    }

}
