/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.jms.integration;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import static org.citrusframework.jms.actions.PurgeJmsQueuesAction.Builder.purgeQueues;

/**
 * @author Christoph Deppisch
 */
@Test
public class PurgeJmsQueuesJavaIT extends TestNGCitrusSpringSupport {

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
        run(purgeQueues()
            .queue("Citrus.Queue.Dummy")
            .queue("Citrus.Queue.Dummy.One.In")
            .queue("Citrus.Queue.Dummy.One.Out")
            .queue("Citrus.Queue.Dummy.One.In")
            .queue("Citrus.Queue.Dummy.One.Out")
            .queue("Citrus.Queue.Dummy.Three.In")
            .queue("Citrus.Queue.Dummy.Three.Out"));

        run(purgeQueues()
            .connectionFactory(connectionFactory)
            .timeout(150L)
            .queue("Citrus.Queue.Dummy")
            .queue("Citrus.Queue.Dummy.One.In")
            .queue("Citrus.Queue.Dummy.One.Out")
            .queue("Citrus.Queue.Dummy.One.In")
            .queue("Citrus.Queue.Dummy.One.Out")
            .queue("Citrus.Queue.Dummy.Three.In")
            .queue("Citrus.Queue.Dummy.Three.Out"));

        run(purgeQueues()
            .queue(queue1)
            .queue(queue2)
            .queue(queue3)
            .queue("Citrus.Queue.Dummy.One.In")
            .queue("Citrus.Queue.Dummy.One.Out")
            .queue("Citrus.Queue.Dummy.Three.In")
            .queue("Citrus.Queue.Dummy.Three.Out"));
    }
}
