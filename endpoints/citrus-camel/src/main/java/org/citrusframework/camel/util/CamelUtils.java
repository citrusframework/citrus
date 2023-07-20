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

package org.citrusframework.camel.util;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * Camel utilities.
 */
public class CamelUtils {

    private static volatile JAXBContext context;

    /**
     * Creates new Camel JaxB context.
     * @return
     * @throws JAXBException
     */
    public static JAXBContext getJaxbContext() throws JAXBException {
        if (context == null) {
            synchronized(CamelUtils.class) {
                context = JAXBContext.newInstance("org.apache.camel:org.apache.camel.model:org.apache.camel.model.cloud:" +
                        "org.apache.camel.model.config:org.apache.camel.model.dataformat:org.apache.camel.model.language:" +
                        "org.apache.camel.model.loadbalancer:org.apache.camel.model.rest:org.apache.camel.model.transformer:" +
                        "org.apache.camel.model.validator:org.apache.camel.core.xml:org.apache.camel.spring.xml", CamelUtils.class.getClassLoader());
            }
        }

        return context;
    }
}
