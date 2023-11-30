/*
 *  Copyright 2023 the original author or authors.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.selenium.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestActor;
import org.citrusframework.selenium.actions.AbstractSeleniumAction;
import org.citrusframework.selenium.actions.FillFormAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;

@XmlRootElement(name = "fill-form")
public class FillForm extends AbstractSeleniumAction.Builder<FillFormAction, FillForm> {

    private final FillFormAction.Builder delegate = new FillFormAction.Builder();

    @XmlElement(name = "fields")
    public void setFields(Fields fields) {
        delegate.fields(fields.getFields()
                .stream()
                .collect(Collectors.toMap(Fields.Field::getId, Fields.Field::getValue)));
    }

    @XmlElement(name = "json")
    public void setJson(String json) {
        this.delegate.fromJson(json);
    }

    @XmlAttribute
    public void setSubmit(String value) {
        this.delegate.submit(value);
    }

    @Override
    public FillForm description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public FillForm actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public FillForm browser(SeleniumBrowser seleniumBrowser) {
        delegate.browser(seleniumBrowser);
        return this;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "fields"
    })
    public static class Fields {
        @XmlElement(name = "field")
        private List<Field> fields;

        public void setFields(List<Field> fields) {
            this.fields = fields;
        }

        public List<Field> getFields() {
            if (fields == null) {
                fields = new ArrayList<>();
            }

            return fields;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {})
        public static class Field {
            @XmlAttribute
            private String id;
            @XmlAttribute
            private String value;

            @XmlElement(name = "value")
            private String valueData;

            public void setId(String id) {
                this.id = id;
            }

            public String getId() {
                return id;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public String getValue() {
                if (value == null) {
                    return getValueData();
                }

                return value;
            }

            public String getValueData() {
                return valueData;
            }

            public void setValueData(String valueData) {
                this.valueData = valueData;
            }
        }
    }

    @Override
    public FillFormAction build() {
        return delegate.build();
    }
}
