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

package org.citrusframework.integration;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.testng.TestNGCitrusSupport;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.1
 */
@Test
public class XmlTestLoaderIT extends TestNGCitrusSupport {

    @BindToRegistry
    DefaultTextEqualsMessageValidator textEqualsMessageValidator = new DefaultTextEqualsMessageValidator();

    @CitrusTestSource(type = TestLoader.XML, name = { "sample-test" })
    public void XmlTestLoader_1_IT() {}

    @CitrusTestSource(type = TestLoader.XML, name = { "echo-test", "sleep-test" }, packageName = "org.citrusframework.xml.actions")
    public void XmlTestLoader_2_IT() {}

    @CitrusTestSource(type = TestLoader.XML, packageScan = "org.citrusframework.integration")
    public void XmlTestLoader_3_IT() {}

    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/xml/actions/create-variables-test.xml" })
    public void XmlTestLoader_4_IT() {}
}
