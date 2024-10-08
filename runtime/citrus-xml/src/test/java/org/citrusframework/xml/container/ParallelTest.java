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
import org.citrusframework.container.Parallel;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.xml.actions.AbstractXmlActionTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ParallelTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadIterate() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/xml/container/parallel-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "ParallelTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);

        Assert.assertEquals(result.getTestAction(0).getClass(), Parallel.class);

        Parallel action = (Parallel) result.getTestAction(0);
        Assert.assertEquals(action.getActionCount(), 2);
        Assert.assertEquals(action.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(action.getActions().get(1).getClass(), EchoAction.class);

        action = (Parallel) result.getTestAction(1);
        Assert.assertEquals(action.getActionCount(), 3);
        Assert.assertEquals(action.getActions().get(0).getClass(), Parallel.class);
        Assert.assertEquals(((Parallel)action.getActions().get(0)).getActionCount(), 2);
        Assert.assertEquals(action.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(action.getActions().get(2).getClass(), EchoAction.class);
    }
}
