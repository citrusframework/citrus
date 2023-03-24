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
import org.citrusframework.exceptions.CitrusRuntimeException;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.PullResponseItem;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class ImagePull extends AbstractDockerCommand<PullResponseItem> {

    /**
     * Default constructor initializing the command name.
     */
    public ImagePull() {
        super("docker:pull");
    }

    @Override
    public void execute(DockerClient dockerClient, TestContext context) {
        final PullImageCmd command = dockerClient.getEndpointConfiguration().getDockerClient().pullImageCmd(getImageId(context));
        PullImageResultCallback imageResult = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                setCommandResult(item);
                super.onNext(item);
            }
        };

        if (hasParameter("registry")) {
            command.withRegistry(getParameter("registry", context));
        }

        if (hasParameter("repository")) {
            command.withRepository(getParameter("repository", context));
        }

        if (hasParameter("tag")) {
            command.withTag(getParameter("tag", context));
        }

        command.exec(imageResult);

        try {
            imageResult.awaitCompletion();
        } catch (InterruptedException e) {
            throw new CitrusRuntimeException("Failed to wait for command success", e);
        }
    }

    /**
     * Sets the image id parameter.
     * @param id
     * @return
     */
    public ImagePull image(String id) {
        getParameters().put(IMAGE_ID, id);
        return this;
    }

    /**
     * Sets the tag parameter.
     * @param tag
     * @return
     */
    public ImagePull tag(String tag) {
        getParameters().put("tag", tag);
        return this;
    }

    /**
     * Sets the repository command parameter.
     * @param repository
     * @return
     */
    public ImagePull repository(String repository) {
        getParameters().put("repository", repository);
        return this;
    }

    /**
     * Sets the registry command parameter.
     * @param registry
     * @return
     */
    public ImagePull registry(String registry) {
        getParameters().put("registry", registry);
        return this;
    }

    /**
     * Command builder.
     */
    public static final class Builder extends AbstractDockerCommandBuilder<PullResponseItem, ImagePull, Builder> {

        public Builder(DockerExecuteAction.Builder parent) {
            super(parent, new ImagePull());
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

        /**
         * Sets the tag parameter.
         * @param tag
         * @return
         */
        public Builder tag(String tag) {
            command.tag(tag);
            return this;
        }

        /**
         * Sets the repository command parameter.
         * @param repository
         * @return
         */
        public Builder repository(String repository) {
            command.repository(repository);
            return this;
        }

        /**
         * Sets the registry command parameter.
         * @param registry
         * @return
         */
        public Builder registry(String registry) {
            command.registry(registry);
            return this;
        }
    }
}
