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
package org.citrusframework.docker.yaml;

import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.docker.actions.DockerExecuteAction;
import org.citrusframework.docker.command.DockerCommand;
import org.citrusframework.docker.command.ImagePull;
import org.citrusframework.docker.command.DockerCommandProvider;

public class PullImage implements DockerCommandProvider {

    private final ImagePull.Builder delegate = new org.citrusframework.docker.command.ImagePull.Builder(new DockerExecuteAction.Builder());

    @SchemaProperty(description = "The id of the image to pull")
    public void setImage(String image) {
        delegate.image(image);
    }
    @SchemaProperty(description = "The tag of the image to pull")
    public void setTag(String tag) {
        delegate.tag(tag);
    }
    @SchemaProperty(description = "The repository to pull the image from")
    public void setRepository(String repository) {
        delegate.repository(repository);
    }
    @SchemaProperty(description = "The registry to pull the image from")
    public void setRegistry(String registry) {
        delegate.registry(registry);
    }
    @Override
    public DockerCommand<?> getCommand() {
        return delegate.command();
    }
}
