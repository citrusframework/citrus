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

import javax.xml.bind.annotation.*;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="statement"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;choice&gt;
 *                   &lt;element name="sql" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element ref="{http://www.citrusframework.org/schema/jdbc/message}create-table"/&gt;
 *                   &lt;element ref="{http://www.citrusframework.org/schema/jdbc/message}drop-table"/&gt;
 *                   &lt;element ref="{http://www.citrusframework.org/schema/jdbc/message}select"/&gt;
 *                   &lt;element ref="{http://www.citrusframework.org/schema/jdbc/message}insert"/&gt;
 *                   &lt;element ref="{http://www.citrusframework.org/schema/jdbc/message}update"/&gt;
 *                   &lt;element ref="{http://www.citrusframework.org/schema/jdbc/message}delete"/&gt;
 *                 &lt;/choice&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "statement"
})
@XmlRootElement(name = "execute")
public class Execute {

    @XmlElement(required = true)
    protected Statement statement;

    public Execute() {
    }

    public Execute(Statement statement) {
        this.statement = statement;
    }

    /**
     * Ruft den Wert der statement-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Statement }
     *
     */
    public Statement getStatement() {
        return statement;
    }

    /**
     * Legt den Wert der statement-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Statement }
     *
     */
    public void setStatement(Statement value) {
        this.statement = value;
    }


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
     *         &lt;element name="sql" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element ref="{http://www.citrusframework.org/schema/jdbc/message}create-table"/&gt;
     *         &lt;element ref="{http://www.citrusframework.org/schema/jdbc/message}drop-table"/&gt;
     *         &lt;element ref="{http://www.citrusframework.org/schema/jdbc/message}select"/&gt;
     *         &lt;element ref="{http://www.citrusframework.org/schema/jdbc/message}insert"/&gt;
     *         &lt;element ref="{http://www.citrusframework.org/schema/jdbc/message}update"/&gt;
     *         &lt;element ref="{http://www.citrusframework.org/schema/jdbc/message}delete"/&gt;
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
        "delete",
        "update",
        "insert",
        "select",
        "dropTable",
        "createTable",
        "sql"
    })
    public static class Statement {

        protected Delete delete;
        protected Update update;
        protected Insert insert;
        protected Select select;
        @XmlElement(name = "drop-table")
        protected DropTable dropTable;
        @XmlElement(name = "create-table")
        protected CreateTable createTable;
        protected String sql;

        public Statement() {
        }

        public Statement(Delete delete) {
            this.delete = delete;
        }

        public Statement(Update update) {
            this.update = update;
        }

        public Statement(Insert insert) {
            this.insert = insert;
        }

        public Statement(Select select) {
            this.select = select;
        }

        public Statement(DropTable dropTable) {
            this.dropTable = dropTable;
        }

        public Statement(CreateTable createTable) {
            this.createTable = createTable;
        }

        public Statement(String sql) {
            this.sql = sql;
        }

        /**
         * Ruft den Wert der delete-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Delete }
         *     
         */
        public Delete getDelete() {
            return delete;
        }

        /**
         * Legt den Wert der delete-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Delete }
         *     
         */
        public void setDelete(Delete value) {
            this.delete = value;
        }

        /**
         * Ruft den Wert der update-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Update }
         *     
         */
        public Update getUpdate() {
            return update;
        }

        /**
         * Legt den Wert der update-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Update }
         *     
         */
        public void setUpdate(Update value) {
            this.update = value;
        }

        /**
         * Ruft den Wert der insert-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Insert }
         *     
         */
        public Insert getInsert() {
            return insert;
        }

        /**
         * Legt den Wert der insert-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Insert }
         *     
         */
        public void setInsert(Insert value) {
            this.insert = value;
        }

        /**
         * Ruft den Wert der select-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Select }
         *     
         */
        public Select getSelect() {
            return select;
        }

        /**
         * Legt den Wert der select-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Select }
         *     
         */
        public void setSelect(Select value) {
            this.select = value;
        }

        /**
         * Ruft den Wert der dropTable-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link DropTable }
         *     
         */
        public DropTable getDropTable() {
            return dropTable;
        }

        /**
         * Legt den Wert der dropTable-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link DropTable }
         *     
         */
        public void setDropTable(DropTable value) {
            this.dropTable = value;
        }

        /**
         * Ruft den Wert der createTable-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link CreateTable }
         *     
         */
        public CreateTable getCreateTable() {
            return createTable;
        }

        /**
         * Legt den Wert der createTable-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link CreateTable }
         *     
         */
        public void setCreateTable(CreateTable value) {
            this.createTable = value;
        }

        /**
         * Ruft den Wert der sql-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSql() {
            return sql;
        }

        /**
         * Legt den Wert der sql-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSql(String value) {
            this.sql = value;
        }

    }

}
