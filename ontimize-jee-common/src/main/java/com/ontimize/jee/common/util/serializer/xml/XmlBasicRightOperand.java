package com.ontimize.jee.common.util.serializer.xml;

import com.ontimize.jee.common.util.serializer.xml.adapters.XmlStaticAdapters;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "basic-right-operand", propOrder = { "basicExpression", "value", "searchValue" })
public class XmlBasicRightOperand {

    @XmlElement(name = "basic-expression")
    protected XmlFilterBasicExpression basicExpression;

    protected Object value;

    @XmlElement(name = "search-value")
    protected XmlFilterSearchValue searchValue;

    public XmlFilterBasicExpression getBasicExpression() {
        return this.basicExpression;
    }

    public void setBasicExpression(XmlFilterBasicExpression value) {
        this.basicExpression = value;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public XmlFilterSearchValue getSearchValue() {
        return this.searchValue;
    }

    public void setSearchValue(XmlFilterSearchValue value) {
        this.searchValue = value;
    }

    public Object getBasicValue() {
        if ((this.value != null) && (this.basicExpression == null) && (this.searchValue == null)) {
            Object val = this.value;
            if (val instanceof XMLGregorianCalendar) {
                val = XmlStaticAdapters.xmlGregorianCalendarToDate((XMLGregorianCalendar) val);
            }
            return val;
        } else if ((this.value == null) && (this.basicExpression != null) && (this.searchValue == null)) {
            return this.basicExpression.getBasicExpression();
        } else if ((this.value == null) && (this.basicExpression == null) && (this.searchValue != null)) {
            return this.searchValue.getSearchValue();
        }
        return null;
    }

}
