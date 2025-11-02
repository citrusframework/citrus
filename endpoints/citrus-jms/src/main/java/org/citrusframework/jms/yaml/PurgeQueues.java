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

package org.citrusframework.jms.yaml;

import java.util.List;

import jakarta.jms.ConnectionFactory;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.jms.actions.PurgeJmsQueuesAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.yaml.SchemaProperty;

public class PurgeQueues implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private final PurgeJmsQueuesAction.Builder builder = new PurgeJmsQueuesAction.Builder();

    private String description;
    private String actor;

    private String connectionFactory;

    private ReferenceResolver referenceResolver;

    @SchemaProperty(advanced = true, description = "Test action description printed when the action is executed.")
    public void setDescription(String value) {
        this.description = value;
    }

    public String getDescription() {
        return description;
    }

    @SchemaProperty(advanced = true)
    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getActor() {
        return actor;
    }

    @SchemaProperty(required = true, description = "The JMS connection factory.")
    public void setConnectionFactory(String connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public String getConnectionFactory() {
        return connectionFactory;
    }

    @SchemaProperty(description = "The JMS queue to purge.")
    public void setQueue(String queue) {
        builder.queue(queue);
    }

    @SchemaProperty(description = "List of JMS queues to purge.")
    public void setQueues(List<String> queues) {
        builder.queueNames(queues);
    }

    @SchemaProperty(description = "Request timeout while consuming messages from the queue.")
    public void setTimeout(long timeout) {
        builder.timeout(timeout);
    }

    @SchemaProperty(advanced = true, description = "Time to wait between message consume attempts.")
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
