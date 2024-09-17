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

package org.citrusframework.kubernetes.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;

public class DeleteResourceAction extends AbstractKubernetesAction {

    private final String content;
    private final Resource resource;
    private final String resourcePath;

    public DeleteResourceAction(Builder builder) {
        super("create-resource", builder);
        this.content = builder.content;
        this.resource = builder.resource;
        this.resourcePath = builder.resourcePath;
    }

    @Override
    public void doExecute(TestContext context) {
        InputStream is;
        if (content != null) {
            is = new ByteArrayInputStream(context.replaceDynamicContentInString(content)
                    .getBytes(StandardCharsets.UTF_8));
        } else if (resource != null) {
            is = resource.getInputStream();
        } else if (resourcePath != null) {
            is = Resources.create(context.replaceDynamicContentInString(resourcePath)).getInputStream();
        } else {
            throw new CitrusRuntimeException("Missing proper Kubernetes resource content");
        }

        getKubernetesClient()
                .load(is)
                .inNamespace(namespace(context))
                .delete();
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKubernetesAction.Builder<DeleteResourceAction, Builder> {

        private String content;
        private Resource resource;
        private String resourcePath;

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder resource(Resource resource) {
            this.resource = resource;
            return this;
        }

        public Builder resource(String path) {
            this.resourcePath = path;
            return this;
        }

        @Override
        public DeleteResourceAction doBuild() {
            return new DeleteResourceAction(this);
        }
    }
}
