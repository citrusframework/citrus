/*
 * Copyright 2022 the original author or authors.
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

package com.consol.citrus.groovy.dsl.actions.model;

import java.io.StringWriter;

import groovy.xml.MarkupBuilder;

/**
 * @author Christoph Deppisch
 */
public class XmlBodySpec extends BodySpec {

    private MarkupBuilder markupBuilder;
    private StringWriter xmlWriter;

    @Override
    public Object methodMissing(String name, Object argLine) {
        if (markupBuilder == null) {
            xmlWriter = new StringWriter();
            markupBuilder = new MarkupBuilder(xmlWriter);
        }

        return markupBuilder.invokeMethod(name, argLine);
    }

    @Override
    public String get(Object result) {
        if (xmlWriter != null) {
            return xmlWriter.toString();
        }

        return result.toString();
    }
}
