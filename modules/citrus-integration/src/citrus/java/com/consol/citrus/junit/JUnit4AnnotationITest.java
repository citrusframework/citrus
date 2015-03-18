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

package com.consol.citrus.junit;

import com.consol.citrus.annotations.CitrusXmlTest;
import org.junit.Test;

/**
 * @author Christoph Deppisch
 * @since 2.2
 */
public class JUnit4AnnotationITest extends AbstractJUnit4CitrusTest {

    @Test
    @CitrusXmlTest
    public void JUnit4AnnotationITest() {
    }

    @Test
    @CitrusXmlTest(name = "SampleITest")
    public void JUnit4Annotation_1_ITest() {
    }

    @Test
    @CitrusXmlTest(name = { "EchoActionITest", "FailActionITest", "CreateVariablesITest" }, packageName = "com.consol.citrus.actions")
    public void JUnit4Annotation_2_ITest() {
    }

    @Test
    @CitrusXmlTest(packageScan = "com.consol.citrus.functions")
    public void JUnit4Annotation_3_ITest() {
    }
}
