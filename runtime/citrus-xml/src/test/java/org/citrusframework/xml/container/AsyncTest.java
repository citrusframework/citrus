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
import org.citrusframework.container.Async;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.xml.actions.AbstractXmlActionTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AsyncTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadAsync() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/xml/container/async.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("AsyncTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());

        Assertions.assertEquals(org.citrusframework.container.Async.class, result.getTestAction(0).getClass());

        int actionIndex = 0;

        org.citrusframework.container.Async action = (Async) result.getTestAction(actionIndex++);
        Assertions.assertEquals(2L, action.getActionCount());
        Assertions.assertEquals(0L, action.getSuccessActions().size());
        Assertions.assertEquals(0L, action.getErrorActions().size());

        action = (Async) result.getTestAction(actionIndex);
        Assertions.assertEquals(1L, action.getActionCount());
        Assertions.assertEquals(1L, action.getSuccessActions().size());
        Assertions.assertEquals("Success!", ((EchoAction)action.getSuccessActions().get(0)).getMessage());
        Assertions.assertEquals(1L, action.getErrorActions().size());
        Assertions.assertEquals("Failed!", ((EchoAction)action.getErrorActions().get(0)).getMessage());

    }
}
