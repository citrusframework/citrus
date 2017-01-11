/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.kubernetes.command;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import org.springframework.core.io.Resource;

import java.io.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public abstract class AbstractCreateCommand<R extends KubernetesResource, T extends AbstractKubernetesCommand> extends AbstractKubernetesCommand<R, T> {

    /** Template yml file to specify target */
    private String template;

    /**
     * Default constructor initializing the command name.
     *
     * @param name
     */
    public AbstractCreateCommand(String name) {
        super("create-" + name);
    }

    /**
     * Create input stream from template resource and add test variable support.
     * @param context
     * @return
     */
    protected InputStream getTemplateAsStream(TestContext context) {
        Resource templateResource = FileUtils.getFileResource(getTemplate(), context);
        String templateYml;
        try {
            templateYml = context.replaceDynamicContentInString(FileUtils.readToString(templateResource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read template resource", e);
        }
        return new ByteArrayInputStream(templateYml.getBytes());
    }

    /**
     * Gets the template.
     *
     * @return
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the template.
     *
     * @param template
     */
    public void setTemplate(String template) {
        this.template = template;
    }
}
