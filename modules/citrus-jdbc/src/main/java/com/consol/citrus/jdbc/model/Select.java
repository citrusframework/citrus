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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "columns",
    "from",
    "where"
})
@XmlRootElement(name = "select")
public class Select {

    @XmlElement(required = true)
    protected String columns;
    @XmlElement(required = true)
    protected String from;
    protected String where;

    /**
     * Gets the columns.
     *
     * @return
     */
    public String getColumns() {
        return columns;
    }

    /**
     * Sets the columns.
     *
     * @param columns
     */
    public void setColumns(String columns) {
        this.columns = columns;
    }

    /**
     * Gets the from.
     *
     * @return
     */
    public String getFrom() {
        return from;
    }

    /**
     * Sets the from.
     *
     * @param from
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Gets the where.
     *
     * @return
     */
    public String getWhere() {
        return where;
    }

    /**
     * Sets the where.
     *
     * @param where
     */
    public void setWhere(String where) {
        this.where = where;
    }
}
