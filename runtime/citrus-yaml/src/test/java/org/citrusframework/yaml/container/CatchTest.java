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

package org.citrusframework.yaml.container;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.actions.FailAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.yaml.YamlTestLoader;
import org.citrusframework.yaml.actions.AbstractYamlActionTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CatchTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadCatch() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/yaml/container/catch-test.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "CatchTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), org.citrusframework.container.Catch.class);
        Assert.assertEquals(((org.citrusframework.container.Catch) result.getTestAction(0)).getException(), CitrusRuntimeException.class.getName());
        Assert.assertEquals(((org.citrusframework.container.Catch) result.getTestAction(0)).getActionCount(), 2L);
        Assert.assertEquals(((org.citrusframework.container.Catch) result.getTestAction(0)).getTestAction(0).getClass(), EchoAction.class);
        Assert.assertEquals(((org.citrusframework.container.Catch) result.getTestAction(0)).getTestAction(1).getClass(), FailAction.class);
    }
}
