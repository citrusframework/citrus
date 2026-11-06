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

package org.citrusframework.xml.container;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.actions.FailAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.xml.actions.AbstractXmlActionTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CatchTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadCatch() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/xml/container/catch.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CatchTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertEquals(org.citrusframework.container.Catch.class, result.getTestAction(0).getClass());
        Assertions.assertEquals(CitrusRuntimeException.class.getName(), ((org.citrusframework.container.Catch) result.getTestAction(0)).getException());
        Assertions.assertEquals(2L, ((org.citrusframework.container.Catch) result.getTestAction(0)).getActionCount());
        Assertions.assertEquals(EchoAction.class, ((org.citrusframework.container.Catch) result.getTestAction(0)).getTestAction(0).getClass());
        Assertions.assertEquals(FailAction.class, ((org.citrusframework.container.Catch) result.getTestAction(0)).getTestAction(1).getClass());
    }
}
