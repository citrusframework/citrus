/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.rmi.model;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.xml.Jaxb2Marshaller;
import com.consol.citrus.xml.Marshaller;
import com.consol.citrus.xml.Unmarshaller;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiMarshaller implements Marshaller, Unmarshaller {

    private final Jaxb2Marshaller marshaller;

    public RmiMarshaller() {
        this.marshaller = new Jaxb2Marshaller(
                new ClassPathResource("com/consol/citrus/schema/citrus-rmi-message.xsd"), RmiServiceInvocation.class, RmiServiceResult.class);
    }

    public void marshal(Object graph, Result result) {
        try {
            marshaller.marshal(graph, result);
        } catch (JAXBException e) {
            throw new CitrusRuntimeException("Failed to marshal object graph", e);
        }
    }

    public Object unmarshal(Source source) {
        try {
            return marshaller.unmarshal(source);
        } catch (JAXBException e) {
            throw new CitrusRuntimeException("Failed to unmarshal source", e);
        }
    }
}
