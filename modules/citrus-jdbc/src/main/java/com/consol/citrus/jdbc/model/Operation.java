/*
 * Copyright 2006-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.jdbc.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element ref="{http://www.citrusframework.org/schema/jdbc/message}open-connection"/&gt;
 *         &lt;element ref="{http://www.citrusframework.org/schema/jdbc/message}close-connection"/&gt;
 *         &lt;element ref="{http://www.citrusframework.org/schema/jdbc/message}create-statement"/&gt;
 *         &lt;element ref="{http://www.citrusframework.org/schema/jdbc/message}create-table"/&gt;
 *         &lt;element ref="{http://www.citrusframework.org/schema/jdbc/message}select"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "execute",
    "createStatement",
    "closeConnection",
    "openConnection"
})
@XmlRootElement(name = "operation")
public class Operation {

    protected Execute execute;
    @XmlElement(name = "create-statement")
    protected CreateStatement createStatement;
    @XmlElement(name = "close-connection")
    protected CloseConnection closeConnection;
    @XmlElement(name = "open-connection")
    protected OpenConnection openConnection;

    /**
     * Default constructor.
     */
    public Operation() {
        super();
    }

    public Operation(OpenConnection operation) {
        this.openConnection = operation;
    }

    public Operation(CloseConnection operation) {
        this.closeConnection = operation;
    }

    public Operation(CreateStatement operation) {
        this.createStatement = operation;
    }

    public Operation(Execute operation) {
        this.execute = operation;
    }

    /**
     * Ruft den Wert der select-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Execute }
     *     
     */
    public Execute getExecute() {
        return execute;
    }

    /**
     * Legt den Wert der select-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Execute }
     *     
     */
    public void setExecute(Execute value) {
        this.execute = value;
    }

    /**
     * Ruft den Wert der createStatement-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CreateStatement }
     *     
     */
    public CreateStatement getCreateStatement() {
        return createStatement;
    }

    /**
     * Legt den Wert der createStatement-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CreateStatement }
     *     
     */
    public void setCreateStatement(CreateStatement value) {
        this.createStatement = value;
    }

    /**
     * Ruft den Wert der closeConnection-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CloseConnection }
     *     
     */
    public CloseConnection getCloseConnection() {
        return closeConnection;
    }

    /**
     * Legt den Wert der closeConnection-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CloseConnection }
     *     
     */
    public void setCloseConnection(CloseConnection value) {
        this.closeConnection = value;
    }

    /**
     * Ruft den Wert der openConnection-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link OpenConnection }
     *     
     */
    public OpenConnection getOpenConnection() {
        return openConnection;
    }

    /**
     * Legt den Wert der openConnection-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenConnection }
     *     
     */
    public void setOpenConnection(OpenConnection value) {
        this.openConnection = value;
    }

}
