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

import java.util.Map;

import org.citrusframework.TestAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.message.Message;

public interface KnativeEventSendActionBuilder<T extends TestAction, B extends KnativeEventSendActionBuilder<T, B>>
        extends KnativeActionBuilderBase<T, B> {

    B broker(String brokerName);

    B brokerUrl(String brokerUrl);

    B timeout(long timeout);

    B fork(boolean enabled);

    B event(Message message);

    B event(Object event);

    B eventData(String eventData);

    B attributes(Map<String, Object> ceAttributes);

    B attribute(String name, Object value);

    B client(Endpoint o);
}
