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
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.junit.spring.JUnit4CitrusSpringSupport;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Christoph Deppisch
 * @since 2.2
 */
@SuppressWarnings("squid:S2699")
public class JUnit4AnnotationIT extends JUnit4CitrusSpringSupport {

    @Test
    @CitrusXmlTest
    public void JUnit4AnnotationIT() {
    }

    @Test
    @CitrusXmlTest(name = "SampleIT")
    public void JUnit4Annotation_1_IT() {
    }

    @Test
    @CitrusXmlTest(name = { "EchoActionIT", "FailActionIT", "CreateVariablesIT" }, packageName = "com.consol.citrus.junit.integration.actions")
    public void JUnit4Annotation_2_IT() {
    }

    @Test
    @CitrusXmlTest(packageScan = "com.consol.citrus.junit.simple")
    public void JUnit4Annotation_3_IT() {
    }

    @Test
    @CitrusXmlTest(sources = "classpath:com/consol/citrus/junit/integration/actions/CreateVariablesIT.xml")
    public void JUnit4Annotation_4_IT() {
    }

    @Test(expected = TestCaseFailedException.class)
    @Category( ShouldFailGroup.class )
    @CitrusXmlTest(name = "FailJUnit4IT")
    public void JUnit4Annotation_5_IT() {
    }
}
