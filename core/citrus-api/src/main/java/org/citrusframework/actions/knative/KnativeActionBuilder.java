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

package org.citrusframework.actions.knative;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;

public interface KnativeActionBuilder<T extends TestAction, B extends TestActionBuilder<T>> {

    /**
     * Use a custom Knative or Kubernetes client.
     */
    KnativeActionBuilder<T, B> client(Object client);

    /**
     * Produce and consume events for the Knative broker.
     */
    KnativeEventActionBuilder event();

    /**
     * Performs action on Knative channels.
     */
    KnativeChannelActionBuilder channels();

    /**
     * Performs action on Knative subscriptions.
     */
    KnativeSubscriptionActionBuilder subscriptions();

    /**
     * Performs action on Knative trigger.
     */
    KnativeTriggerActionBuilder trigger();

    /**
     * Performs action on Knative brokers.
     */
    KnativeBrokerActionBuilder brokers();

    interface BuilderFactory {

        KnativeActionBuilder<?, ?> knative();

    }
}
