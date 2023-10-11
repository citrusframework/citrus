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

package org.citrusframework.kubernetes.command;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.fabric8.kubernetes.api.model.Doneable;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.dsl.ClientMixedOperation;
import io.fabric8.kubernetes.client.dsl.ClientResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public abstract class AbstractCreateCommand<R extends KubernetesResource, D extends Doneable<R>, T extends KubernetesCommand<R>> extends AbstractClientCommand<ClientMixedOperation<R, ? extends KubernetesResourceList, D, ? extends ClientResource<R, D>>, R, T> {

    /** Optional resource object to create */
    private R resource;

    /** Template yml file to specify target */
    private String template;

    /** Template yml resource to specify target */
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
    public void execute(ClientMixedOperation<R, ? extends KubernetesResourceList, D, ? extends ClientResource<R, D>> operation, TestContext context) {
        if (resource != null) {
            operation.create(resource);
            setCommandResult(new CommandResult<>(resource));
        } else if (StringUtils.hasText(getTemplate()) || templateResource != null) {
            R resource = operation.load(getTemplateAsStream(context)).get();
            operation.create(resource);
            setCommandResult(new CommandResult<>(resource));
        } else {
            setCommandResult(new CommandResult<>(specify(operation.createNew(), context).done()));
        }
    }

    /**
     * Specify pod to create.
     * @param pod
     * @param context
     * @return
     */
    protected abstract D specify(D pod, TestContext context);

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
    public R getResource() {
        return resource;
    }

    /**
     * Sets the resource.
     *
     * @param resource
     */
    public void setResource(R resource) {
        this.resource = resource;
    }
}
