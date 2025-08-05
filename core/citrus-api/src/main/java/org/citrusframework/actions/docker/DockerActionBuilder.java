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

package org.citrusframework.actions.docker;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ActionBuilder;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;

public interface DockerActionBuilder<T extends TestAction, B extends DockerActionBuilder<T, B>>
        extends ActionBuilder<T, B>, TestActionBuilder<T> {

    /**
     * Use a custom docker client.
     */
    B client(Object client);

    B mapper(Object jsonMapper);

    B validator(MessageValidator<? extends ValidationContext> validator);

    /**
     * Adds some command via abstract command builder.
     */
    DockerActionBuilder<T, B> command(Object dockerCommand);

    /**
     * Use an info command.
     */
    DockerInfoActionBuilder<?, ?, ?> info();

    /**
     * Adds a ping command.
     */
    DockerPingActionBuilder<?, ?, ?> ping();

    /**
     * Adds a version command.
     */
    DockerVersionActionBuilder<?, ?, ?> version();

    /**
     * Adds a create command.
     */
    DockerContainerCreateActionBuilder<?, ?, ?> create();

    /**
     * Adds a create command.
     */
    default DockerContainerCreateActionBuilder<?, ?, ?> create(String imageId) {
        return create().image(imageId);
    }

    /**
     * Adds a start command.
     */
    DockerContainerStartActionBuilder<?, ?, ?> start();

    /**
     * Adds a start command.
     */
    default DockerContainerStartActionBuilder<?, ?, ?> start(String containerId) {
        return start().container(containerId);
    }

    /**
     * Adds a stop command.
     */
    DockerContainerStopActionBuilder<?, ?, ?> stop();

    /**
     * Adds a stop command.
     */
    default DockerContainerStopActionBuilder<?, ?, ?> stop(String containerId) {
        return stop().container(containerId);
    }

    /**
     * Adds a remove command.
     */
    DockerContainerRemoveActionBuilder<?, ?, ?> remove();

    /**
     * Adds a remove command.
     */
    default DockerContainerRemoveActionBuilder<?, ?, ?> remove(String containerId) {
        return remove().container(containerId);
    }

    /**
     * Adds a wait command.
     */
    DockerContainerWaitActionBuilder<?, ?, ?> waitFor();

    /**
     * Adds a wait command.
     */
    default DockerContainerWaitActionBuilder<?, ?, ?> waitFor(String containerId) {
        return waitFor().container(containerId);
    }

    /**
     * Adds an inspect container command.
     */
    DockerContainerInspectActionBuilder<?, ?, ?> inspect();

    /**
     * Adds an inspect container command.
     */
    default DockerContainerInspectActionBuilder<?, ?, ?> inspect(String containerId) {
        return inspect().container(containerId);
    }

    /**
     * Adds an inspect image command.
     */
    DockerImageInspectActionBuilder<?, ?, ?> inspectImage();

    /**
     * Adds an inspect image command.
     */
    default DockerImageInspectActionBuilder<?, ?, ?> inspectImage(String imageId) {
        return inspectImage().image(imageId);
    }

    /**
     * Adds a build image command.
     */
    DockerImageBuildActionBuilder<?, ?, ?> buildImage();

    /**
     * Adds a pull image command.
     */
    DockerImagePullActionBuilder<?, ?, ?> pullImage();

    /**
     * Adds a pull image command.
     */
    default DockerImagePullActionBuilder<?, ?, ?> pullImage(String imageId) {
        return pullImage().image(imageId);
    }

    /**
     * Adds a remove image command.
     */
    DockerImageRemoveActionBuilder<?, ?, ?> removeImage();

    /**
     * Adds a remove image command.
     */
    default DockerImageRemoveActionBuilder<?, ?, ?> removeImage(String imageId) {
        return removeImage().image(imageId);
    }

    /**
     * Adds expected command result.
     */
    DockerActionBuilder<T, B> result(String result);

    interface BuilderFactory {

        DockerActionBuilder<?, ?> docker();

    }
}
