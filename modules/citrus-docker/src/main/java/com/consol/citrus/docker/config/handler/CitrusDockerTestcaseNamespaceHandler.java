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

package com.consol.citrus.docker.config.handler;

import com.consol.citrus.docker.command.*;
import com.consol.citrus.docker.config.xml.DockerExecuteActionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class CitrusDockerTestcaseNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("info", new DockerExecuteActionParser(new Info()));
        registerBeanDefinitionParser("ping", new DockerExecuteActionParser(new Ping()));
        registerBeanDefinitionParser("version", new DockerExecuteActionParser(new Version()));
        registerBeanDefinitionParser("build", new DockerExecuteActionParser(new ImageBuild()));
        registerBeanDefinitionParser("pull", new DockerExecuteActionParser(new ImagePull()));
        registerBeanDefinitionParser("inspect", new DockerExecuteActionParser(new ImageInspect(), new ContainerInspect()));
        registerBeanDefinitionParser("remove", new DockerExecuteActionParser(new ImageRemove(), new ContainerRemove()));
        registerBeanDefinitionParser("start", new DockerExecuteActionParser(new ContainerStart()));
        registerBeanDefinitionParser("stop", new DockerExecuteActionParser(new ContainerStop()));
        registerBeanDefinitionParser("create", new DockerExecuteActionParser(new ContainerCreate()));
        registerBeanDefinitionParser("wait", new DockerExecuteActionParser(new ContainerWait()));
    }
}
