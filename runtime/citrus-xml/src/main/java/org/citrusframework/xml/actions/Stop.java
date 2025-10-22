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

@XmlRootElement(name = "stop")
public class Stop implements TestActionBuilder<StopServerAction>, ReferenceResolverAware {

    private final StopServerAction.Builder builder = new StopServerAction.Builder();

    @XmlAttribute
    public void setServer(String server) {
        builder.server(server);
    }

    @XmlElement
    public void setServers(Servers servers) {
        servers.getServers().forEach(server -> builder.server(server.name));
    }

    @Override
    public StopServerAction build() {
        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        builder.setReferenceResolver(referenceResolver);
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
            @XmlAttribute(required = true)
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
