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
    "table",
    "set",
    "where"
})
@XmlRootElement(name = "update")
public class Update {

    @XmlElement(required = true)
    protected String table;
    @XmlElement(required = true)
    protected String set;
    protected String where;

    /**
     * Gets the table.
     *
     * @return
     */
    public String getTable() {
        return table;
    }

    /**
     * Sets the table.
     *
     * @param table
     */
    public void setTable(String table) {
        this.table = table;
    }

    /**
     * Gets the set.
     *
     * @return
     */
    public String getSet() {
        return set;
    }

    /**
     * Sets the set.
     *
     * @param set
     */
    public void setSet(String set) {
        this.set = set;
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
