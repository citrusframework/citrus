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

package com.consol.citrus.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.xml.container.Template;
import org.springframework.core.io.Resource;

/**
 * @author Christoph Deppisch
 */
public class TemplateLoader implements ReferenceResolverAware {

    private final JAXBContext jaxbContext;
    private final String source;

    private Template template;
    private ReferenceResolver referenceResolver;

    /**
     * Default constructor.
     */
    public TemplateLoader(String source) {
        this.source = source;
        try {
            jaxbContext = JAXBContext.newInstance("com.consol.citrus.xml");
        } catch (JAXBException e) {
            throw new CitrusRuntimeException("Failed to create TemplateLoader instance", e);
        }
    }

    public Template load() {
        if (template == null) {
            Resource xmlSource = FileUtils.getFileResource(source);

            try {
                template = jaxbContext.createUnmarshaller()
                        .unmarshal(new StringSource(FileUtils.readToString(xmlSource)), Template.class)
                        .getValue();
            } catch (JAXBException | IOException e) {
                throw new CitrusRuntimeException("Failed to load XML template for source '" + source + "'", e);
            }

            template.setReferenceResolver(referenceResolver);
        }

        return template;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    /**
     * Adds reference resolver in builder pattern style.
     * @param referenceResolver
     * @return
     */
    public TemplateLoader withReferenceResolver(ReferenceResolver referenceResolver) {
        setReferenceResolver(referenceResolver);
        return this;
    }
}
