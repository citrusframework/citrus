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

package org.citrusframework.yaml.actions;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.FailAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.yaml.YamlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FailTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadFail() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/yaml/actions/fail-test.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "FailTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), org.citrusframework.container.Assert.class);
        Assert.assertEquals(((org.citrusframework.container.Assert) result.getTestAction(0)).getException(), CitrusRuntimeException.class);
        Assert.assertEquals(((org.citrusframework.container.Assert) result.getTestAction(0)).getMessage(), "Something went wrong");
        Assert.assertEquals(((org.citrusframework.container.Assert) result.getTestAction(0)).getActionCount(), 1L);
        Assert.assertEquals(((org.citrusframework.container.Assert) result.getTestAction(0)).getAction().getClass(), FailAction.class);

        Assert.assertEquals(result.getTestAction(1).getClass(), org.citrusframework.container.Assert.class);
        Assert.assertEquals(((org.citrusframework.container.Assert) result.getTestAction(1)).getException(), CitrusRuntimeException.class);
        Assert.assertEquals(((org.citrusframework.container.Assert) result.getTestAction(1)).getMessage(), "Something went wrong");
        Assert.assertEquals(((org.citrusframework.container.Assert) result.getTestAction(1)).getActionCount(), 1L);
        Assert.assertEquals(((org.citrusframework.container.Assert) result.getTestAction(1)).getAction().getClass(), FailAction.class);
    }
}
