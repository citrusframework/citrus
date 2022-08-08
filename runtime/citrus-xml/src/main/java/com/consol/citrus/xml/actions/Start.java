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

package com.consol.citrus.xml.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.actions.StartServerAction;
import com.consol.citrus.server.Server;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "start")
public class Start implements TestActionBuilder<StartServerAction>, ReferenceResolverAware {

    private final StartServerAction.Builder builder = new StartServerAction.Builder();

    private final List<String> servers = new ArrayList<>();
    private ReferenceResolver referenceResolver;

    @XmlAttribute
    public Start setServer(String server) {
        this.servers.add(server);
        return this;
    }

    @XmlElement
    public Start setServers(Servers servers) {
        servers.getServers().forEach(server -> this.servers.add(server.name));
        return this;
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
