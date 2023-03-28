/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.actions;

import java.util.Map;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.container.Async;
import org.citrusframework.container.Catch;
import org.citrusframework.container.Conditional;
import org.citrusframework.container.FinallySequence;
import org.citrusframework.container.Iterate;
import org.citrusframework.container.Parallel;
import org.citrusframework.container.RepeatOnErrorUntilTrue;
import org.citrusframework.container.RepeatUntilTrue;
import org.citrusframework.container.Sequence;
import org.citrusframework.container.Template;
import org.citrusframework.container.Timer;
import org.citrusframework.container.Wait;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TestActionBuilderLookupTest {

    @Test
    public void shouldLookupTestActions() {
        Map<String, TestActionBuilder<?>> endpointBuilders = TestActionBuilder.lookup();
        Assert.assertTrue(endpointBuilders.containsKey("echo"));
        Assert.assertTrue(endpointBuilders.containsKey("delay"));
        Assert.assertTrue(endpointBuilders.containsKey("sleep"));
        Assert.assertTrue(endpointBuilders.containsKey("fail"));
        Assert.assertTrue(endpointBuilders.containsKey("send"));
        Assert.assertTrue(endpointBuilders.containsKey("receive"));
        Assert.assertTrue(endpointBuilders.containsKey("transform"));
        Assert.assertTrue(endpointBuilders.containsKey("waitFor"));
        Assert.assertTrue(endpointBuilders.containsKey("load"));
        Assert.assertTrue(endpointBuilders.containsKey("createVariables"));
        Assert.assertTrue(endpointBuilders.containsKey("traceVariables"));
        Assert.assertTrue(endpointBuilders.containsKey("start"));
        Assert.assertTrue(endpointBuilders.containsKey("stop"));
        Assert.assertTrue(endpointBuilders.containsKey("purge"));
        Assert.assertTrue(endpointBuilders.containsKey("apply"));
        Assert.assertTrue(endpointBuilders.containsKey("expectTimeout"));
        Assert.assertTrue(endpointBuilders.containsKey("sequential"));
        Assert.assertTrue(endpointBuilders.containsKey("parallel"));
        Assert.assertTrue(endpointBuilders.containsKey("iterate"));
        Assert.assertTrue(endpointBuilders.containsKey("repeat"));
        Assert.assertTrue(endpointBuilders.containsKey("repeatOnError"));
        Assert.assertTrue(endpointBuilders.containsKey("timer"));
        Assert.assertTrue(endpointBuilders.containsKey("conditional"));
        Assert.assertTrue(endpointBuilders.containsKey("async"));
        Assert.assertTrue(endpointBuilders.containsKey("assertException"));
        Assert.assertTrue(endpointBuilders.containsKey("catchException"));
        Assert.assertTrue(endpointBuilders.containsKey("applyTemplate"));
        Assert.assertTrue(endpointBuilders.containsKey("doFinally"));
    }

    @Test
    public void shouldLookupTestActionByName() {
        Assert.assertTrue(TestActionBuilder.lookup("echo").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("echo").get().getClass(), EchoAction.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("delay").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("delay").get().getClass(), SleepAction.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("sleep").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("sleep").get().getClass(), SleepAction.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("fail").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("fail").get().getClass(), FailAction.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("send").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("send").get().getClass(), SendMessageAction.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("receive").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("receive").get().getClass(), ReceiveMessageAction.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("transform").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("transform").get().getClass(), TransformAction.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("waitFor").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("waitFor").get().getClass(), Wait.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("load").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("load").get().getClass(), LoadPropertiesAction.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("createVariables").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("createVariables").get().getClass(), CreateVariablesAction.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("traceVariables").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("traceVariables").get().getClass(), TraceVariablesAction.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("start").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("start").get().getClass(), StartServerAction.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("stop").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("stop").get().getClass(), StopServerAction.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("purge").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("purge").get().getClass(), PurgeEndpointAction.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("apply").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("apply").get().getClass(), ApplyTestBehaviorAction.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("expectTimeout").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("expectTimeout").get().getClass(), ReceiveTimeoutAction.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("sequential").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("sequential").get().getClass(), Sequence.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("parallel").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("parallel").get().getClass(), Parallel.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("iterate").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("iterate").get().getClass(), Iterate.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("repeat").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("repeat").get().getClass(), RepeatUntilTrue.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("repeatOnError").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("repeatOnError").get().getClass(), RepeatOnErrorUntilTrue.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("timer").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("timer").get().getClass(), Timer.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("conditional").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("conditional").get().getClass(), Conditional.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("async").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("async").get().getClass(), Async.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("assertException").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("assertException").get().getClass(), org.citrusframework.container.Assert.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("catchException").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("catchException").get().getClass(), Catch.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("applyTemplate").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("applyTemplate").get().getClass(), Template.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("doFinally").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("doFinally").get().getClass(), FinallySequence.Builder.class);
    }
}
