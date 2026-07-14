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

package org.citrusframework.camel.actions;

import org.citrusframework.camel.UnitTestSupport;
import org.citrusframework.camel.jbang.CamelJBang;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;

public class DeleteCamelPluginActionTest extends UnitTestSupport {

    @Mock
    private CamelJBang camelJBang;

    @BeforeMethod
    @Override
    public void prepareTest() {
        super.prepareTest();
        MockitoAnnotations.openMocks(this);
        context.getReferenceResolver().bind("camelJBang", camelJBang);
    }

    @Test
    public void shouldDeletePlugin() {
        DeleteCamelPluginAction action = new DeleteCamelPluginAction.Builder()
                .pluginName("my-plugin")
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);

        verify(camelJBang).deletePlugin("my-plugin");
    }

    @Test
    public void shouldDeletePluginWithVariableExpression() {
        context.setVariable("pluginName", "my-plugin");

        DeleteCamelPluginAction action = new DeleteCamelPluginAction.Builder()
                .pluginName("${pluginName}")
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);

        verify(camelJBang).deletePlugin("my-plugin");
    }
}
