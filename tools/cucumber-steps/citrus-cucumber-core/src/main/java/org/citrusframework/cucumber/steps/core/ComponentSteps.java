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

package org.citrusframework.cucumber.steps.core;

import io.cucumber.java.en.Given;
import org.citrusframework.Citrus;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.message.DefaultMessageQueue;

public class ComponentSteps {

    @CitrusFramework
    private Citrus citrus;

    @Given("^(?:create|new) message queue ([^\"\\s]+)$")
    public void createMessageQueue(String name) {
        citrus.getCitrusContext().getReferenceResolver().bind(name, new DefaultMessageQueue(name));
    }
}
