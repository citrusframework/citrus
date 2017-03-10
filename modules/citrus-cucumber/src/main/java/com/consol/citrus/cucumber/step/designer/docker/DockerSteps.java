/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.cucumber.step.designer.docker;

import com.consol.citrus.Citrus;
import com.consol.citrus.annotations.CitrusFramework;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.docker.client.DockerClient;
import com.consol.citrus.docker.message.DockerMessageHeaders;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.*;
import org.springframework.util.Assert;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class DockerSteps {

    @CitrusResource
    private TestDesigner designer;

    @CitrusFramework
    private Citrus citrus;

    protected DockerClient dockerClient;

    @Before
    public void before(Scenario scenario) {
        if (dockerClient == null && citrus.getApplicationContext().getBeansOfType(DockerClient.class).size() == 1L) {
            dockerClient = citrus.getApplicationContext().getBean(DockerClient.class);
        }
    }

    @Given("^docker-client \"([^\"]+)\"$")
    public void setClient(String id) {
        if (!citrus.getApplicationContext().containsBean(id)) {
            throw new CitrusRuntimeException("Unable to find docker client for id: " + id);
        }

        dockerClient = citrus.getApplicationContext().getBean(id, DockerClient.class);
    }

    @When("^create container \"([^\"]+)\" from \"([^\"]+)\"$")
    public void createContainer(String containerName, String imageTag) {
        designer.docker().client(dockerClient)
                .create(imageTag)
                .name(containerName)
                .validateCommandResult((result, context) -> context.setVariable(DockerMessageHeaders.CONTAINER_ID, result.getId()));
    }

    @When("^build image \"([^\"]+)\" from file \"([^\"]+)\"$")
    public void buildImage(String imageTag, String fileName) {
        designer.docker().client(dockerClient)
                .buildImage()
                .tag(imageTag)
                .dockerFile(fileName)
                .validateCommandResult((result, context) -> context.setVariable(DockerMessageHeaders.IMAGE_ID, result.getImageId()));
    }

    @Then("^start container \"([^\"]+)\"$")
    public void startContainer(String containerId) {
        designer.docker().client(dockerClient)
                .start(containerId)
                .validateCommandResult((result, context) ->
                        Assert.isTrue(!result.isErrorIndicated(), String.format("Failed to start container '%s' - %s", containerId, result.getErrorDetail())));
    }

    @Then("^stop container \"([^\"]+)\"$")
    public void stopContainer(String containerId) {
        designer.docker().client(dockerClient)
                .stop(containerId)
                .validateCommandResult((result, context) ->
                        Assert.isTrue(!result.isErrorIndicated(), String.format("Failed to stop container '%s' - %s", containerId, result.getErrorDetail())));
    }

    @Then("^(?:the )?container \"([^\"]+)\" should be running$")
    public void containerIsRunning(String containerId) {
        designer.docker().client(dockerClient)
                .inspectContainer(containerId)
                .validateCommandResult((result, context) ->
                        Assert.isTrue(result.getState().getRunning(), "Failed to validate container state, expected running but was stopped"));
    }

    @Then("^(?:the )?container \"([^\"]+)\" should be stopped")
    public void containerIsStopped(String containerId) {
        designer.docker().client(dockerClient)
                .inspectContainer(containerId)
                .validateCommandResult((result, context) ->
                        Assert.isTrue(!result.getState().getRunning(), "Failed to validate container state, expected stopped but was running"));
    }
}
