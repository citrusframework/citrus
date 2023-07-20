/*
 *  Copyright 2023 the original author or authors.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.jms.yaml;

import java.util.List;

import jakarta.jms.ConnectionFactory;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.jms.actions.PurgeJmsQueuesAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

public class PurgeQueues implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private final PurgeJmsQueuesAction.Builder builder = new PurgeJmsQueuesAction.Builder();

    private String description;
    private String actor;

    private String connectionFactory;

    private ReferenceResolver referenceResolver;

    public void setDescription(String value) {
        this.description = value;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public void setConnectionFactory(String connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setQueue(String queue) {
        builder.queue(queue);
    }

    public void setQueues(List<String> queues) {
        builder.queueNames(queues);
    }

    public void setTimeout(long timeout) {
        builder.timeout(timeout);
    }

    public void setSleep(long sleep) {
        builder.sleep(sleep);
    }

    @Override
    public TestAction build() {
        builder.setReferenceResolver(referenceResolver);
        builder.description(description);

        if (referenceResolver != null) {
            if (actor != null) {
                builder.actor(referenceResolver.resolve(actor, TestActor.class));
            }

            if (connectionFactory != null) {
                builder.connectionFactory(referenceResolver.resolve(connectionFactory, ConnectionFactory.class));
            }
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
