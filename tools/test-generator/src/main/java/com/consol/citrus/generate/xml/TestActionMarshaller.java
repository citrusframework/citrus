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

package com.consol.citrus.generate.xml;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Result;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.xml.Jaxb2Marshaller;
import com.consol.citrus.xml.Marshaller;
import com.consol.citrus.xml.namespace.CitrusNamespacePrefixMapper;
import org.springframework.core.io.Resource;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class TestActionMarshaller implements Marshaller {

    private final Jaxb2Marshaller marshaller;

    public TestActionMarshaller(Resource[] schemas, String... contextPaths) {
        this.marshaller = new Jaxb2Marshaller(schemas, contextPaths);

        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FRAGMENT, true);

        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new CitrusNamespacePrefixMapper());
    }

    @Override
    public void marshal(Object graph, Result result) {
        try {
            marshaller.marshal(graph, result);
        } catch (JAXBException e) {
            throw new CitrusRuntimeException("Failed to marshal object graph", e);
        }
    }
}
