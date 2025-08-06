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

import org.citrusframework.actions.BaseTestActions;
import org.citrusframework.actions.agent.AgentTestActions;
import org.citrusframework.actions.camel.CamelTestActions;
import org.citrusframework.actions.docker.DockerTestActions;
import org.citrusframework.actions.http.HttpTestActions;
import org.citrusframework.actions.jbang.JBangTestActions;
import org.citrusframework.actions.jms.JmsTestActions;
import org.citrusframework.actions.knative.KnativeTestActions;
import org.citrusframework.actions.kubernetes.KubernetesTestActions;
import org.citrusframework.actions.openapi.OpenApiTestActions;
import org.citrusframework.actions.selenium.SeleniumTestActions;
import org.citrusframework.actions.sql.SqlTestActions;
import org.citrusframework.actions.testcontainers.TestcontainersTestActions;
import org.citrusframework.actions.ws.SoapTestActions;

/**
 * Interface combines domain specific language methods for all test actions available in Citrus.
 */
public interface TestActions extends
        BaseTestActions,
        AgentTestActions,
        CamelTestActions,
        DockerTestActions,
        SqlTestActions,
        HttpTestActions,
        JBangTestActions,
        JmsTestActions,
        KnativeTestActions,
        KubernetesTestActions,
        OpenApiTestActions,
        SeleniumTestActions,
        SoapTestActions,
        TestcontainersTestActions {

}
