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

package org.citrusframework.spring.config.xml;

import org.citrusframework.actions.StartServerAction;
import org.citrusframework.server.Server;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.2
 */
public class StartServerActionParserTest extends AbstractActionParserTest<StartServerAction> {

    @Test
    public void testStartServerActionParser() {
        assertActionCount(2);
        assertActionClassAndName(StartServerAction.class, "start-server");

        StartServerAction action = getNextTestActionFromTest();
        Assertions.assertEquals(1L, action.getServers().size());
        Assertions.assertEquals(beanDefinitionContext.getBean("myServer", Server.class), action.getServers().get(0));

        action = getNextTestActionFromTest();
        Assertions.assertEquals(2L, action.getServers().size());
        Assertions.assertEquals(beanDefinitionContext.getBean("myFooServer", Server.class), action.getServers().get(0));
        Assertions.assertEquals(beanDefinitionContext.getBean("myBarServer", Server.class), action.getServers().get(1));
    }
}
