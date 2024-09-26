package com.ontimize.jee.common.util.serializer.xml.adapters;

import com.ontimize.jee.common.db.SQLStatementBuilder.BasicOperator;
import org.apache.commons.lang.StringEscapeUtils;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class XmlBasicOperatorAdapter extends XmlAdapter<String, BasicOperator> {

    @Override
    public BasicOperator unmarshal(String v) throws Exception {
        return new BasicOperator(StringEscapeUtils.unescapeHtml(v));
    }

    @Override
    public String marshal(BasicOperator v) throws Exception {
        return v.toString();
    }

}

