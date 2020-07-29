//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de
// enlace (JAXB) XML v2.2.8-b130911.1802
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el
// esquema de origen.
// Generado el: 2018.09.11 a las 05:39:50 PM CEST
//


package com.ontimize.jee.server.dao.jdbc.setup;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>
 * Clase Java para OrderColumnsType complex type.
 *
 * <p>
 * El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 *
 * <pre>
 * &lt;complexType name="OrderColumnsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="OrderColumn" type="{http://www.ontimize.com/schema/jdbc}OrderColumnType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrderColumnsType", propOrder = {
        "orderColumn"
})
public class OrderColumnsType {

    @XmlElement(name = "OrderColumn", required = true)
    protected List<OrderColumnType> orderColumn;

    /**
     * Gets the value of the orderColumn property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any
     * modification you make to the returned list will be present inside the JAXB object. This is why
     * there is not a <CODE>set</CODE> method for the orderColumn property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getOrderColumn().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link OrderColumnType }
     *
     *
     */
    public List<OrderColumnType> getOrderColumn() {
        if (orderColumn == null) {
            orderColumn = new ArrayList<OrderColumnType>();
        }
        return this.orderColumn;
    }

}
