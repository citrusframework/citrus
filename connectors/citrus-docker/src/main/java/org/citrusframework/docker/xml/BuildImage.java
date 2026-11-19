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
package org.citrusframework.docker.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.citrusframework.docker.actions.DockerExecuteAction;
import org.citrusframework.docker.command.DockerCommand;
import org.citrusframework.docker.command.DockerCommandProvider;
import org.citrusframework.docker.command.ImageBuild;

@XmlRootElement(name = "build-image")
public class BuildImage implements DockerCommandProvider {

    private final ImageBuild.Builder delegate = new ImageBuild.Builder(new DockerExecuteAction.Builder());

    @XmlAttribute(name = "no-cache")
    public void setNoCache(Boolean noCache) {
        delegate.noCache(noCache);
    }

    @XmlAttribute
    public void setBasedir(String basedir) {
        delegate.basedir(basedir);
    }

    @XmlAttribute
    public void setTag(String tag) {
        delegate.tag(tag);
    }

    @XmlAttribute
    public void setRemove(Boolean remove) {
        delegate.remove(remove);
    }

    @XmlAttribute
    public void setQuiet(Boolean quiet) {
        delegate.quiet(quiet);
    }

    @XmlAttribute(name = "docker-file")
    public void setDockerFile(String dockerFile) {
        delegate.dockerFile(dockerFile);
    }

    @Override
    public DockerCommand<?> getCommand() {
        return delegate.command();
    }
}
