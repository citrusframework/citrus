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

package org.citrusframework.junit.integration;

import org.citrusframework.ShouldFailGroup;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.junit.JUnit4CitrusSupport;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Christoph Deppisch
 * @since 2.2
 */
@SuppressWarnings("squid:S2699")
public class JUnit4GroovyIT extends JUnit4CitrusSupport {

    @Test
    @CitrusTestSource(type = TestLoader.GROOVY, name = "sample.it")
    public void JUnit4Groovy_1_IT() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.GROOVY, name = { "echo.test", "delay.test", "createVariables.test" }, packageName = "org.citrusframework.junit.integration.actions")
    public void JUnit4Groovy_2_IT() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.GROOVY, packageScan = "org.citrusframework.junit.integration.simple")
    public void JUnit4Groovy_3_IT() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.GROOVY, sources = "classpath:org/citrusframework/junit/integration/actions/createVariables.test.groovy")
    public void JUnit4Groovy_4_IT() {
    }

    @Test(expected = TestCaseFailedException.class)
    @Category( ShouldFailGroup.class )
    @CitrusTestSource(type = TestLoader.GROOVY, name = "fail.it")
    public void JUnit4Groovy_5_IT() {
    }
}
