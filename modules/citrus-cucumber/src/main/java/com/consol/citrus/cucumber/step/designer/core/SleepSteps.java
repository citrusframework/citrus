/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.cucumber.step.designer.core;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.design.TestDesigner;
import cucumber.api.java.en.Then;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class SleepSteps {

    @CitrusResource
    private TestDesigner designer;

    @Then("^sleep$")
    public void sleep() {
        designer.sleep();
    }

    @Then("^sleep (\\d+) ms$")
    public void sleep(long milliseconds) {
        designer.sleep(milliseconds);
    }
}
