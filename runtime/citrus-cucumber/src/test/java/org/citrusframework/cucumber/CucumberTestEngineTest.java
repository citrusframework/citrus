/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.cucumber;

import java.util.Collections;

import org.citrusframework.TestSource;
import org.citrusframework.cucumber.integration.echo.EchoFeatureIT;
import org.citrusframework.main.TestEngine;
import org.citrusframework.main.TestRunConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class CucumberTestEngineTest {

    @Test
    public void testRunPackage() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setPackages(Collections.singletonList(EchoFeatureIT.class.getPackage().getName()));

        CucumberTestEngine engine = new CucumberTestEngine(configuration);
        engine.run();
    }

    @Test
    public void testRunClass() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setTestSources(Collections.singletonList(new TestSource(EchoFeatureIT.class)));

        CucumberTestEngine engine = new CucumberTestEngine(configuration);
        engine.run();
    }

    @Test
    public void shouldResolveCucumberEngine() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setEngine("cucumber");
        Assert.assertEquals(TestEngine.lookup(configuration).getClass(), CucumberTestEngine.class);
    }
}
