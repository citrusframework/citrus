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
import org.citrusframework.docker.command.ImageBuild;
import org.citrusframework.docker.command.DockerCommandProvider;

public class BuildImage implements DockerCommandProvider {

    private final ImageBuild.Builder delegate = new org.citrusframework.docker.command.ImageBuild.Builder(new DockerExecuteAction.Builder());

    @SchemaProperty(description = "Skip the image cache when building the image")
    public void setNoCache(Boolean noCache) {
        delegate.noCache(noCache);
    }
    @SchemaProperty(description = "The base directory holding the Dockerfile")
    public void setBasedir(String basedir) {
        delegate.basedir(basedir);
    }
    @SchemaProperty(description = "The tag of the image to build")
    public void setTag(String tag) {
        delegate.tag(tag);
    }
    @SchemaProperty(description = "Remove intermediate containers after a successful build")
    public void setRemove(Boolean remove) {
        delegate.remove(remove);
    }
    @SchemaProperty(description = "Suppress the verbose build output")
    public void setQuiet(Boolean quiet) {
        delegate.quiet(quiet);
    }
    @SchemaProperty(description = "The Dockerfile to use for building the image")
    public void setDockerFile(String dockerFile) {
        delegate.dockerFile(dockerFile);
    }
    @Override
    public DockerCommand<?> getCommand() {
        return delegate.command();
    }
}
