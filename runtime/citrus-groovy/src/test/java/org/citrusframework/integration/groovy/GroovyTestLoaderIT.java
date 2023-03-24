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

package org.citrusframework.integration.groovy;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.1
 */
@Test
public class GroovyTestLoaderIT extends TestNGCitrusSpringSupport {

    @CitrusTestSource(type = TestLoader.GROOVY, name = { "sample.it" })
    public void GroovyTestLoader_1_IT() {}

    @CitrusTestSource(type = TestLoader.GROOVY, name = { "echo.test", "delay.test" }, packageName = "org.citrusframework.integration.actions")
    public void GroovyTestLoader_2_IT() {}

    @CitrusTestSource(type = TestLoader.GROOVY, packageScan = "org.citrusframework.integration.groovy")
    public void GroovyTestLoader_3_IT() {}

    @CitrusTestSource(type = TestLoader.GROOVY, sources = { "classpath:org/citrusframework/integration/actions/createVariables.test.groovy" })
    public void GroovyTestLoader_4_IT() {}
}
