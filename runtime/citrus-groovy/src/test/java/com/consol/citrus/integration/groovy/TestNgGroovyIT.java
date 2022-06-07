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

package com.consol.citrus.integration.groovy;

import com.consol.citrus.annotations.CitrusTestSource;
import com.consol.citrus.common.TestLoader;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.1
 */
@Test
public class TestNgGroovyIT extends TestNGCitrusSpringSupport {

    @CitrusTestSource(type = TestLoader.GROOVY, name = { "sample.it" })
    public void TestNgGroovy_1_IT() {}

    @CitrusTestSource(type = TestLoader.GROOVY, name = { "echo.test", "delay.test" }, packageName = "com.consol.citrus.integration.actions")
    public void TestNgGroovy_2_IT() {}

    @CitrusTestSource(type = TestLoader.GROOVY, packageScan = "com.consol.citrus.integration.groovy")
    public void TestNgGroovy_3_IT() {}

    @CitrusTestSource(type = TestLoader.GROOVY, sources = { "classpath:com/consol/citrus/integration/actions/createVariables.test.groovy" })
    public void TestNgGroovy_4_IT() {}
}
