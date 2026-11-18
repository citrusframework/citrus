/*
 * Copyright the original author or authors.
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

package org.citrusframework.xml;

import org.citrusframework.CitrusSettings;
import org.citrusframework.TestCase;
import org.citrusframework.xml.actions.AbstractXmlActionTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestIdentityTest extends AbstractXmlActionTest {

    @Test
    public void shouldReportTestClassAndPackage() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/xml/identity.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("IdentityXmlTest", result.getName());
        Assertions.assertEquals(TestIdentityTest.class, result.getTestClass());
        Assertions.assertEquals(TestIdentityTest.class.getPackageName(), result.getPackageName());
        Assertions.assertEquals(TestIdentityTest.class.getPackageName(),
                context.getVariable(CitrusSettings.getTestPackageVariable()));
    }
}
