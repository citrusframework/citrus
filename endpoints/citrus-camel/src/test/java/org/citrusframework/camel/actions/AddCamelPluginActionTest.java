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

import java.util.Collections;
import java.util.List;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.camel.UnitTestSupport;
import org.citrusframework.camel.cli.CamelCli;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AddCamelPluginActionTest extends UnitTestSupport {

    @Mock
    private CamelCli camelCli;

    @BeforeMethod
    @Override
    public void prepareTest() {
        super.prepareTest();
        MockitoAnnotations.openMocks(this);
        context.getReferenceResolver().bind("camelCli", camelCli);
    }

    @Test
    public void shouldInstallPluginWhenNotInstalled() {
        when(camelCli.getPlugins()).thenReturn(Collections.emptyList());

        AddCamelPluginAction action = new AddCamelPluginAction.Builder()
                .pluginName("my-plugin")
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);

        verify(camelCli).addPlugin("my-plugin");
        verify(camelCli, never()).deletePlugin("my-plugin");
    }

    @Test
    public void shouldSkipWhenAlreadyInstalledWithoutVersionArgs() {
        when(camelCli.getPlugins()).thenReturn(List.of("my-plugin"));

        AddCamelPluginAction action = new AddCamelPluginAction.Builder()
                .pluginName("my-plugin")
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);

        verify(camelCli, never()).addPlugin("my-plugin");
        verify(camelCli, never()).deletePlugin("my-plugin");
    }

    @Test
    public void shouldReinstallWhenAlreadyInstalledWithVersionArg() {
        when(camelCli.getPlugins()).thenReturn(List.of("my-plugin"));

        AddCamelPluginAction action = new AddCamelPluginAction.Builder()
                .pluginName("my-plugin")
                .withArg("--version", "1.5.0-SNAPSHOT")
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);

        InOrder order = inOrder(camelCli);
        order.verify(camelCli).deletePlugin("my-plugin");
        order.verify(camelCli).addPlugin("my-plugin", "--version", "1.5.0-SNAPSHOT");
    }

    @Test
    public void shouldReinstallWhenAlreadyInstalledWithGavArg() {
        when(camelCli.getPlugins()).thenReturn(List.of("my-plugin"));

        AddCamelPluginAction action = new AddCamelPluginAction.Builder()
                .pluginName("my-plugin")
                .withArg("--gav", "com.example:my-plugin:2.0.0")
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);

        InOrder order = inOrder(camelCli);
        order.verify(camelCli).deletePlugin("my-plugin");
        order.verify(camelCli).addPlugin("my-plugin", "--gav", "com.example:my-plugin:2.0.0");
    }

    @Test
    public void shouldInstallWithVersionWhenNotInstalled() {
        when(camelCli.getPlugins()).thenReturn(Collections.emptyList());

        AddCamelPluginAction action = new AddCamelPluginAction.Builder()
                .pluginName("my-plugin")
                .withArg("--version", "1.5.0-SNAPSHOT")
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);

        verify(camelCli).addPlugin("my-plugin", "--version", "1.5.0-SNAPSHOT");
        verify(camelCli, never()).deletePlugin("my-plugin");
    }

    @Test
    public void shouldRegisterFinallyActionWhenAutoRemoveEnabled() {
        when(camelCli.getPlugins()).thenReturn(Collections.emptyList());

        AddCamelPluginAction action = new AddCamelPluginAction.Builder()
                .pluginName("my-plugin")
                .autoRemove(true)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);

        verify(camelCli).addPlugin("my-plugin");

        List<TestActionBuilder<?>> finalActions = context.getFinalActions();
        Assert.assertEquals(finalActions.size(), 1);
        Assert.assertEquals(finalActions.get(0).build().getClass(), DeleteCamelPluginAction.class);
    }

    @Test
    public void shouldNotRegisterFinallyActionWhenAutoRemoveDisabled() {
        when(camelCli.getPlugins()).thenReturn(Collections.emptyList());

        AddCamelPluginAction action = new AddCamelPluginAction.Builder()
                .pluginName("my-plugin")
                .autoRemove(false)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);

        verify(camelCli).addPlugin("my-plugin");
        Assert.assertTrue(context.getFinalActions().isEmpty());
    }
}
