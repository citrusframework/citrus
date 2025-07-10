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

package org.citrusframework.endpoint;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.Producer;

/**
 * Special endpoint implementation that produces/consumes static messages.
 */
public class StaticEndpoint extends AbstractEndpoint {

    private Message message;

    public StaticEndpoint() {
        this(new StaticEndpointConfiguration());
    }

    /**
     * Constructor with given static message.
     */
    public StaticEndpoint(Message message) {
        this(message, new StaticEndpointConfiguration());
    }

    /**
     * Constructor with empty message and endpoint configuration.
     */
    public StaticEndpoint(StaticEndpointConfiguration endpointConfiguration) {
        this(new DefaultMessage(""), endpointConfiguration);
    }

    /**
     * Constructor with given message and endpoint configuration.
     */
    public StaticEndpoint(Message message, StaticEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
        this.message = message;
    }

    @Override
    public Producer createProducer() {
        return new Producer() {
            @Override
            public void send(Message message, TestContext context) {
                // do nothing
            }

            @Override
            public String getName() {
                return StaticEndpoint.this.getName() + "-producer";
            }
        };
    }

    @Override
    public Consumer createConsumer() {
        return new Consumer() {
            @Override
            public Message receive(TestContext context) {
                return getMessage();
            }

            @Override
            public Message receive(TestContext context, long timeout) {
                return getMessage();
            }

            @Override
            public String getName() {
                return StaticEndpoint.this.getName() + "-consumer";
            }
        };
    }

    @Override
    public StaticEndpointConfiguration getEndpointConfiguration() {
        return (StaticEndpointConfiguration) super.getEndpointConfiguration();
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        if (getEndpointConfiguration().isReuseMessage()) {
            return message;
        } else {
            return new DefaultMessage(message, true);
        }
    }

    public static class StaticEndpointConfiguration extends AbstractEndpointConfiguration {

        private boolean reuseMessage = true;

        public boolean isReuseMessage() {
            return reuseMessage;
        }

        public void setReuseMessage(boolean reuseMessage) {
            this.reuseMessage = reuseMessage;
        }
    }
}
