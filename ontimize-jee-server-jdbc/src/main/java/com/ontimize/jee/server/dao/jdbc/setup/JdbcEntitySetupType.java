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
 * 
 * 				Elemento raiz para la configuracion de la entidad.
 * 			
 * 
 * <p>Java class for JdbcEntitySetupType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="JdbcEntitySetupType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DeleteKeys" type="{http://www.ontimize.com/schema/jdbc}ColumnGroupType" minOccurs="0"/>
 *         &lt;element name="UpdateKeys" type="{http://www.ontimize.com/schema/jdbc}ColumnGroupType" minOccurs="0"/>
 *         &lt;element name="GeneratedKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Queries" type="{http://www.ontimize.com/schema/jdbc}QueriesType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="table" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="schema" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="catalog" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="datasource" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="sqlhandler" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="nameconverter" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "JdbcEntitySetupType", propOrder = {
    "deleteKeys",
    "updateKeys",
    "generatedKey",
    "queries"
})
public class JdbcEntitySetupType {

    @XmlElement(name = "DeleteKeys")
    protected ColumnGroupType deleteKeys;
    @XmlElement(name = "UpdateKeys")
    protected ColumnGroupType updateKeys;
    @XmlElement(name = "GeneratedKey")
    protected String generatedKey;
    @XmlElement(name = "Queries")
    protected QueriesType queries;
    @XmlAttribute(name = "table")
    protected String table;
    @XmlAttribute(name = "schema")
    protected String schema;
    @XmlAttribute(name = "catalog")
    protected String catalog;
    @XmlAttribute(name = "datasource")
    protected String datasource;
    @XmlAttribute(name = "sqlhandler")
    protected String sqlhandler;
    @XmlAttribute(name = "nameconverter")
    protected String nameconverter;

    /**
     * Gets the value of the deleteKeys property.
     * 
     * @return
     *     possible object is
     *     {@link ColumnGroupType }
     *     
     */
    public ColumnGroupType getDeleteKeys() {
        return deleteKeys;
    }

    /**
     * Sets the value of the deleteKeys property.
     * 
     * @param value
     *     allowed object is
     *     {@link ColumnGroupType }
     *     
     */
    public void setDeleteKeys(ColumnGroupType value) {
        this.deleteKeys = value;
    }

    /**
     * Gets the value of the updateKeys property.
     * 
     * @return
     *     possible object is
     *     {@link ColumnGroupType }
     *     
     */
    public ColumnGroupType getUpdateKeys() {
        return updateKeys;
    }

    /**
     * Sets the value of the updateKeys property.
     * 
     * @param value
     *     allowed object is
     *     {@link ColumnGroupType }
     *     
     */
    public void setUpdateKeys(ColumnGroupType value) {
        this.updateKeys = value;
    }

    /**
     * Gets the value of the generatedKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGeneratedKey() {
        return generatedKey;
    }

    /**
     * Sets the value of the generatedKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGeneratedKey(String value) {
        this.generatedKey = value;
    }

    /**
     * Gets the value of the queries property.
     * 
     * @return
     *     possible object is
     *     {@link QueriesType }
     *     
     */
    public QueriesType getQueries() {
        return queries;
    }

    /**
     * Sets the value of the queries property.
     * 
     * @param value
     *     allowed object is
     *     {@link QueriesType }
     *     
     */
    public void setQueries(QueriesType value) {
        this.queries = value;
    }

    /**
     * Gets the value of the table property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTable() {
        return table;
    }

    /**
     * Sets the value of the table property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTable(String value) {
        this.table = value;
    }

    /**
     * Gets the value of the schema property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Sets the value of the schema property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchema(String value) {
        this.schema = value;
    }

    /**
     * Gets the value of the catalog property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCatalog() {
        return catalog;
    }

    /**
     * Sets the value of the catalog property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCatalog(String value) {
        this.catalog = value;
    }

    /**
     * Gets the value of the datasource property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDatasource() {
        return datasource;
    }

    /**
     * Sets the value of the datasource property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDatasource(String value) {
        this.datasource = value;
    }

    /**
     * Gets the value of the sqlhandler property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSqlhandler() {
        return sqlhandler;
    }

    /**
     * Sets the value of the sqlhandler property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSqlhandler(String value) {
        this.sqlhandler = value;
    }

    /**
     * Gets the value of the nameconverter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNameconverter() {
        return nameconverter;
    }

    /**
     * Sets the value of the nameconverter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNameconverter(String value) {
        this.nameconverter = value;
    }

}
