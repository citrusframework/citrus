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

package org.citrusframework;

import org.citrusframework.actions.*;
import org.citrusframework.actions.agent.AgentActionBuilder;
import org.citrusframework.actions.camel.CamelActionBuilder;
import org.citrusframework.actions.docker.DockerActionBuilder;
import org.citrusframework.actions.http.HttpActionBuilder;
import org.citrusframework.actions.jbang.JBangActionBuilder;
import org.citrusframework.actions.knative.KnativeActionBuilder;
import org.citrusframework.actions.kubernetes.KubernetesActionBuilder;
import org.citrusframework.actions.openapi.OpenApiActionBuilder;
import org.citrusframework.actions.selenium.SeleniumActionBuilder;
import org.citrusframework.actions.sql.ExecutePlsqlActionBuilder;
import org.citrusframework.actions.sql.ExecuteSqlActionBuilder;
import org.citrusframework.actions.sql.ExecuteSqlQueryActionBuilder;
import org.citrusframework.actions.testcontainers.TestcontainersActionBuilder;

/**
 * Interface combines domain specific language methods for all test actions available in Citrus.
 */
public interface TestActions extends
        AgentActionBuilder.BuilderFactory,
        AntRunActionBuilder.BuilderFactory,
        ApplyTestBehaviorActionBuilder.BuilderFactory,
        CamelActionBuilder.BuilderFactory,
        CreateEndpointActionBuilder.BuilderFactory,
        CreateVariablesActionBuilder.BuilderFactory,
        DockerActionBuilder.BuilderFactory,
        EchoActionBuilder.BuilderFactory,
        ExecutePlsqlActionBuilder.BuilderFactory,
        ExecuteSqlActionBuilder.BuilderFactory,
        ExecuteSqlQueryActionBuilder.BuilderFactory,
        FailActionBuilder.BuilderFactory,
        HttpActionBuilder.BuilderFactory,
        InputActionBuilder.BuilderFactory,
        JBangActionBuilder.BuilderFactory,
        KnativeActionBuilder.BuilderFactory,
        KubernetesActionBuilder.BuilderFactory,
        LoadPropertiesActionBuilder.BuilderFactory,
        OpenApiActionBuilder.BuilderFactory,
        PurgeEndpointActionBuilder.BuilderFactory,
        ReceiveActionBuilder.BuilderFactory,
        ReceiveTimeoutActionBuilder.BuilderFactory,
        SeleniumActionBuilder.BuilderFactory,
        SendActionBuilder.BuilderFactory,
        SleepActionBuilder.BuilderFactory,
        StartServerActionBuilder.BuilderFactory,
        StopServerActionBuilder.BuilderFactory,
        StopTimeActionBuilder.BuilderFactory,
        StopTimerActionBuilder.BuilderFactory,
        TestcontainersActionBuilder.BuilderFactory,
        TraceVariablesActionBuilder.BuilderFactory,
        TransformActionBuilder.BuilderFactory {

}
