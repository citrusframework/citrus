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

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReceiveActionBuilder;
import org.citrusframework.actions.ReceiveMessageBuilderFactory;
import org.citrusframework.actions.ReferenceResolverAwareBuilder;
import org.citrusframework.actions.SendActionBuilder;
import org.citrusframework.actions.SendMessageBuilderFactory;

public interface CamelActionBuilder<T extends TestAction, B extends TestActionBuilder.DelegatingTestActionBuilder<T>>
        extends ReferenceResolverAwareBuilder<T, B> {

    /**
     * Entrance for all Camel related Java DSL functionalities.
     */
    CamelActionBuilder<T, B> camelContext(Object camelContext);

    /**
     * Entrance for all Camel related Java DSL functionalities.
     */
    CamelContextActionBuilder<?, ?> camelContext();

    /**
     * Sends message using Camel endpointUri and components.
     */
    <S extends CamelExchangeActionBuilder<T, MB, S>, MB extends SendActionBuilder<T, M, MB>, M extends SendMessageBuilderFactory<T, M>> S send();

    /**
     * Receive messages using Camel endpointUri and components.
     */
    <S extends CamelExchangeActionBuilder<T, MB, S>, MB extends ReceiveActionBuilder<T, M, MB>, M extends ReceiveMessageBuilderFactory<T, M>> S receive();

    /**
     * Binds given component to the Camel context.
     */
    CamelCreateComponentActionBuilder<?, ?> bind(String name, Object component);

    /**
     * Binds a component to the Camel context.
     */
    CamelCreateComponentActionBuilder<?, ?> bind();

    /**
     * Creates new control bus test action builder and sets the Camel context.
     */
    CamelControlBusActionBuilder<?, ?> controlBus();

    /**
     * Perform actions on a Camel route such as start/stop/create and process.
     */
    CamelRouteActionBuilder<?, ?> route();

    /**
     * Perform actions with Camel infra.
     */
    CamelInfraActionBuilder<?, ?> infra();

    /**
     * Perform actions with Camel JBang.
     */
    CamelJBangActionBuilder<?, ?> jbang();

    interface BuilderFactory {

        CamelActionBuilder<?, ?> camel();

        default CamelActionBuilder<?, ?> camel(Object camelContext) {
            return camel().camelContext(camelContext);
        }

    }
}
