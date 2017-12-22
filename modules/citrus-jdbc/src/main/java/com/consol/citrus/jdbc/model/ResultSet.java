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
import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="column" maxOccurs="unbounded"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="row" maxOccurs="unbounded"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "columns",
    "rows",
    "affectedRows"
})
@XmlRootElement(name = "result-set")
public class ResultSet implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @XmlElement(name = "column", required = true)
    protected List<Column> columns;
    @XmlElement(name = "row", required = true)
    protected List<Row> rows;

    @XmlAttribute(name = "affected-rows")
    protected int affectedRows = 0;

    @XmlTransient
    private boolean closed = false;
    @XmlTransient
    private AtomicInteger cursor = new AtomicInteger(0);

    public ResultSet() {
    }

    public ResultSet(int affectedRows) {
        this.affectedRows = affectedRows;
    }

    /**
     * Add columns.
     *
     * @param columns
     */
    public ResultSet columns(Column... columns) {
        getColumns().addAll(Arrays.asList(columns));
        return this;
    }

    /**
     * Add rows.
     *
     * @param rows
     */
    public ResultSet rows(Row... rows) {
        getRows().addAll(Arrays.asList(rows));
        return this;
    }

    /**
     * Adds new column with name and type information.
     * @param column
     * @return
     */
    public ResultSet addColumn(Column column) {
        this.columns.add(column);
        return this;
    }

    /**
     * Adds new row with values.
     * @param row
     * @return
     */
    public ResultSet addRow(Row row) {
        this.rows.add(row);
        return this;
    }

    /**
     * Gets next row in this result set based on cursor position.
     * @return
     * @throws SQLException
     */
    public Row getNextRow() throws SQLException {
        if (closed) {
            throw new SQLException("Result set already closed");
        }

        return rows.get(cursor.getAndIncrement());
    }

    /**
     * Gets current row index.
     * @return
     * @throws SQLException
     */
    public int getRow() throws SQLException {
        if (closed) {
            throw new SQLException("Result set already closed");
        }

        return cursor.get() + 1;
    }

    /**
     * Close result set - no further access to rows and columns allowed.
     * @throws SQLException
     */
    public void close() throws SQLException {
        this.closed = true;
    }

    /**
     * Gets the value of the columns property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the columns property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getColumns().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Column }
     * 
     * 
     */
    public List<Column> getColumns() {
        if (columns == null) {
            columns = new ArrayList<Column>();
        }
        return this.columns;
    }

    /**
     * Gets the value of the rows property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rows property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRows().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Row }
     * 
     * 
     */
    public List<Row> getRows() {
        if (rows == null) {
            rows = new ArrayList<Row>();
        }
        return this.rows;
    }

    /**
     * Gets the affectedRows.
     *
     * @return
     */
    public int getAffectedRows() {
        return affectedRows;
    }

    /**
     * Sets the affectedRows.
     *
     * @param affectedRows
     */
    public void setAffectedRows(int affectedRows) {
        this.affectedRows = affectedRows;
    }

    /**
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Column implements Serializable {

        @XmlAttribute(name = "name", required = true)
        protected String name;
        @XmlAttribute(name = "type")
        protected String type;

        public Column() {
        }

        public Column(String name) {
            this.name = name;
        }

        public Column(String name, String type) {
            this.name = name;
            this.type = type;
        }

        /**
         * Gets the name.
         *
         * @return
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the name.
         *
         * @param name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets the type.
         *
         * @return
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the type.
         *
         * @param type
         */
        public void setType(String type) {
            this.type = type;
        }
    }


    /**
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "values"
    })
    public static class Row implements Serializable {

        @XmlElement(name = "value", required = true)
        protected List<String> values;

        public Row() {
        }

        public Row(List<String> values) {
            this.values = values;
        }

        public Row(String... values) {
            this.values = Arrays.asList(values);
        }

        /**
         * Gets the value of the values property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the values property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getValues().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getValues() {
            if (values == null) {
                values = new ArrayList<String>();
            }
            return this.values;
        }

    }

}
