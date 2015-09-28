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
import com.consol.citrus.docker.message.DockerMessageHeaders;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.core.command.BuildImageResultCallback;

import java.io.IOException;

/**
 * @author Christoph Deppisch
 * @since 2.3.1
 */
public class ImageBuild extends AbstractDockerCommand<String> {

    /**
     * Default constructor initializing the command name.
     */
    public ImageBuild() {
        super("docker:build");
    }

    @Override
    public void execute(DockerClient dockerClient, TestContext context) {
        BuildImageCmd command = dockerClient.getDockerClient().buildImageCmd();

        if (hasParameter("no-cache")) {
            command.withNoCache(Boolean.valueOf(getParameter("no-cache", context)));
        }

        if (hasParameter("basedir")) {
            try {
                command.withBaseDirectory(FileUtils.getFileResource(getParameter("basedir", context), context).getFile());
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to access Dockerfile base directory", e);
            }
        }

        if (hasParameter("dockerfile")) {
            try {
                command.withDockerfile(FileUtils.getFileResource(getParameter("dockerfile", context), context).getFile());
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read Dockerfile", e);
            }
        }

        if (hasParameter("quiet")) {
            command.withNoCache(Boolean.valueOf(getParameter("quiet", context)));
        }

        if (hasParameter("remove")) {
            command.withRemove(Boolean.valueOf(getParameter("remove", context)));
        }

        if (hasParameter("tag")) {
            command.withTag(getParameter("tag", context));
        }

        BuildImageResultCallback imageResult = new BuildImageResultCallback();
        command.exec(imageResult);
        String imageId = imageResult.awaitImageId();

        setCommandResult(imageId);
        context.setVariable(DockerMessageHeaders.IMAGE_ID, imageId);
    }
}
