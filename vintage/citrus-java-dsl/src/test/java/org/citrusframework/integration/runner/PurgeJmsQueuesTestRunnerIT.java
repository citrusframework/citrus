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

package org.citrusframework.integration.runner;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.testng.TestNGCitrusTestRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;

/**
 * @author Christoph Deppisch
 */
@Test
public class PurgeJmsQueuesTestRunnerIT extends TestNGCitrusTestRunner {

    @Autowired
    @Qualifier("connectionFactory")
    private ConnectionFactory connectionFactory;

    @Autowired
    @Qualifier("testQueue1")
    private Queue queue1;

    @Autowired
    @Qualifier("testQueue2")
    private Queue queue2;

    @Autowired
    @Qualifier("testQueue3")
    private Queue queue3;

    @CitrusTest
    public void purgeJmsQueuesAction() {
        purgeQueues(builder -> builder.queue("Citrus.Queue.Dummy")
                .queue("Citrus.Queue.Dummy.One.In")
                .queue("Citrus.Queue.Dummy.One.Out")
                .queue("Citrus.Queue.Dummy.One.In")
                .queue("Citrus.Queue.Dummy.One.Out")
                .queue("Citrus.Queue.Dummy.Three.In")
                .queue("Citrus.Queue.Dummy.Three.Out"));

        purgeQueues(builder -> builder.connectionFactory(connectionFactory)
                .timeout(150L)
                .queue("Citrus.Queue.Dummy")
                .queue("Citrus.Queue.Dummy.One.In")
                .queue("Citrus.Queue.Dummy.One.Out")
                .queue("Citrus.Queue.Dummy.One.In")
                .queue("Citrus.Queue.Dummy.One.Out")
                .queue("Citrus.Queue.Dummy.Three.In")
                .queue("Citrus.Queue.Dummy.Three.Out"));

        purgeQueues(builder -> builder.queue(queue1)
                .queue(queue2)
                .queue(queue3)
                .queue("Citrus.Queue.Dummy.One.In")
                .queue("Citrus.Queue.Dummy.One.Out")
                .queue("Citrus.Queue.Dummy.Three.In")
                .queue("Citrus.Queue.Dummy.Three.Out"));
    }
}
