/*
 *  Copyright 2006-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.consol.citrus.cucumber.designer.core;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.design.TestDesigner;
import cucumber.api.java.en.*;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class StepDefinition {

    @CitrusResource
    private TestDesigner designer;

    @Given("^variable (.+)=\"([^\"]*)\"$")
    public void variable(String name, String value) {
        designer.variable(name, value);
    }

    @When("^<([^>]*)> sends \"([^\"]*)\"$")
    public void send(String endpoint, String message) {
        designer.send(endpoint)
                .payload(message);
    }

    @When("^<([^>]*)> receives \"([^\"]*)\" as (.+)$")
    public void receive(String endpoint, String message, String type) {
        designer.receive(endpoint)
                .messageType(type)
                .payload(message);
    }

    @When("^<([^>]*)> receives \"([^\"]*)\"$")
    public void receiveXml(String endpoint, String message) {
        designer.receive(endpoint)
                .payload(message);
    }

    @Then("^<([^>]*)> should send \"([^\"]*)\"$")
    public void shouldSend(String endpoint, String message) {
        designer.send(endpoint)
                .payload(message);
    }

    @Then("^<([^>]*)> should receive \"([^\"]*)\"$")
    public void shouldReceive(String endpoint, String message) {
        designer.receive(endpoint)
                .payload(message);
    }

    @Then("^<([^>]*)> should receive \"([^\"]*)\" as (.+)$")
    public void shouldReceive(String endpoint, String message, String type) {
        designer.receive(endpoint)
                .messageType(type)
                .payload(message);
    }
}
