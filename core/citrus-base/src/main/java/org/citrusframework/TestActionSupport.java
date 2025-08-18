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

import org.citrusframework.dsl.BaseTestActionSupport;
import org.citrusframework.dsl.agent.AgentTestActionSupport;
import org.citrusframework.dsl.camel.CamelTestActionSupport;
import org.citrusframework.dsl.docker.DockerTestActionSupport;
import org.citrusframework.dsl.http.HttpTestActionSupport;
import org.citrusframework.dsl.jbang.JBangTestActionSupport;
import org.citrusframework.dsl.jms.JmsTestActionSupport;
import org.citrusframework.dsl.knative.KnativeTestActionSupport;
import org.citrusframework.dsl.kubernetes.KubernetesTestActionSupport;
import org.citrusframework.dsl.openapi.OpenApiTestActionSupport;
import org.citrusframework.dsl.script.ScriptTestActionSupport;
import org.citrusframework.dsl.selenium.SeleniumTestActionSupport;
import org.citrusframework.dsl.soap.SoapTestActionSupport;
import org.citrusframework.dsl.sql.SqlTestActionSupport;
import org.citrusframework.dsl.testcontainers.TestcontainersTestActionSupport;
import org.citrusframework.validation.DefaultValidations;
import org.citrusframework.validation.Validations;

/**
 * Interface combines default implementations with domain specific language methods for all test actions available in Citrus.
 */
public interface TestActionSupport extends TestActions, TestActionContainers,
        BaseTestActionSupport,
        AgentTestActionSupport,
        CamelTestActionSupport,
        DockerTestActionSupport,
        HttpTestActionSupport,
        JBangTestActionSupport,
        JmsTestActionSupport,
        KnativeTestActionSupport,
        KubernetesTestActionSupport,
        OpenApiTestActionSupport,
        ScriptTestActionSupport,
        SeleniumTestActionSupport,
        SoapTestActionSupport,
        SqlTestActionSupport,
        TestcontainersTestActionSupport {

    default TestActions actions() {
        return this;
    }

    default TestActionContainers containers() {
        return this;
    }

    default Validations validation() {
        return new DefaultValidations();
    }
}
