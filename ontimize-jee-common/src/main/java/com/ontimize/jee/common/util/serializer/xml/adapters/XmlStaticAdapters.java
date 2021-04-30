package com.ontimize.jee.common.util.serializer.xml.adapters;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;

public class XmlStaticAdapters {

    public static Date xmlGregorianCalendarToDate(XMLGregorianCalendar cal) {
        return cal.toGregorianCalendar().getTime();
    }

}
