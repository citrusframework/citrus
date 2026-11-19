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
import org.citrusframework.docker.command.ImagePull;

@XmlRootElement(name = "pull-image")
public class PullImage implements DockerCommandProvider {

    private final ImagePull.Builder delegate = new ImagePull.Builder(new DockerExecuteAction.Builder());

    @XmlAttribute(required = true)
    public void setImage(String image) {
        delegate.image(image);
    }

    @XmlAttribute
    public void setTag(String tag) {
        delegate.tag(tag);
    }

    @XmlAttribute
    public void setRepository(String repository) {
        delegate.repository(repository);
    }

    @XmlAttribute
    public void setRegistry(String registry) {
        delegate.registry(registry);
    }

    @Override
    public DockerCommand<?> getCommand() {
        return delegate.command();
    }
}
