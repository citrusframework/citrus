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

package com.consol.citrus.dsl.design;

import com.consol.citrus.TestCase;
import com.consol.citrus.docker.actions.DockerExecuteAction;
import com.consol.citrus.docker.command.*;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class DockerTestDesignerTest extends AbstractTestNGUnitTest {
    
    @Test
    public void testDockerBuilder() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext) {
            @Override
            public void configure() {
                docker()
                    .info()
                    .version()
                    .ping()
                    .create("my_image");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DockerExecuteAction.class);

        DockerExecuteAction action = (DockerExecuteAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "docker-execute");
        Assert.assertEquals(action.getCommands().size(), 4);
        Assert.assertEquals(action.getCommands().get(0).getClass(), Info.class);
        Assert.assertEquals(action.getCommands().get(1).getClass(), Version.class);
        Assert.assertEquals(action.getCommands().get(2).getClass(), Ping.class);
        Assert.assertEquals(action.getCommands().get(3).getClass(), ContainerCreate.class);
        Assert.assertEquals(action.getCommands().get(3).getParameters().get("image"), "my_image");
    }
}
