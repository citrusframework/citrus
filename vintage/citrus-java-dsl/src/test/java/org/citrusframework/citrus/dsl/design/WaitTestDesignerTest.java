/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.citrus.dsl.design;

import java.io.File;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.actions.EchoAction;
import org.citrusframework.citrus.condition.ActionCondition;
import org.citrusframework.citrus.condition.FileCondition;
import org.citrusframework.citrus.condition.HttpCondition;
import org.citrusframework.citrus.condition.MessageCondition;
import org.citrusframework.citrus.container.Wait;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Martin Maher
 * @since 2.4
 */
public class WaitTestDesignerTest extends UnitTestSupport {

    @Test
    public void testWaitHttpBuilder() {
        final long seconds = 3L;
        final String interval = "1500";
        final String url = "http://some.path/";

        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                waitFor()
                    .http()
                        .method(HttpMethod.GET.name())
                        .status(HttpStatus.OK.value())
                        .timeout(500L)
                        .seconds(seconds)
                        .interval(interval)
                        .url(url);
            }
        };
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Wait.class);

        Wait action = (Wait) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "wait");
        Assert.assertEquals(action.getTime(), "3000");
        Assert.assertEquals(action.getInterval(), interval);
        Assert.assertEquals(action.getCondition().getClass(), HttpCondition.class);
        HttpCondition condition = (HttpCondition) action.getCondition();
        Assert.assertEquals(condition.getUrl(), url);
        Assert.assertEquals(condition.getMethod(), "GET");
        Assert.assertEquals(condition.getHttpResponseCode(), "200");
        Assert.assertEquals(condition.getTimeout(), "500");
    }

    @Test
    public void testWaitFilePathBuilder() {
        final String milliseconds = "3000";
        final String interval = "1500";
        final String filePath = "path/to/some/file.txt";

        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                waitFor()
                    .file()
                    .milliseconds(milliseconds)
                    .interval(interval)
                    .path(filePath);
            }
        };
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Wait.class);

        Wait action = (Wait) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "wait");
        Assert.assertEquals(action.getTime(), milliseconds);
        Assert.assertEquals(action.getInterval(), interval);
        Assert.assertEquals(action.getCondition().getClass(), FileCondition.class);
        FileCondition condition = (FileCondition) action.getCondition();
        Assert.assertEquals(condition.getFilePath(), filePath);
    }

    @Test
    public void testWaitFileBuilder() {
        final String milliseconds = "3000";
        final String interval = "1500";
        final File file = Mockito.mock(File.class);

        when(file.getPath()).thenReturn("path/to/some/file.txt");

        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                waitFor()
                    .file()
                    .milliseconds(milliseconds)
                    .interval(interval)
                    .resource(file);
            }
        };
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Wait.class);

        Wait action = (Wait) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "wait");
        Assert.assertEquals(action.getTime(), milliseconds);
        Assert.assertEquals(action.getInterval(), interval);
        Assert.assertEquals(action.getCondition().getClass(), FileCondition.class);
        FileCondition condition = (FileCondition) action.getCondition();
        Assert.assertEquals(condition.getFile(), file);
    }

    @Test
    public void testWaitMessageBuilder() {
        final String messageName = "request";

        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                waitFor()
                    .message()
                    .name(messageName);
            }
        };
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Wait.class);

        Wait action = (Wait) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "wait");
        Assert.assertEquals(action.getTime(), "5000");
        Assert.assertEquals(action.getInterval(), "1000");
        Assert.assertEquals(action.getCondition().getClass(), MessageCondition.class);
        MessageCondition condition = (MessageCondition) action.getCondition();
        Assert.assertEquals(condition.getMessageName(), messageName);
    }

    @Test
    public void testWaitActionBuilder() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                waitFor()
                    .execution()
                    .action(new EchoAction.Builder().build());
            }
        };
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Wait.class);

        Wait action = (Wait) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "wait");
        Assert.assertEquals(action.getTime(), "5000");
        Assert.assertEquals(action.getInterval(), "1000");
        Assert.assertEquals(action.getCondition().getClass(), ActionCondition.class);
        Assert.assertEquals(((ActionCondition)action.getCondition()).getAction().getClass(), EchoAction.class);
        Assert.assertEquals(((ActionCondition)action.getCondition()).getAction().getName(), "echo");
    }

    @Test
    public void testWaitActionFluentBuilder() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                waitFor()
                    .execution()
                    .action(echo("Citrus rocks!"));
            }
        };
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Wait.class);

        Wait action = (Wait) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "wait");
        Assert.assertEquals(action.getTime(), "5000");
        Assert.assertEquals(action.getInterval(), "1000");
        Assert.assertEquals(action.getCondition().getClass(), ActionCondition.class);
        Assert.assertEquals(((ActionCondition)action.getCondition()).getAction().getClass(), EchoAction.class);
        Assert.assertEquals(((ActionCondition)action.getCondition()).getAction().getName(), "echo");
        Assert.assertEquals(((EchoAction) ((ActionCondition)action.getCondition()).getAction()).getMessage(), "Citrus rocks!");
    }

}
