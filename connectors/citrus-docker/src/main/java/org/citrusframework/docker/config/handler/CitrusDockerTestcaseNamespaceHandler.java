/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.docker.config.handler;

import org.citrusframework.docker.command.*;
import org.citrusframework.docker.config.xml.DockerExecuteActionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class CitrusDockerTestcaseNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("info", new DockerExecuteActionParser(Info.class));
        registerBeanDefinitionParser("ping", new DockerExecuteActionParser(Ping.class));
        registerBeanDefinitionParser("version", new DockerExecuteActionParser(Version.class));
        registerBeanDefinitionParser("build", new DockerExecuteActionParser(ImageBuild.class));
        registerBeanDefinitionParser("pull", new DockerExecuteActionParser(ImagePull.class));
        registerBeanDefinitionParser("inspect", new DockerExecuteActionParser(ImageInspect.class, ContainerInspect.class));
        registerBeanDefinitionParser("remove", new DockerExecuteActionParser(ImageRemove.class, ContainerRemove.class));
        registerBeanDefinitionParser("start", new DockerExecuteActionParser(ContainerStart.class));
        registerBeanDefinitionParser("stop", new DockerExecuteActionParser(ContainerStop.class));
        registerBeanDefinitionParser("create", new DockerExecuteActionParser(ContainerCreate.class));
        registerBeanDefinitionParser("wait", new DockerExecuteActionParser(ContainerWait.class));
    }
}
