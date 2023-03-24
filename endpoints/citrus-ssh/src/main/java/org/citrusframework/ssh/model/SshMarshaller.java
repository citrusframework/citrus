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

package org.citrusframework.ssh.model;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.xml.Jaxb2Marshaller;
import org.citrusframework.xml.Marshaller;
import org.citrusframework.xml.Unmarshaller;
import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Christoph Deppisch
 * @since 2.1
 */
public class SshMarshaller implements Marshaller, Unmarshaller {

    /** Logger */
    private static final Logger log = LoggerFactory.getLogger(SshMarshaller.class);

    private final Jaxb2Marshaller marshaller;

    public SshMarshaller() {
        this.marshaller = new Jaxb2Marshaller(
                new ClassPathResource("org/citrusframework/schema/citrus-ssh-message.xsd"), SshRequest.class, SshResponse.class);
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
