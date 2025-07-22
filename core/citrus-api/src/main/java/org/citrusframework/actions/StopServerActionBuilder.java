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

package org.citrusframework.actions;

import java.util.List;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.server.Server;

public interface StopServerActionBuilder<T extends TestAction>
        extends ActionBuilder<T, StopServerActionBuilder<T>>, TestActionBuilder<T>, ReferenceResolverAwareBuilder<T, StopServerActionBuilder<T>> {

    StopServerActionBuilder<T> server(Server server);

    StopServerActionBuilder<T> server(Server... server);

    StopServerActionBuilder<T> server(List<Server> servers);

    StopServerActionBuilder<T> server(String server);

    StopServerActionBuilder<T> server(String... server);

    StopServerActionBuilder<T> serverNames(List<String> servers);

    interface BuilderFactory {

        /**
         * Fluent API action building entry method used in Java DSL.
         */
        StopServerActionBuilder<?> stopServer();

        default StopServerActionBuilder<?> stop(Server... servers) {
            return stopServer().server(servers);
        }

        default StopServerActionBuilder<?> stop(Server server) {
            return stopServer().server(server);
        }

        default StopServerActionBuilder<?> stop(String... serverNames) {
            return stopServer().server(serverNames);
        }

        default StopServerActionBuilder<?> stop(String server) {
            return stopServer().server(server);
        }

    }

}
