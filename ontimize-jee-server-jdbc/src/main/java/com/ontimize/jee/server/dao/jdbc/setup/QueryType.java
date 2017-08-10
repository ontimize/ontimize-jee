//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.07.16 at 04:38:17 PM CEST 
//


package com.ontimize.jee.server.dao.jdbc.setup;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for QueryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AmbiguousColumns" type="{http://www.ontimize.com/schema/jdbc}AmbiguousColumnsType" minOccurs="0"/>
 *         &lt;element name="ValidColumns" type="{http://www.ontimize.com/schema/jdbc}ColumnGroupType" minOccurs="0"/>
 *         &lt;element name="FunctionColumns" type="{http://www.ontimize.com/schema/jdbc}FunctionColumnsType" minOccurs="0"/>
 *         &lt;element name="Sentence" type="{http://www.ontimize.com/schema/jdbc}SentenceType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryType", propOrder = {
    "ambiguousColumns",
    "validColumns",
    "functionColumns",
    "sentence"
})
public class QueryType {

    @XmlElement(name = "AmbiguousColumns")
    protected AmbiguousColumnsType ambiguousColumns;
    @XmlElement(name = "ValidColumns")
    protected ColumnGroupType validColumns;
    @XmlElement(name = "FunctionColumns")
    protected FunctionColumnsType functionColumns;
    @XmlElement(name = "Sentence", required = true)
    protected SentenceType sentence;
    @XmlAttribute(name = "id")
    protected String id;

    /**
     * Gets the value of the ambiguousColumns property.
     * 
     * @return
     *     possible object is
     *     {@link AmbiguousColumnsType }
     *     
     */
    public AmbiguousColumnsType getAmbiguousColumns() {
        return ambiguousColumns;
    }

    /**
     * Sets the value of the ambiguousColumns property.
     * 
     * @param value
     *     allowed object is
     *     {@link AmbiguousColumnsType }
     *     
     */
    public void setAmbiguousColumns(AmbiguousColumnsType value) {
        this.ambiguousColumns = value;
    }

    /**
     * Gets the value of the validColumns property.
     * 
     * @return
     *     possible object is
     *     {@link ColumnGroupType }
     *     
     */
    public ColumnGroupType getValidColumns() {
        return validColumns;
    }

    /**
     * Sets the value of the validColumns property.
     * 
     * @param value
     *     allowed object is
     *     {@link ColumnGroupType }
     *     
     */
    public void setValidColumns(ColumnGroupType value) {
        this.validColumns = value;
    }

    /**
     * Gets the value of the functionColumns property.
     * 
     * @return
     *     possible object is
     *     {@link FunctionColumnsType }
     *     
     */
    public FunctionColumnsType getFunctionColumns() {
        return functionColumns;
    }

    /**
     * Sets the value of the functionColumns property.
     * 
     * @param value
     *     allowed object is
     *     {@link FunctionColumnsType }
     *     
     */
    public void setFunctionColumns(FunctionColumnsType value) {
        this.functionColumns = value;
    }

    /**
     * Gets the value of the sentence property.
     * 
     * @return
     *     possible object is
     *     {@link SentenceType }
     *     
     */
    public SentenceType getSentence() {
        return sentence;
    }

    /**
     * Sets the value of the sentence property.
     * 
     * @param value
     *     allowed object is
     *     {@link SentenceType }
     *     
     */
    public void setSentence(SentenceType value) {
        this.sentence = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
