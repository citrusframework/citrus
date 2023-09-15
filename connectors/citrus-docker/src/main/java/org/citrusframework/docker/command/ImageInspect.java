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

package org.citrusframework.docker.command;

import org.citrusframework.context.TestContext;
import org.citrusframework.docker.actions.DockerExecuteAction;
import org.citrusframework.docker.client.DockerClient;
import com.github.dockerjava.api.command.InspectImageCmd;
import com.github.dockerjava.api.command.InspectImageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class ImageInspect extends AbstractDockerCommand<InspectImageResponse> {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ImageInspect.class);

    /**
     * Default constructor initializing the command name.
     */
    public ImageInspect() {
        super("docker:image:inspect");
    }

    @Override
    public void execute(DockerClient dockerClient, TestContext context) {
        InspectImageCmd command = dockerClient.getEndpointConfiguration().getDockerClient().inspectImageCmd(getImageId(context));
        InspectImageResponse response = command.exec();

        setCommandResult(response);
        logger.debug(response.toString());
    }

    /**
     * Sets the image id parameter.
     * @param id
     * @return
     */
    public ImageInspect image(String id) {
        getParameters().put(IMAGE_ID, id);
        return this;
    }

    /**
     * Command builder.
     */
    public static final class Builder extends AbstractDockerCommandBuilder<InspectImageResponse, ImageInspect, Builder> {

        public Builder(DockerExecuteAction.Builder parent) {
            super(parent, new ImageInspect());
        }

        /**
         * Sets the image id parameter.
         * @param id
         * @return
         */
        public Builder image(String id) {
            command.image(id);
            return this;
        }
    }
}
