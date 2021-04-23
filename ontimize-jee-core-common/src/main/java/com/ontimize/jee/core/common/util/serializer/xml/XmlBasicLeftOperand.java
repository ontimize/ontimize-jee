package com.ontimize.jee.core.common.util.serializer.xml;

import com.ontimize.jee.core.common.db.SQLStatementBuilder.BasicField;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for basic-left-operand complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="basic-left-operand">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="basic-field" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="basic-expression" type="{}xmlFilterBasicExpression" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "basic-left-operand", propOrder = { "basicField", "basicExpression" })
public class XmlBasicLeftOperand {

    @XmlElement(name = "basic-field")
    protected String basicField;

    @XmlElement(name = "basic-expression")
    protected XmlFilterBasicExpression basicExpression;

    /**
     * Gets the value of the basicField property.
     * @return possible object is {@link String }
     *
     */
    public String getBasicField() {
        return this.basicField;
    }

    /**
     * Sets the value of the basicField property.
     * @param value allowed object is {@link String }
     *
     */
    public void setBasicField(String value) {
        this.basicField = value;
    }

    /**
     * Gets the value of the basicExpression property.
     * @return possible object is {@link XmlFilterBasicExpression }
     *
     */
    public XmlFilterBasicExpression getBasicExpression() {
        return this.basicExpression;
    }

    /**
     * Sets the value of the basicExpression property.
     * @param value allowed object is {@link XmlFilterBasicExpression }
     *
     */
    public void setBasicExpression(XmlFilterBasicExpression value) {
        this.basicExpression = value;
    }

    public Object getBasicValue() {
        if ((this.basicField != null) && (this.basicExpression == null)) {
            return new BasicField(this.basicField);
        } else if ((this.basicField == null) && (this.basicExpression != null)) {
            return this.basicExpression.getBasicExpression();
        }
        return null;
    }

}
