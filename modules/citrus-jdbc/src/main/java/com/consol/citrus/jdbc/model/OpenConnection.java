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
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="username" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "properties"
})
@XmlRootElement(name = "open-connection")
public class OpenConnection {

    @XmlElement(name = "property")
    protected List<Property> properties;

    public OpenConnection() {
    }

    public OpenConnection(List<Property> properties) {
        this.properties = properties;
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
     * {@link Property }
     *
     *
     */
    public List<Property> getProperties() {
        if (properties == null) {
            properties = new ArrayList<Property>();
        }
        return this.properties;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Property implements Serializable {

        @XmlAttribute(name = "name", required = true)
        protected String name;
        @XmlAttribute(name = "value")
        protected String value;

        public Property() {
        }

        public Property(String name) {
            this.name = name;
        }

        public Property(String name, String value) {
            this.name = name;
            this.value = value;
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
         * Gets the value.
         *
         * @return
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value.
         *
         * @param value
         */
        public void setValue(String value) {
            this.value = value;
        }
    }

}
