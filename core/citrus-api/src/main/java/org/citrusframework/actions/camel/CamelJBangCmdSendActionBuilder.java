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

package org.citrusframework.actions.camel;

import java.util.Map;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ActionBuilder;
import org.citrusframework.actions.ReferenceResolverAwareBuilder;
import org.citrusframework.spi.Resource;

public interface CamelJBangCmdSendActionBuilder<T extends TestAction, B extends CamelJBangCmdSendActionBuilder<T, B>>
        extends ActionBuilder<T, B>, TestActionBuilder<T>, ReferenceResolverAwareBuilder<T, B> {

    /**
     * Sets the integration name.
     */
    B integration(String name);

    B timeout(long timeout);

    B timeout(String timeout);

    B header(String name, String value);

    B headers(Map<String, String> values);

    B body(String body);

    B body(Resource body);

    B endpoint(String endpoint);

    B endpointUri(String uri);

    /**
     * Adds a command argument.
     */
    B withArg(String arg);

    /**
     * Adds a command argument with name and value.
     */
    B withArg(String name, String value);

    /**
     * Adds command arguments.
     */
    B withArgs(String... args);

    B reply(boolean reply);
}
