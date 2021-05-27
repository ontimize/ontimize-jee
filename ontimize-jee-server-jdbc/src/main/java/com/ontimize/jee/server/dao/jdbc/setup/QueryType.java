//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantacion de la referencia de
// enlace (JAXB) XML v2.2.8-b130911.1802
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Todas las modificaciones realizadas en este archivo se perderan si se vuelve a compilar el
// esquema de origen.
// Generado el: 2018.09.11 a las 05:39:50 PM CEST
//


package com.ontimize.jee.server.dao.jdbc.setup;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>
 * Clase Java para QueryType complex type.
 *
 * <p>
 * El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 *
 * <pre>
 * &lt;complexType name="QueryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AmbiguousColumns" type="{http://www.ontimize.com/schema/jdbc}AmbiguousColumnsType" minOccurs="0"/>
 *         &lt;element name="ValidColumns" type="{http://www.ontimize.com/schema/jdbc}ColumnGroupType" minOccurs="0"/>
 *         &lt;element name="FunctionColumns" type="{http://www.ontimize.com/schema/jdbc}FunctionColumnsType" minOccurs="0"/>
 *         &lt;element name="OrderColumns" type="{http://www.ontimize.com/schema/jdbc}OrderColumnsType" minOccurs="0"/>
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
        "orderColumns",
        "sentence"
})
public class QueryType {

    @XmlElement(name = "AmbiguousColumns")
    protected AmbiguousColumnsType ambiguousColumns;

    @XmlElement(name = "ValidColumns")
    protected ColumnGroupType validColumns;

    @XmlElement(name = "FunctionColumns")
    protected FunctionColumnsType functionColumns;

    @XmlElement(name = "OrderColumns")
    protected OrderColumnsType orderColumns;

    @XmlElement(name = "Sentence", required = true)
    protected SentenceType sentence;

    @XmlAttribute(name = "id")
    protected String id;

    /**
     * Obtiene el valor de la propiedad ambiguousColumns.
     * @return possible object is {@link AmbiguousColumnsType }
     *
     */
    public AmbiguousColumnsType getAmbiguousColumns() {
        return ambiguousColumns;
    }

    /**
     * Define el valor de la propiedad ambiguousColumns.
     * @param value allowed object is {@link AmbiguousColumnsType }
     *
     */
    public void setAmbiguousColumns(AmbiguousColumnsType value) {
        this.ambiguousColumns = value;
    }

    /**
     * Obtiene el valor de la propiedad validColumns.
     * @return possible object is {@link ColumnGroupType }
     *
     */
    public ColumnGroupType getValidColumns() {
        return validColumns;
    }

    /**
     * Define el valor de la propiedad validColumns.
     * @param value allowed object is {@link ColumnGroupType }
     *
     */
    public void setValidColumns(ColumnGroupType value) {
        this.validColumns = value;
    }

    /**
     * Obtiene el valor de la propiedad functionColumns.
     * @return possible object is {@link FunctionColumnsType }
     *
     */
    public FunctionColumnsType getFunctionColumns() {
        return functionColumns;
    }

    /**
     * Define el valor de la propiedad functionColumns.
     * @param value allowed object is {@link FunctionColumnsType }
     *
     */
    public void setFunctionColumns(FunctionColumnsType value) {
        this.functionColumns = value;
    }

    /**
     * Obtiene el valor de la propiedad orderColumns.
     * @return possible object is {@link OrderColumnsType }
     *
     */
    public OrderColumnsType getOrderColumns() {
        return orderColumns;
    }

    /**
     * Define el valor de la propiedad orderColumns.
     * @param value allowed object is {@link OrderColumnsType }
     *
     */
    public void setOrderColumns(OrderColumnsType value) {
        this.orderColumns = value;
    }

    /**
     * Obtiene el valor de la propiedad sentence.
     * @return possible object is {@link SentenceType }
     *
     */
    public SentenceType getSentence() {
        return sentence;
    }

    /**
     * Define el valor de la propiedad sentence.
     * @param value allowed object is {@link SentenceType }
     *
     */
    public void setSentence(SentenceType value) {
        this.sentence = value;
    }

    /**
     * Obtiene el valor de la propiedad id.
     * @return possible object is {@link String }
     *
     */
    public String getId() {
        return id;
    }

    /**
     * Define el valor de la propiedad id.
     * @param value allowed object is {@link String }
     *
     */
    public void setId(String value) {
        this.id = value;
    }

}
