//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de
// enlace (JAXB) XML v2.2.8-b130911.1802
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el
// esquema de origen.
// Generado el: 2018.09.11 a las 05:39:50 PM CEST
//


package com.ontimize.jee.server.dao.jdbc.setup;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>
 * Clase Java para OrderColumnType complex type.
 *
 * <p>
 * El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 *
 * <pre>
 * &lt;complexType name="OrderColumnType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="type" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="ASC"/>
 *             &lt;enumeration value="DES"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrderColumnType")
public class OrderColumnType {

    @XmlAttribute(name = "name", required = true)
    protected String name;

    @XmlAttribute(name = "type", required = true)
    protected String type;

    /**
     * Obtiene el valor de la propiedad name.
     * @return possible object is {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Define el valor de la propiedad name.
     * @param value allowed object is {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Obtiene el valor de la propiedad type.
     * @return possible object is {@link String }
     *
     */
    public String getType() {
        return type;
    }

    /**
     * Define el valor de la propiedad type.
     * @param value allowed object is {@link String }
     *
     */
    public void setType(String value) {
        this.type = value;
    }

}
