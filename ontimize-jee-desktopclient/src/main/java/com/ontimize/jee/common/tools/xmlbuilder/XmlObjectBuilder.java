package com.ontimize.jee.common.tools.xmlbuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class XmlObjectBuilder {

    public XmlObjectBuilder() {
        super();
    }

    public Object buildFromXml(final Object parent, final InputStream is, Map<String, String> equivalences,
            final Map<String, String> extraRootParameters)
            throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        XmlObjectBuilderSaxHandler handler = new XmlObjectBuilderSaxHandler(parent, equivalences, extraRootParameters);
        saxParser.parse(is, handler);
        Map<Object, Map<String, String>> objectParameters = handler.getObjectParameters();
        // notifyCompositeBuild(handler.getRoot(), handler.getRoot(), objectParameters);
        // for (Entry<Object, Map<String, String>> entry : objectParameters.entrySet()) {
        // if (entry.getKey() instanceof IScreenPartBuilt) {
        // ((IScreenPartBuilt) entry.getKey()).screenPartBuilt(handler.getRoot(), entry.getValue());
        // }
        // }
        objectParameters.clear();
        return handler.getRoot();
    }

}
