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

package com.consol.citrus.docker.command;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.docker.client.DockerClient;
import com.github.dockerjava.api.command.VersionCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class Version extends AbstractDockerCommand<com.github.dockerjava.api.model.Version> {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(Version.class);

    /**
     * Default constructor initializing the command name.
     */
    public Version() {
        super("docker:version");
    }

    @Override
    public void execute(DockerClient dockerClient, TestContext context) {
        VersionCmd command = dockerClient.getEndpointConfiguration().getDockerClient().versionCmd();
        setCommandResult(command.exec());

        log.debug(getCommandResult().toString());
    }
}
