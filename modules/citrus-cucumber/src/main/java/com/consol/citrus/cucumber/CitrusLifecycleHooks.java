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

package com.consol.citrus.cucumber;

import com.consol.citrus.Citrus;
import com.consol.citrus.annotations.CitrusFramework;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.TestRunner;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CitrusLifecycleHooks {

    @CitrusFramework
    protected Citrus citrus;

    @CitrusResource
    protected TestDesigner designer;

    @CitrusResource
    protected TestRunner runner;

    @Before
    public void before(Scenario scenario) {
        if (designer != null) {
            designer.name(scenario.getId());
            designer.description(scenario.getName());
        }

        if (runner != null) {
            runner.name(scenario.getId());
            runner.description(scenario.getName());
            runner.start();
        }
    }

    @After
    public void after(Scenario scenario) {
        if (!scenario.isFailed()) {
            if (designer != null && designer.getTestCase().getActionCount() > 0) {
                citrus.run(designer.getTestCase());
            }
        }

        if (runner != null) {
            runner.stop();
        }
    }
}
