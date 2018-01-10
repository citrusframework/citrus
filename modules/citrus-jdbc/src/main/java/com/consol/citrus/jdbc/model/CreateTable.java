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
    "name"
})
@XmlRootElement(name = "create-table")
public class CreateTable {

    @XmlElement(required = true)
    protected String name;
    @XmlAttribute(name = "create-or-replace")
    protected Boolean createOrReplace;

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
     * Gets the createOrReplace.
     *
     * @return
     */
    public Boolean getCreateOrReplace() {
        return createOrReplace;
    }

    /**
     * Sets the createOrReplace.
     *
     * @param createOrReplace
     */
    public void setCreateOrReplace(Boolean createOrReplace) {
        this.createOrReplace = createOrReplace;
    }
}
