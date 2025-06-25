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
import org.citrusframework.actions.CreateEndpointAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreateEndpointTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadCreateEndpoint() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/yaml/actions/create-endpoint-test.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "CreateEndpointTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), CreateEndpointAction.class);
        Assert.assertEquals(((CreateEndpointAction) result.getTestAction(0)).getEndpointUri(), "direct:hello");
        Assert.assertEquals(((CreateEndpointAction) result.getTestAction(1)).getEndpointUri(), "direct?queueName=hello&timeout=2000");
    }
}
