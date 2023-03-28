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

package org.citrusframework.cucumber.integration.echo;

import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.message.MessageType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class EchoSteps {

    @CitrusResource
    protected TestCaseRunner runner;

    @CitrusResource
    protected TestContext context;

    @Given("^My name is (.*)$")
    public void my_name_is(String name) {
        context.setVariable("username", name);
    }

    @When("^I say hello.*$")
    public void say_hello() {
        runner.when(send("echoEndpoint")
            .message()
            .type(MessageType.PLAINTEXT)
            .body("Hello, my name is ${username}!"));
    }

    @When("^I say goodbye.*$")
    public void say_goodbye() {
        runner.when(send("echoEndpoint")
            .message()
            .type(MessageType.PLAINTEXT)
            .body("Goodbye from ${username}!"));
    }

    @Then("^the service should return: \"([^\"]*)\"$")
    public void verify_return(final String body) {
        runner.then(receive("echoEndpoint")
            .message()
            .type(MessageType.PLAINTEXT)
            .body("You just said: " + body));
    }

}
