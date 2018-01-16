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
    "into",
    "values"
})
@XmlRootElement(name = "insert")
public class Insert {

    @XmlElement(required = true)
    protected String into;
    @XmlElement(required = true)
    protected String values;

    /**
     * Gets the into.
     *
     * @return
     */
    public String getInto() {
        return into;
    }

    /**
     * Sets the into.
     *
     * @param into
     */
    public void setInto(String into) {
        this.into = into;
    }

    /**
     * Gets the values.
     *
     * @return
     */
    public String getValues() {
        return values;
    }

    /**
     * Sets the values.
     *
     * @param values
     */
    public void setValues(String values) {
        this.values = values;
    }
}
