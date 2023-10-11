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

package org.citrusframework.yaml;

import java.io.IOException;

import org.citrusframework.container.TemplateLoader;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.yaml.container.Template;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * @author Christoph Deppisch
 */
public class YamlTemplateLoader implements ReferenceResolverAware, TemplateLoader {

    private final Yaml yaml;

    private ReferenceResolver referenceResolver;

    /**
     * Default constructor.
     */
    public YamlTemplateLoader() {
        yaml = new Yaml(new Constructor(Template.class, new LoaderOptions()));
    }

    @Override
    public org.citrusframework.container.Template load(String filePath) {
        try {
            Resource yamlSource = FileUtils.getFileResource(filePath);
            Template template = yaml.load(FileUtils.readToString(yamlSource));
            template.setReferenceResolver(referenceResolver);
            return template.build();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to load YAML template for source '" + filePath + "'", e);
        }
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
    public YamlTemplateLoader withReferenceResolver(ReferenceResolver referenceResolver) {
        setReferenceResolver(referenceResolver);
        return this;
    }
}
