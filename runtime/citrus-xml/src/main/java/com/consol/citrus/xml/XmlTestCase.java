/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.xml;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

import com.consol.citrus.DefaultTestCase;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.xml.actions.CreateVariables;
import com.consol.citrus.xml.actions.Echo;
import com.consol.citrus.xml.actions.Print;
import com.consol.citrus.xml.actions.Sleep;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "test")
public class XmlTestCase {

    private final TestCase delegate = new DefaultTestCase();

    /**
     * Gets the test case.
     * @return
     */
    public TestCase getTestCase() {
        return delegate;
    }

    @XmlAttribute
    public void setName(String name) {
        delegate.setName(name);
    }

    @XmlAttribute
    public void setAuthor(String author) {
        delegate.getMetaInfo().setAuthor(author);
    }

    @XmlAttribute
    public void setStatus(TestCaseMetaInfo.Status status) {
        delegate.getMetaInfo().setStatus(status);
    }

    @XmlElement
    public void setActions(TestActions actions) {
        delegate.setActions(actions.getActions().stream()
                .filter(t -> t instanceof TestActionBuilder<?>)
                .map(TestActionBuilder.class::cast)
                .map(TestActionBuilder::build)
                .collect(Collectors.toList()));
    }

    private static class TestActions {
        @XmlElementRefs({
                @XmlElementRef(name = "echo", type = Echo.class, required = false),
                @XmlElementRef(name = "print", type = Print.class, required = false),
                @XmlElementRef(name = "sleep", type = Sleep.class, required = false),
                @XmlElementRef(name = "create-variables", type = CreateVariables.class, required = false),
        })
        @XmlAnyElement(lax = true)
        private List<Object> actions;

        public List<Object> getActions() {
            return actions;
        }
    }
}
