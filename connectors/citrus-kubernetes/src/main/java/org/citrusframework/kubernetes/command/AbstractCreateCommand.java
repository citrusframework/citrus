/*
 * Copyright the original author or authors.
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

package org.citrusframework.kubernetes.command;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;

/**
 * @since 2.7
 */
public abstract class AbstractCreateCommand<T extends HasMetadata, L extends KubernetesResourceList<T>, R extends io.fabric8.kubernetes.client.dsl.Resource<T>, C extends KubernetesCommand<T, T>>
        extends AbstractClientCommand<T, T, L, R, C> {

    /** Optional resource object to create */
    private T resource;

    /** Template yaml file to specify target */
    private String template;

    /** Template yaml resource to specify target */
    private Resource templateResource;

    /**
     * Default constructor initializing the command name.
     *
     * @param name
     */
    public AbstractCreateCommand(String name) {
        super("create-" + name);
    }

    @Override
    public void execute(MixedOperation<T, L, R> operation, TestContext context) {
        if (resource != null) {
            T result = operation.resource(resource).create();
            setCommandResult(new CommandResult<>(result));
        } else if (StringUtils.hasText(getTemplate()) || templateResource != null) {
            R resource = operation.load(getTemplateAsStream(context));
            T result = operation.resource(resource.item()).create();
            setCommandResult(new CommandResult<>(result));
        } else {
            T result = operation.resource(specify(getResourceName(context), context)).create();
            setCommandResult(new CommandResult<>(result));
        }
    }

    /**
     * Specify resource to create.
     * @param name
     * @param context
     * @return
     */
    protected abstract T specify(String name, TestContext context);

    /**
     * Create input stream from template resource and add test variable support.
     * @param context
     * @return
     */
    protected InputStream getTemplateAsStream(TestContext context) {
        Resource resource;
        if (templateResource != null) {
            resource = templateResource;
        } else {
            resource = FileUtils.getFileResource(template, context);
        }

        String templateYml;
        try {
            templateYml = context.replaceDynamicContentInString(FileUtils.readToString(resource));
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

    /**
     * Gets the templateResource.
     *
     * @return
     */
    public Resource getTemplateResource() {
        return templateResource;
    }

    /**
     * Sets the templateResource.
     *
     * @param templateResource
     */
    public void setTemplateResource(Resource templateResource) {
        this.templateResource = templateResource;
    }

    /**
     * Gets the resource.
     *
     * @return
     */
    public T getResource() {
        return resource;
    }

    /**
     * Sets the resource.
     *
     * @param resource
     */
    public void setResource(T resource) {
        this.resource = resource;
    }
}
