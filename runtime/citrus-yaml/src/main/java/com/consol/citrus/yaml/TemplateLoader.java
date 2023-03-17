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

package com.consol.citrus.yaml;

import java.io.IOException;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.yaml.container.Template;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * @author Christoph Deppisch
 */
public class TemplateLoader implements ReferenceResolverAware {

    private final Yaml yaml;
    private final String source;

    private Template template;
    private ReferenceResolver referenceResolver;

    /**
     * Default constructor.
     */
    public TemplateLoader(String source) {
        this.source = source;
        yaml = new Yaml(new Constructor(Template.class, new LoaderOptions()));
    }

    public Template load() {
        if (template == null) {
            Resource yamlSource = FileUtils.getFileResource(source);

            try {
                template = yaml.load(FileUtils.readToString(yamlSource));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to load YAML template for source '" + source + "'", e);
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
