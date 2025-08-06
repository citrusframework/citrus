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

/**
 * Interface combines domain specific language methods for basic test actions available in Citrus.
 */
public interface BaseTestActions extends
        AntRunActionBuilder.BuilderFactory,
        ApplyTestBehaviorActionBuilder.BuilderFactory,
        CreateEndpointActionBuilder.BuilderFactory,
        CreateVariablesActionBuilder.BuilderFactory,
        EchoActionBuilder.BuilderFactory,
        FailActionBuilder.BuilderFactory,
        InputActionBuilder.BuilderFactory,
        LoadPropertiesActionBuilder.BuilderFactory,
        PurgeEndpointActionBuilder.BuilderFactory,
        ReceiveActionBuilder.BuilderFactory,
        ReceiveTimeoutActionBuilder.BuilderFactory,
        SendActionBuilder.BuilderFactory,
        SleepActionBuilder.BuilderFactory,
        StartServerActionBuilder.BuilderFactory,
        StopServerActionBuilder.BuilderFactory,
        StopTimeActionBuilder.BuilderFactory,
        StopTimerActionBuilder.BuilderFactory,
        TraceVariablesActionBuilder.BuilderFactory,
        TransformActionBuilder.BuilderFactory {
}
