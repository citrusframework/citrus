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

import java.io.IOException;
import java.util.regex.Pattern;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.citrusframework.container.TemplateLoader;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.xml.container.Template;

/**
 * @author Christoph Deppisch
 */
public class XmlTemplateLoader implements TemplateLoader, ReferenceResolverAware {

    private final JAXBContext jaxbContext;

    private ReferenceResolver referenceResolver;

    private static final Pattern NAMESPACE_IS_SET = Pattern.compile("^\\s*<(\\w+:)?template .*xmlns(:\\w+)?=\\s*\".*>\\s*$", Pattern.DOTALL);

    /**
     * Default constructor.
     */
    public XmlTemplateLoader() {
        try {
            jaxbContext = JAXBContext.newInstance("org.citrusframework.xml");
        } catch (JAXBException e) {
            throw new CitrusRuntimeException("Failed to create XmlTemplateLoader instance", e);
        }
    }

    @Override
    public org.citrusframework.container.Template load(String filePath) {
        try {
            Resource xmlSource = FileUtils.getFileResource(filePath);
            Template template = jaxbContext.createUnmarshaller()
                    .unmarshal(new StringSource(applyNamespace(FileUtils.readToString(xmlSource))), Template.class)
                    .getValue();
            template.setReferenceResolver(referenceResolver);
            return template.build();
        } catch (JAXBException | IOException e) {
            throw new CitrusRuntimeException("Failed to load XML template for source '" + filePath + "'", e);
        }
    }

    /**
     * Automatically applies Citrus test namespace if non is set on the root element.
     * @param xmlSource
     * @return
     */
    public static String applyNamespace(String xmlSource) {
        if (NAMESPACE_IS_SET.matcher(xmlSource).matches()) {
            return xmlSource;
        }

        return xmlSource.replace("<template ", String.format("<template xmlns=\"%s\" ", XmlTestLoader.TEST_NS));
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
    public XmlTemplateLoader withReferenceResolver(ReferenceResolver referenceResolver) {
        setReferenceResolver(referenceResolver);
        return this;
    }
}
