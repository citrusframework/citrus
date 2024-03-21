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

package org.citrusframework.jms.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.jms.ConnectionFactory;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.jms.actions.PurgeJmsQueuesAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "description",
        "queues",
})
@XmlRootElement(name = "purge-jms-queues")
public class PurgeQueues implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    @XmlTransient
    private final PurgeJmsQueuesAction.Builder builder = new PurgeJmsQueuesAction.Builder();

    @XmlElement
    private String description;
    @XmlAttribute
    private String actor;

    @XmlAttribute
    protected String connectionFactory;

    @XmlAttribute
    protected String timeout;

    @XmlAttribute
    protected String sleep;

    @XmlElement(name = "queue")
    protected List<Queue> queues;

    @XmlTransient
    private ReferenceResolver referenceResolver;

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getActor() {
        return actor;
    }

    public void setConnectionFactory(String connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public String getConnectionFactory() {
        return connectionFactory;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setSleep(String sleep) {
        this.sleep = sleep;
    }

    public String getSleep() {
        return sleep;
    }

    public void setQueues(List<Queue> queues) {
        this.queues = queues;
    }

    public List<Queue> getQueues() {
        if (queues == null) {
            queues = new ArrayList<>();
        }

        return queues;
    }

    @Override
    public TestAction build() {
        builder.setReferenceResolver(referenceResolver);
        builder.description(description);

        for (Queue queue : getQueues()) {
            if (queue.name != null) {
                builder.queue(queue.getName());
            }

            if (queue.ref != null && referenceResolver != null) {
                builder.queue(referenceResolver.resolve(queue.ref, jakarta.jms.Queue.class));
            }
        }

        if (timeout != null) {
            builder.timeout(Long.parseLong(timeout));
        }

        if (sleep != null) {
            builder.sleep(Long.parseLong(sleep));
        }

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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {})
    public static class Queue {
        @XmlAttribute
        protected String name;

        @XmlAttribute
        protected String ref;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setRef(String ref) {
            this.ref = ref;
        }

        public String getRef() {
            return ref;
        }
    }
}
