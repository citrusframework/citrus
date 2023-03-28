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

package org.citrusframework.integration.annotation;

import org.citrusframework.annotations.CitrusXmlTest;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.1
 */
@Test
public class CitrusXmlAnnotationIT extends TestNGCitrusSpringSupport {

    @CitrusXmlTest(name = { "SampleIT" })
    public void CitrusXmlAnnotation_1_IT() {}

    @CitrusXmlTest(name = { "EchoActionIT", "FailActionIT" }, packageName = "org.citrusframework.integration.actions")
    public void CitrusXmlAnnotation_2_IT() {}

    @CitrusXmlTest(packageScan = "org.citrusframework.integration.functions")
    public void CitrusXmlAnnotation_3_IT() {}

    @CitrusXmlTest(sources = { "classpath:org/citrusframework/integration/actions/CreateVariablesIT.xml" })
    public void CitrusXmlAnnotation_4_IT() {}
}
