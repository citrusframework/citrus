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

package org.citrusframework.xml;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import java.io.IOException;

import org.springframework.oxm.MarshallingFailureException;
import org.springframework.oxm.UnmarshallingFailureException;
import org.springframework.oxm.XmlMappingException;

/**
 * Spring Oxm marshaller delegates to given internal marshaller.
 * @author Christoph Deppisch
 */
public class SpringMarshallerAdapter implements org.springframework.oxm.Marshaller, org.springframework.oxm.Unmarshaller {

    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;

    public SpringMarshallerAdapter(Marshaller marshaller) {
        this.marshaller = marshaller;

        if (marshaller instanceof Unmarshaller) {
            this.unmarshaller = (Unmarshaller) marshaller;
        } else {
            throw new IllegalArgumentException("Failed to initialize Spring Oxm marshaller - missing proper unmarshaller delegate");
        }
    }

    public SpringMarshallerAdapter(Marshaller marshaller, Unmarshaller unmarshaller) {
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public Object unmarshal(Source source) throws IOException, XmlMappingException {
        try {
            return unmarshaller.unmarshal(source);
        } catch (Exception e) {
            throw new UnmarshallingFailureException("Failed to unmarshal source", e.getCause());
        }
    }

    @Override
    public void marshal(Object graph, Result result) throws IOException, XmlMappingException {
        try {
            marshaller.marshal(graph, result);
        } catch (Exception e) {
            throw new MarshallingFailureException("Failed to marshal object graph", e.getCause());
        }
    }
}
