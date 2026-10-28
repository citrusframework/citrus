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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestActionBuilderLookupTest {

    @Test
    public void shouldLookupTestActions() {
        Map<String, TestActionBuilder<?>> endpointBuilders = TestActionBuilder.lookup();
        Assertions.assertTrue(endpointBuilders.containsKey("echo"));
        Assertions.assertTrue(endpointBuilders.containsKey("delay"));
        Assertions.assertTrue(endpointBuilders.containsKey("sleep"));
        Assertions.assertTrue(endpointBuilders.containsKey("fail"));
        Assertions.assertTrue(endpointBuilders.containsKey("send"));
        Assertions.assertTrue(endpointBuilders.containsKey("receive"));
        Assertions.assertTrue(endpointBuilders.containsKey("transform"));
        Assertions.assertTrue(endpointBuilders.containsKey("waitFor"));
        Assertions.assertTrue(endpointBuilders.containsKey("load"));
        Assertions.assertTrue(endpointBuilders.containsKey("createVariables"));
        Assertions.assertTrue(endpointBuilders.containsKey("traceVariables"));
        Assertions.assertTrue(endpointBuilders.containsKey("start"));
        Assertions.assertTrue(endpointBuilders.containsKey("stop"));
        Assertions.assertTrue(endpointBuilders.containsKey("purge"));
        Assertions.assertTrue(endpointBuilders.containsKey("apply"));
        Assertions.assertTrue(endpointBuilders.containsKey("expectTimeout"));
        Assertions.assertTrue(endpointBuilders.containsKey("sequential"));
        Assertions.assertTrue(endpointBuilders.containsKey("parallel"));
        Assertions.assertTrue(endpointBuilders.containsKey("iterate"));
        Assertions.assertTrue(endpointBuilders.containsKey("repeat"));
        Assertions.assertTrue(endpointBuilders.containsKey("repeatOnError"));
        Assertions.assertTrue(endpointBuilders.containsKey("timer"));
        Assertions.assertTrue(endpointBuilders.containsKey("conditional"));
        Assertions.assertTrue(endpointBuilders.containsKey("async"));
        Assertions.assertTrue(endpointBuilders.containsKey("assertException"));
        Assertions.assertTrue(endpointBuilders.containsKey("catchException"));
        Assertions.assertTrue(endpointBuilders.containsKey("applyTemplate"));
        Assertions.assertTrue(endpointBuilders.containsKey("doFinally"));
    }

    @Test
    public void shouldLookupTestActionByName() {
        Assertions.assertTrue(TestActionBuilder.lookup("echo").isPresent());
        Assertions.assertEquals(EchoAction.Builder.class, TestActionBuilder.lookup("echo").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("delay").isPresent());
        Assertions.assertEquals(SleepAction.Builder.class, TestActionBuilder.lookup("delay").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("sleep").isPresent());
        Assertions.assertEquals(SleepAction.Builder.class, TestActionBuilder.lookup("sleep").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("fail").isPresent());
        Assertions.assertEquals(FailAction.Builder.class, TestActionBuilder.lookup("fail").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("send").isPresent());
        Assertions.assertEquals(SendMessageAction.Builder.class, TestActionBuilder.lookup("send").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("receive").isPresent());
        Assertions.assertEquals(ReceiveMessageAction.Builder.class, TestActionBuilder.lookup("receive").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("transform").isPresent());
        Assertions.assertEquals(TransformAction.Builder.class, TestActionBuilder.lookup("transform").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("waitFor").isPresent());
        Assertions.assertEquals(Wait.Builder.class, TestActionBuilder.lookup("waitFor").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("load").isPresent());
        Assertions.assertEquals(LoadPropertiesAction.Builder.class, TestActionBuilder.lookup("load").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("createVariables").isPresent());
        Assertions.assertEquals(CreateVariablesAction.Builder.class, TestActionBuilder.lookup("createVariables").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("createEndpoint").isPresent());
        Assertions.assertEquals(CreateEndpointAction.Builder.class, TestActionBuilder.lookup("createEndpoint").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("traceVariables").isPresent());
        Assertions.assertEquals(TraceVariablesAction.Builder.class, TestActionBuilder.lookup("traceVariables").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("start").isPresent());
        Assertions.assertEquals(StartServerAction.Builder.class, TestActionBuilder.lookup("start").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("stop").isPresent());
        Assertions.assertEquals(StopServerAction.Builder.class, TestActionBuilder.lookup("stop").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("purge").isPresent());
        Assertions.assertEquals(PurgeEndpointAction.Builder.class, TestActionBuilder.lookup("purge").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("apply").isPresent());
        Assertions.assertEquals(ApplyTestBehaviorAction.Builder.class, TestActionBuilder.lookup("apply").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("expectTimeout").isPresent());
        Assertions.assertEquals(ReceiveTimeoutAction.Builder.class, TestActionBuilder.lookup("expectTimeout").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("sequential").isPresent());
        Assertions.assertEquals(Sequence.Builder.class, TestActionBuilder.lookup("sequential").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("parallel").isPresent());
        Assertions.assertEquals(Parallel.Builder.class, TestActionBuilder.lookup("parallel").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("iterate").isPresent());
        Assertions.assertEquals(Iterate.Builder.class, TestActionBuilder.lookup("iterate").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("repeat").isPresent());
        Assertions.assertEquals(RepeatUntilTrue.Builder.class, TestActionBuilder.lookup("repeat").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("repeatOnError").isPresent());
        Assertions.assertEquals(RepeatOnErrorUntilTrue.Builder.class, TestActionBuilder.lookup("repeatOnError").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("timer").isPresent());
        Assertions.assertEquals(Timer.Builder.class, TestActionBuilder.lookup("timer").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("conditional").isPresent());
        Assertions.assertEquals(Conditional.Builder.class, TestActionBuilder.lookup("conditional").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("async").isPresent());
        Assertions.assertEquals(Async.Builder.class, TestActionBuilder.lookup("async").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("assertException").isPresent());
        Assertions.assertEquals(org.citrusframework.container.Assert.Builder.class, TestActionBuilder.lookup("assertException").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("catchException").isPresent());
        Assertions.assertEquals(Catch.Builder.class, TestActionBuilder.lookup("catchException").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("applyTemplate").isPresent());
        Assertions.assertEquals(Template.Builder.class, TestActionBuilder.lookup("applyTemplate").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("doFinally").isPresent());
        Assertions.assertEquals(FinallySequence.Builder.class, TestActionBuilder.lookup("doFinally").get().getClass());
    }
}
