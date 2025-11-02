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

package org.citrusframework.yaml.actions;

import java.util.List;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.StartServerAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.yaml.SchemaProperty;

public class Start implements TestActionBuilder<StartServerAction>, ReferenceResolverAware {

    private final StartServerAction.Builder builder = new StartServerAction.Builder();

    @SchemaProperty
    public void setServer(String server) {
        builder.server(server);
    }

    @SchemaProperty
    public void setServers(List<ServerRef> servers) {
        servers.forEach(server -> builder.server(server.name));
    }

    @Override
    public StartServerAction build() {
        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        builder.setReferenceResolver(referenceResolver);
    }

    public static class ServerRef {
        protected String name;

        public String getName() {
            return name;
        }

        @SchemaProperty
        public void setName(String value) {
            this.name = value;
        }
    }
}
