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

public interface StartServerActionBuilder<T extends TestAction>
        extends ActionBuilder<T, StartServerActionBuilder<T>>, TestActionBuilder<T>, ReferenceResolverAwareBuilder<T, StartServerActionBuilder<T>> {

    StartServerActionBuilder<T> server(Server server);

    StartServerActionBuilder<T> server(Server... server);

    StartServerActionBuilder<T> server(List<Server> servers);

    StartServerActionBuilder<T> server(String server);

    StartServerActionBuilder<T> server(String... server);

    StartServerActionBuilder<T> serverNames(List<String> servers);

    interface BuilderFactory {

        /**
         * Fluent API action building entry method used in Java DSL.
         */
        StartServerActionBuilder<?> startServer();

        default StartServerActionBuilder<?> start(Server... servers) {
            return startServer().server(servers);
        }

        default StartServerActionBuilder<?> start(Server server) {
            return startServer().server(server);
        }

        default StartServerActionBuilder<?> start(String... serverNames) {
            return startServer().server(serverNames);
        }

        default StartServerActionBuilder<?> start(String server) {
            return startServer().server(server);
        }

    }

}
