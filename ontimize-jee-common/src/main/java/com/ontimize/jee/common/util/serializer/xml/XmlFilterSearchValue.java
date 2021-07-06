package com.ontimize.jee.common.util.serializer.xml;

import com.ontimize.jee.common.gui.SearchValue;
import com.ontimize.jee.common.util.serializer.xml.adapters.XmlStaticAdapters;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlRootElement(name = "search-value")
public class XmlFilterSearchValue {

    protected int operator;

    protected Object value;

    protected XmlFilterValueSearchValue svValue;

    public XmlFilterSearchValue() {
        // TODO Auto-generated constructor stub
    }

    public XmlFilterSearchValue(SearchValue sv) {
        this.operator = sv.getCondition();
        this.svValue = new XmlFilterValueSearchValue(sv.getValue());
    }

    public SearchValue getSearchValue() {

        Object value = this.getSvValue();

        if (value == null) {
            value = this.getValue();
        }

        if (value instanceof XmlFilterValueSearchValue) {
            value = ((XmlFilterValueSearchValue) value).getBaseValues();
        }

        if (value instanceof XMLGregorianCalendar) {
            value = XmlStaticAdapters.xmlGregorianCalendarToDate((XMLGregorianCalendar) value);
        }

        return new SearchValue(this.getOperator(), value);

    }

    public int getOperator() {
        return this.operator;
    }

    @XmlElement(name = "operator")
    public void setOperator(int operator) {
        this.operator = operator;
    }

    public XmlFilterValueSearchValue getSvValue() {
        return this.svValue;
    }

    @XmlElement(name = "svvalue", required = true)
    public void setSvValue(XmlFilterValueSearchValue svValue) {
        this.svValue = svValue;
    }

    @Deprecated
    public Object getValue() {
        return this.value;
    }

    @Deprecated
    @XmlElement(name = "value", required = true)
    public void setValue(Object value) {
        if (!(value instanceof Element)) {
            this.value = value;
        }
    }

}
