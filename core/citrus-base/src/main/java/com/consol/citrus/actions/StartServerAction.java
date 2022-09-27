/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.consol.citrus.AbstractTestActionBuilder;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action starting a {@link Server} instance.
 *
 * @author Christoph Deppisch
 * @since 2006
 */
public class StartServerAction extends AbstractTestAction {
    /** List of servers to start */
    private final List<Server> servers;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(StartServerAction.class);

    /**
     * Default constructor.
     */
    public StartServerAction(Builder builder) {
        super("start-server", builder);

        this.servers = builder.servers;
    }

    @Override
    public void doExecute(TestContext context) {
        for (Server server : servers) {
            server.start();
            log.info("Started server: " + server.getName());
        }
    }

    /**
     * Get the list of servers to start.
     * @return the list of servers.
     */
    public List<Server> getServers() {
        return servers;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<StartServerAction, Builder> {

        private final List<Server> servers = new ArrayList<>();

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param servers
         * @return
         */
        public static Builder start(Server... servers) {
            Builder builder = new Builder();
            Stream.of(servers).forEach(builder::server);
            return builder;
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param server
         * @return
         */
        public static Builder start(Server server) {
            Builder builder = new Builder();
            builder.server(server);
            return builder;
        }

        public Builder server(Server server) {
            this.servers.add(server);
            return this;
        }

        public Builder server(Server... server) {
            return server(Arrays.asList(server));
        }

        public Builder server(List<Server> servers) {
            this.servers.addAll(servers);
            return this;
        }

        @Override
        public StartServerAction build() {
            return new StartServerAction(this);
        }
    }
}
