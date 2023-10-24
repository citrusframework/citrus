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
import org.citrusframework.junit.spring.JUnit4CitrusSpringSupport;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Christoph Deppisch
 * @since 2.2
 */
@SuppressWarnings("squid:S2699")
public class JUnit4SpringXmlIT extends JUnit4CitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.SPRING)
    public void JUnit4SpringXmlIT() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.SPRING, name = "SampleIT")
    public void JUnit4SpringXml_1_IT() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.SPRING, name = { "EchoActionIT", "FailActionIT", "CreateVariablesIT" }, packageName = "org.citrusframework.junit.integration.actions")
    public void JUnit4SpringXml_2_IT() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.SPRING, packageScan = "org.citrusframework.junit.integration.simple")
    public void JUnit4SpringXml_3_IT() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.SPRING, sources = "classpath:org/citrusframework/junit/integration/actions/CreateVariablesIT.xml")
    public void JUnit4SpringXml_4_IT() {
    }

    @Test(expected = TestCaseFailedException.class)
    @Category( ShouldFailGroup.class )
    @CitrusTestSource(type = TestLoader.SPRING, name = "FailJUnit4IT")
    public void JUnit4SpringXml_5_IT() {
    }
}
