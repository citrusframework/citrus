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

package com.consol.citrus.junit.integration;

import com.consol.citrus.ShouldFailGroup;
import com.consol.citrus.annotations.CitrusGroovyTest;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.junit.JUnit4CitrusSupport;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Christoph Deppisch
 * @since 2.2
 */
@SuppressWarnings("squid:S2699")
public class JUnit4GroovyIT extends JUnit4CitrusSupport {

    @Test
    @CitrusGroovyTest(name = "sample.it")
    public void JUnit4Groovy_1_IT() {
    }

    @Test
    @CitrusGroovyTest(name = { "echo.test", "delay.test", "createVariables.test" }, packageName = "com.consol.citrus.junit.integration.actions")
    public void JUnit4Groovy_2_IT() {
    }

    @Test
    @CitrusGroovyTest(packageScan = "com.consol.citrus.junit.integration.simple")
    public void JUnit4Groovy_3_IT() {
    }

    @Test
    @CitrusGroovyTest(sources = "classpath:com/consol/citrus/junit/integration/actions/createVariables.test.groovy")
    public void JUnit4Groovy_4_IT() {
    }

    @Test(expected = TestCaseFailedException.class)
    @Category( ShouldFailGroup.class )
    @CitrusGroovyTest(name = "fail.it")
    public void JUnit4Groovy_5_IT() {
    }
}
