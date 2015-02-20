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

package com.consol.citrus.annotation;

import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.1
 */
@Test
public class CitrusXmlAnnotationITest extends AbstractTestNGCitrusTest {

    @CitrusXmlTest(name = { "SampleITest" })
    public void CitrusXmlAnnotation_1_ITest() {}

    @CitrusXmlTest(name = { "EchoActionITest", "FailActionITest" }, packageName = "com.consol.citrus.actions")
    public void CitrusXmlAnnotation_2_ITest() {}

    @CitrusXmlTest(packageScan = "com.consol.citrus.functions")
    public void CitrusXmlAnnotation_3_ITest() {}
}
