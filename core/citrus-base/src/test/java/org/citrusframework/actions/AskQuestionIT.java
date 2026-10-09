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

import org.citrusframework.Answer;
import org.citrusframework.Question;
import org.citrusframework.TestActionRunner;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.DefaultMessageQueue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AskQuestionIT extends UnitTestSupport implements TestActionSupport {

    private DefaultTestCaseRunner runner;
    private DirectEndpoint orders;

    @BeforeMethod
    public void setup() {
        runner = new DefaultTestCaseRunner(context);

        orders = new DirectEndpoint();
        orders.getEndpointConfiguration().setQueue(new DefaultMessageQueue("orders"));
    }

    @Test
    public void askQuestion() {
        Answer<String> orderId = new Answer<>();

        runner.run(send(orders)
                .message(new DefaultMessage("new-order").setHeader("OrderId", "ORD-12345")));

        runner.run(receive(orders)
                .message(new DefaultMessage("new-order"))
                .extract(extractor().fromHeaders().header("OrderId", "orderId")));

        runner.run(ask(theOrderId()).into(orderId).saveAs("orderIdCopy"));

        assertTrue(orderId.get().startsWith("ORD-"));

        String lookup = "lookup " + orderId.get();

        runner.run(send(orders)
                .message(new DefaultMessage(lookup)));

        runner.run(receive(orders)
                .message(new DefaultMessage(lookup)));

        assertEquals(context.getVariable("orderIdCopy"), "ORD-12345");
    }

    static Question<String> theOrderId() {
        return new TheOrderId();
    }

    static class TheOrderId implements Question<String> {
        @Override
        public String answeredBy(TestActionRunner runner, TestContext context) {
            return context.getVariable("orderId", String.class);
        }
    }
}
