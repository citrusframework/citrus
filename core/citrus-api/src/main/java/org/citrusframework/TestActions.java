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

import org.citrusframework.api.actions.BaseTestActions;
import org.citrusframework.api.actions.agent.AgentTestActions;
import org.citrusframework.api.actions.camel.CamelTestActions;
import org.citrusframework.api.actions.docker.DockerTestActions;
import org.citrusframework.api.actions.http.HttpTestActions;
import org.citrusframework.api.actions.jbang.JBangTestActions;
import org.citrusframework.api.actions.jms.JmsTestActions;
import org.citrusframework.api.actions.knative.KnativeTestActions;
import org.citrusframework.api.actions.kubernetes.KubernetesTestActions;
import org.citrusframework.api.actions.openapi.OpenApiTestActions;
import org.citrusframework.api.actions.script.ScriptTestActions;
import org.citrusframework.api.actions.selenium.SeleniumTestActions;
import org.citrusframework.api.actions.sql.SqlTestActions;
import org.citrusframework.api.actions.testcontainers.TestcontainersTestActions;
import org.citrusframework.api.actions.ws.SoapTestActions;

/**
 * Interface combines domain specific language methods for all test actions available in Citrus.
 */
public interface TestActions extends
        BaseTestActions,
        AgentTestActions,
        CamelTestActions,
        DockerTestActions,
        HttpTestActions,
        JBangTestActions,
        JmsTestActions,
        KnativeTestActions,
        KubernetesTestActions,
        OpenApiTestActions,
        ScriptTestActions,
        SeleniumTestActions,
        SoapTestActions,
        SqlTestActions,
        TestcontainersTestActions {

}
