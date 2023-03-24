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

package org.citrusframework.xml.actions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.StopServerAction;
import org.citrusframework.server.Server;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "stop")
public class Stop implements TestActionBuilder<StopServerAction>, ReferenceResolverAware {

    private final StopServerAction.Builder builder = new StopServerAction.Builder();

    private final List<String> servers = new ArrayList<>();
    private ReferenceResolver referenceResolver;

    @XmlAttribute
    public Stop setServer(String server) {
        this.servers.add(server);
        return this;
    }

    @XmlElement
    public Stop setServers(Servers servers) {
        servers.getServers().forEach(server -> this.servers.add(server.name));
        return this;
    }

    @Override
    public StopServerAction build() {
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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "servers"
    })
    public static class Servers {
        @XmlElement(name = "server")
        protected List<Server> servers;

        public List<Server> getServers() {
            if (servers == null) {
                servers = new ArrayList<>();
            }
            return this.servers;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Server {
            @XmlAttribute(name = "name", required = true)
            protected String name;

            public String getName() {
                return name;
            }

            public void setName(String value) {
                this.name = value;
            }
        }
    }
}
