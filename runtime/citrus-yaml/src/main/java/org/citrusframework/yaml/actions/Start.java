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

package org.citrusframework.yaml.actions;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.StartServerAction;
import org.citrusframework.server.Server;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

/**
 * @author Christoph Deppisch
 */
public class Start implements TestActionBuilder<StartServerAction>, ReferenceResolverAware {

    private final StartServerAction.Builder builder = new StartServerAction.Builder();

    private final List<String> servers = new ArrayList<>();
    private ReferenceResolver referenceResolver;

    public void setServer(String server) {
        this.servers.add(server);
    }

    public void setServers(List<ServerRef> servers) {
        servers.forEach(server -> this.servers.add(server.name));
    }

    @Override
    public StartServerAction build() {
        if (referenceResolver != null) {
            for (String server : servers) {
                builder.server(referenceResolver.resolve(server, Server.class));
            }
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    public static class ServerRef {
        protected String name;

        public String getName() {
            return name;
        }

        public void setName(String value) {
            this.name = value;
        }
    }
}
